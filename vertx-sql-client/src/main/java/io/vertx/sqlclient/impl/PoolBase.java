/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.impl;

import io.vertx.core.*;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.pool.SqlConnectionPool;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.List;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class PoolBase<P extends Pool> extends SqlClientBase<P> implements Pool, Closeable {

  private final VertxInternal vertx;
  private final ConnectionFactory factory;
  private final SqlConnectionPool pool;
  private final CloseFuture closeFuture;
  private final long idleTimeout;
  private final long connectionTimeout;
  private final long cleanerPeriod;
  private long timerID;

  public PoolBase(VertxInternal vertx,
                  ConnectionFactory factory,
                  QueryTracer tracer,
                  ClientMetrics metrics,
                  int pipeliningLimit,
                  PoolOptions poolOptions) {
    super(tracer, metrics);

    this.factory = factory;
    this.idleTimeout = MILLISECONDS.convert(poolOptions.getIdleTimeout(), poolOptions.getIdleTimeoutUnit());
    this.connectionTimeout = MILLISECONDS.convert(poolOptions.getConnectionTimeout(), poolOptions.getConnectionTimeoutUnit());
    this.cleanerPeriod = poolOptions.getPoolCleanerPeriod();
    this.timerID = -1L;
    this.vertx = vertx;
    this.pool = new SqlConnectionPool(factory, this.vertx, idleTimeout, poolOptions.getMaxSize(), pipeliningLimit, poolOptions.getMaxWaitQueueSize());
    this.closeFuture = new CloseFuture();
  }

  public P init() {
    closeFuture.add(this);
    if (idleTimeout > 0 && cleanerPeriod > 0) {
      synchronized (this) {
        timerID = vertx.setTimer(cleanerPeriod, id -> {
          checkExpired();
        });
      }
    }
    return (P) this;
  }

  private void checkExpired() {
    synchronized (this) {
      if (timerID == -1) {
        // Cancelled
        return;
      }
      timerID = vertx.setTimer(cleanerPeriod, id -> {
        checkExpired();
      });
    }
    pool.checkExpired();
  }

  public CloseFuture closeFuture() {
    return closeFuture;
  }

  @Override
  protected <T> PromiseInternal<T> promise() {
    return vertx.promise();
  }

  protected ContextInternal context() {
    return vertx.getOrCreateContext();
  }

  @Override
  protected <T> PromiseInternal<T> promise(Handler<AsyncResult<T>> handler) {
    return vertx.promise(handler);
  }

  @Override
  public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {
    Future<SqlConnection> fut = getConnection();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public Future<SqlConnection> getConnection() {
    ContextInternal current = vertx.getOrCreateContext();
    Object metric;
    if (metrics != null) {
      metric = metrics.enqueueRequest();
    } else {
      metric = null;
    }
    Promise<Connection> promise = current.promise();
    acquire(current, connectionTimeout, promise);
    if (metrics != null) {
      promise.future().onComplete(ar -> {
        metrics.dequeueRequest(metric);
      });
    }
    return promise.future().map(conn -> {
      SqlConnectionImpl wrapper = wrap(current, conn);
      conn.init(wrapper);
      return wrapper;
    });
  }

  @Override
  public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
    Object metric;
    if (metrics != null) {
      metric = metrics.enqueueRequest();
    } else {
      metric = null;
    }
    Future<R> fut = pool.execute(context, cmd);
    if (metrics != null) {
      fut.onComplete(ar -> {
        if (metrics != null) {
          metrics.dequeueRequest(metric);
        }
      });
    }
    return fut;
  }

  private void acquire(ContextInternal context, long timeout, Handler<AsyncResult<Connection>> completionHandler) {
    pool.acquire(context, timeout, completionHandler);
  }

  private static abstract class CommandWaiter implements Connection.Holder, Handler<AsyncResult<Connection>> {

    protected abstract void onSuccess(Connection conn);

    protected abstract void onFailure(Throwable cause);

    @Override
    public void handleEvent(Object event) {
      // What should we do ?
    }

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        conn.init(this);
        onSuccess(conn);
      } else {
        onFailure(ar.cause());
      }
    }

    @Override
    public void handleClosed() {
    }

    @Override
    public void handleException(Throwable err) {
    }
  }

  protected abstract SqlConnectionImpl wrap(ContextInternal context, Connection conn);

  @Override
  public void close(Promise<Void> completion) {
    doClose().onComplete(completion);
  }

  @Override
  public Future<Void> close() {
    Promise<Void> promise = vertx.promise();
    closeFuture.close(promise);
    return promise.future();
  }

  @Override
  public void close(Handler<AsyncResult<Void>> handler) {
    closeFuture.close(vertx.promise(handler));
  }

  private Future<Void> doClose() {
    synchronized (this) {
      if (timerID >= 0) {
        vertx.cancelTimer(timerID);
        timerID = -1;
      }
    }
    ContextInternal ctx = context();
    return pool.close().eventually(v -> {
      PromiseInternal<Void> promise = ctx.promise();
      factory.close(promise);
      return promise;
    }).onComplete(v -> {
      if (metrics != null) {
        metrics.close();
      }
    });
  }

  public int size() {
    return pool.size();
  }

  public void check(Handler<AsyncResult<List<Integer>>> handler) {
    pool.check(handler);
  }
}
