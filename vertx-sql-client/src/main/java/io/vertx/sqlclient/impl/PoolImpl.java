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

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.pool.SqlConnectionPool;
import io.vertx.sqlclient.impl.tracing.QueryTracer;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Function;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PoolImpl extends SqlClientBase implements Pool, Closeable {

  private final VertxInternal vertx;
  private final SqlConnectionPool pool;
  private final CloseFuture closeFuture;
  private final long idleTimeout;
  private final long connectionTimeout;
  private final long cleanerPeriod;
  private final int pipeliningLimit;
  private volatile Handler<SqlConnectionPool.PooledConnection> connectionInitializer;
  private long timerID;
  private volatile Function<Context, Future<SqlConnection>> connectionProvider;

  public static final String PROPAGATABLE_CONNECTION = "propagatable_connection";

  public PoolImpl(VertxInternal vertx,
                  Driver driver,
                  QueryTracer tracer,
                  ClientMetrics metrics,
                  int pipeliningLimit,
                  PoolOptions poolOptions,
                  Function<Connection, Future<Void>> afterAcquire,
                  Function<Connection, Future<Void>> beforeRecycle,
                  CloseFuture closeFuture) {
    super(driver, tracer, metrics);

    this.idleTimeout = MILLISECONDS.convert(poolOptions.getIdleTimeout(), poolOptions.getIdleTimeoutUnit());
    this.connectionTimeout = MILLISECONDS.convert(poolOptions.getConnectionTimeout(), poolOptions.getConnectionTimeoutUnit());
    this.cleanerPeriod = poolOptions.getPoolCleanerPeriod();
    this.timerID = -1L;
    this.pipeliningLimit = pipeliningLimit;
    this.vertx = vertx;
    this.pool = new SqlConnectionPool(ctx -> connectionProvider.apply(ctx), () -> connectionInitializer, afterAcquire, beforeRecycle, vertx, idleTimeout, poolOptions.getMaxSize(), pipeliningLimit, poolOptions.getMaxWaitQueueSize(), poolOptions.getEventLoopSize());
    this.closeFuture = closeFuture;
  }

  public Pool init() {
    closeFuture.add(this);
    if (idleTimeout > 0 && cleanerPeriod > 0) {
      synchronized (this) {
        timerID = vertx.setTimer(cleanerPeriod, id -> {
          checkExpired();
        });
      }
    }
    return this;
  }

  public Pool connectionProvider(Function<Context, Future<SqlConnection>> connectionProvider) {
    if (connectionProvider == null) {
      throw new NullPointerException();
    }
    this.connectionProvider = connectionProvider;
    return this;
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
    if (pipeliningLimit > 1) {
      return current.failedFuture("Cannot acquire a connection on a pipelined pool");
    }
    Object metric;
    if (metrics != null) {
      metric = metrics.enqueueRequest();
    } else {
      metric = null;
    }
    Promise<SqlConnectionPool.PooledConnection> promise = current.promise();
    acquire(current, connectionTimeout, promise);
    if (metrics != null) {
      promise.future().onComplete(ar -> {
        metrics.dequeueRequest(metric);
      });
    }
    return promise.future().map(conn -> {
      SqlConnectionInternal wrapper = driver.wrapConnection(current, conn.factory(), conn, tracer, metrics);
      conn.init(wrapper);
      return wrapper;
    });
  }

  public static <T> Future<@Nullable T> startPropagatableConnection(Pool pool, Function<SqlConnection, Future<@Nullable T>> function) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    return pool.getConnection().onComplete(handler -> context.putLocal(PROPAGATABLE_CONNECTION, handler.result()))
      .flatMap(conn -> conn
        .begin()
        .flatMap(tx -> function
          .apply(conn)
          .compose(
            res -> tx
              .commit()
              .flatMap(v -> context.succeededFuture(res)),
            err -> {
              if (err instanceof TransactionRollbackException) {
                return context.failedFuture(err);
              } else {
                return tx
                  .rollback()
                  .compose(v -> context.failedFuture(err), failure -> context.failedFuture(err));
              }
            }))
        .onComplete(ar -> conn.close().onComplete(v -> context.removeLocal(PROPAGATABLE_CONNECTION))));
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

  private void acquire(ContextInternal context, long timeout, Handler<AsyncResult<SqlConnectionPool.PooledConnection>> completionHandler) {
    pool.acquire(context, timeout, completionHandler);
  }

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

  @Override
  public Pool connectHandler(Handler<SqlConnection> handler) {
    if (handler != null) {
      connectionInitializer = conn -> {
        ContextInternal current = vertx.getContext();
        SqlConnectionInternal wrapper = driver.wrapConnection(current, conn.factory(), conn, tracer, metrics);
        conn.init(wrapper);
        current.dispatch(wrapper, handler);
      };
    } else {
      connectionInitializer = null;
    }
    return this;
  }

  private Future<Void> doClose() {
    synchronized (this) {
      if (timerID >= 0) {
        vertx.cancelTimer(timerID);
        timerID = -1;
      }
    }
    return pool.close().onComplete(v -> {
      if (metrics != null) {
        metrics.close();
      }
    });
  }

  public int size() {
    return pool.size();
  }
}
