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

import io.vertx.core.Closeable;
import io.vertx.core.Promise;
import io.vertx.core.impl.CloseFuture;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.pool.ConnectionPool;
import io.vertx.sqlclient.transaction.TransactionOptions;

import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class PoolBase<P extends Pool> extends SqlClientBase<P> implements Pool, Closeable {

  private final VertxInternal vertx;
  private final ConnectionFactory factory;
  private final ConnectionPool pool;
  private final CloseFuture closeFuture;

  public PoolBase(ContextInternal context, ConnectionFactory factory, PoolOptions poolOptions) {
    this.vertx = context.owner();
    this.factory = factory;
    this.pool = new ConnectionPool(factory, context, poolOptions.getMaxSize(), poolOptions.getMaxWaitQueueSize());
    this.closeFuture = new CloseFuture(this);
  }

  public CloseFuture closeFuture() {
    return closeFuture;
  }

  @Override
  protected <T> Promise<T> promise() {
    return vertx.promise();
  }

  @Override
  protected <T> Promise<T> promise(Handler<AsyncResult<T>> handler) {
    return vertx.promise(handler);
  }

  /**
   * Create a connection and connect to the database server.
   *
   * @param completionHandler the handler completed with the result
   */
  public abstract void connect(Handler<AsyncResult<Connection>> completionHandler);

  private void acquire(Handler<AsyncResult<Connection>> completionHandler) {
    pool.acquire(completionHandler);
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
    Promise<Connection> promise = current.promise();
    acquire(promise);
    return promise.future().map(conn -> {
      SqlConnectionImpl wrapper = wrap(current, conn);
      conn.init(wrapper);
      return wrapper;
    });
  }

  @Override
  public <T> void withTransaction(Function<SqlClient, Future<T>> function, Handler<AsyncResult<T>> handler) {
    withTransaction(TransactionOptions.DEFAULT_TX_OPTIONS, function, handler);
  }

  @Override
  public <T> Future<T> withTransaction(Function<SqlClient, Future<T>> function) {
    return withTransaction(TransactionOptions.DEFAULT_TX_OPTIONS, function);
  }

  @Override
  public <T> void withTransaction(TransactionOptions txOptions, Function<SqlClient, Future<T>> function, Handler<AsyncResult<T>> handler) {
    Future<T> res = withTransaction(txOptions, function);
    if (handler != null) {
      res.onComplete(handler);
    }
  }

  @Override
  public <T> Future<T> withTransaction(TransactionOptions txOptions, Function<SqlClient, Future<T>> function) {
    return getConnection()
      .flatMap(conn -> conn
        .begin(txOptions)
        .flatMap(tx -> function
          .apply(conn)
          .compose(
            res -> tx
              .commit()
              .flatMap(v -> Future.succeededFuture(res)),
            err -> tx
              .rollback()
              .flatMap(v -> Future.failedFuture(err))))
        .onComplete(ar -> conn.close()));
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Promise<R> promise) {
    acquire(new CommandWaiter() {
      @Override
      protected void onSuccess(Connection conn) {
        conn.schedule(cmd, promise);
        // Use null promise instead
        conn.close(this, Promise.promise());
      }
      @Override
      protected void onFailure(Throwable cause) {
        promise.fail(cause);
      }
    });
  }

  private abstract class CommandWaiter implements Connection.Holder, Handler<AsyncResult<Connection>> {

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
    return pool.close().flatMap(v -> factory.close());
  }
}
