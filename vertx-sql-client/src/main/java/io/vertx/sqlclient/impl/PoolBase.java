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

import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class PoolBase<P extends Pool> extends SqlClientBase<P> implements Pool {

  private final VertxInternal vertx;
  private final boolean closeVertx;

  public PoolBase(VertxInternal vertx, boolean closeVertx) {
    this.vertx = vertx;
    this.closeVertx = closeVertx;
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

  public abstract void acquire(Handler<AsyncResult<Connection>> completionHandler);

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
  public Future<Transaction> begin() {
    return getConnection().flatMap(conn -> {
      Future<Transaction> fut = conn.begin();
      fut.onComplete(ar1 -> {
        if (ar1.succeeded()) {
          Transaction tx = ar1.result();
          tx.result().onComplete(ar2 -> {
            conn.close();
          });
        } else {
          conn.close();
        }
      });
      return fut;
    });
  }

  @Override
  public void begin(Handler<AsyncResult<Transaction>> handler) {
    Future<Transaction> fut = begin();
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Promise<R> promise) {
    acquire(new CommandWaiter() {
      @Override
      protected void onSuccess(Connection conn) {
        conn.schedule(cmd, promise);
        conn.close(this);
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

  protected void doClose() {
    if (closeVertx) {
      vertx.close();
    }
  }

  @Override
  public void close() {
    doClose();
  }
}
