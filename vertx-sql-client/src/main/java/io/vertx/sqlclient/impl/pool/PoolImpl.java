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

package io.vertx.sqlclient.impl.pool;

import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.*;
import io.vertx.core.internal.CloseFuture;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.TransactionPropagationLocal;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.internal.SqlClientBase;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.sqlclient.spi.connection.ConnectionContext;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.Driver;

import java.util.function.Function;
import java.util.function.Supplier;

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
  private final long maxLifetime;
  private final long cleanerPeriod;
  private final boolean pipelined;
  private final Handler<SqlConnection> connectionInitializer;
  private final ConnectionWrapper<?> connectionWrapper;
  private long timerID;

  public <O extends SqlConnectOptions> PoolImpl(VertxInternal vertx,
                  Driver driver,
                  boolean pipelined,
                  PoolOptions poolOptions,
                  Function<Connection, Future<Void>> afterAcquire,
                  Function<Connection, Future<Void>> beforeRecycle,
                  ConnectionFactory<O> connectionFactory,
                  Supplier<Future<O>> connectionProvider,
                  Handler<SqlConnection> connectionInitializer,
                  ConnectionWrapper connectionWrapper,
                  CloseFuture closeFuture) {
    super(driver);

    Handler<SqlConnectionPool.PooledConnection> hook = connectionInitializer != null ? this::initializeConnection : null;

    VertxMetrics metrics = vertx.metrics();
    PoolMetrics poolMetrics;
    if (metrics != null) {
      poolMetrics = metrics.createPoolMetrics("sql", poolOptions.getName(), poolOptions.getMaxSize());
    } else {
      poolMetrics = null;
    }

    this.connectionWrapper = connectionWrapper;
    this.idleTimeout = MILLISECONDS.convert(poolOptions.getIdleTimeout(), poolOptions.getIdleTimeoutUnit());
    this.connectionTimeout = MILLISECONDS.convert(poolOptions.getConnectionTimeout(), poolOptions.getConnectionTimeoutUnit());
    this.maxLifetime = MILLISECONDS.convert(poolOptions.getMaxLifetime(), poolOptions.getMaxLifetimeUnit());
    this.cleanerPeriod = poolOptions.getPoolCleanerPeriod();
    this.timerID = -1L;
    this.pipelined = pipelined;
    this.vertx = vertx;
    this.pool = new SqlConnectionPool(connectionProvider, connectionFactory, poolMetrics, hook, afterAcquire,
      beforeRecycle, vertx, idleTimeout, maxLifetime, poolOptions.getMaxSize(), pipelined,
      poolOptions.getMaxWaitQueueSize(), poolOptions.getEventLoopSize());
    this.closeFuture = closeFuture;
    this.connectionInitializer = connectionInitializer;
  }

  private void initializeConnection(SqlConnectionPool.PooledConnection conn) {
    if (connectionInitializer != null) {
      ContextInternal current = vertx.getContext();
      SqlConnectionInternal wrapper = connectionWrapper.wrap(current, conn.factory(), conn);
      conn.init((ConnectionContext) wrapper);
      current.dispatch(wrapper, connectionInitializer);
    }
  }

  public Pool init() {
    closeFuture.add(this);
    if ((idleTimeout > 0 || maxLifetime > 0) && cleanerPeriod > 0) {
      synchronized (this) {
        timerID = vertx.setTimer(cleanerPeriod, id -> {
          runEviction();
        });
      }
    }
    return this;
  }

  private void runEviction() {
    synchronized (this) {
      if (timerID == -1) {
        // Cancelled
        return;
      }
      timerID = vertx.setTimer(cleanerPeriod, id -> {
        runEviction();
      });
    }
    pool.evict();
  }

  @Override
  protected <T> PromiseInternal<T> promise() {
    return vertx.promise();
  }

  protected ContextInternal context() {
    return vertx.getOrCreateContext();
  }

  @Override
  public Future<SqlConnection> getConnection() {
    ContextInternal current = vertx.getOrCreateContext();
    if (pipelined) {
      return current.failedFuture("Cannot acquire a connection on a pipelined pool");
    }
    Promise<SqlConnectionPool.PooledConnection> promise = current.promise();
    acquire(current, connectionTimeout, promise);
    return promise.future().map(conn -> {
      SqlConnectionInternal wrapper = connectionWrapper.wrap(current, conn.factory(), conn);
      conn.init((ConnectionContext) wrapper);
      return wrapper;
    });
  }

  public static <T> Future<@Nullable T> startPropagatableConnection(Pool pool, Function<SqlConnection, Future<@Nullable T>> function) {
    ContextInternal context = (ContextInternal) Vertx.currentContext();
    return pool.getConnection().onComplete(handler -> context.putLocal(TransactionPropagationLocal.KEY, handler.result()))
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
        .onComplete(ar -> conn.close().onComplete(v -> context.removeLocal(TransactionPropagationLocal.KEY))));
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
    pool.execute(cmd, handler);
  }

  private void acquire(ContextInternal context, long timeout, Completable<SqlConnectionPool.PooledConnection> completionHandler) {
    pool.acquire(context, timeout, completionHandler);
  }

  @Override
  public void close(Completable<Void> completion) {
    doClose().onComplete(completion);
  }

  @Override
  public Future<Void> close() {
    Promise<Void> promise = vertx.promise();
    closeFuture.close(promise);
    return promise.future();
  }

  private Future<Void> doClose() {
    synchronized (this) {
      if (timerID >= 0) {
        vertx.cancelTimer(timerID);
        timerID = -1;
      }
    }
    return pool.close();
  }

  public int size() {
    return pool.size();
  }
}
