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
import io.vertx.core.impl.PromiseInternal;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class PoolBase<P extends PoolBase<P>> extends SqlClientBase<P> implements Pool {

  private final ContextInternal context;
  private final ConnectionPool pool;
  private final boolean closeVertx;

  public PoolBase(ContextInternal context, boolean closeVertx, PoolOptions options) {
    int maxSize = options.getMaxSize();
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.context = context;
    this.pool = new ConnectionPool(h -> connect(context, h), maxSize, options.getMaxWaitQueueSize());
    this.closeVertx = closeVertx;
  }

  @Override
  protected <T> Promise<T> promise() {
    return context.owner().promise();
  }

  @Override
  protected <T> Promise<T> promise(Handler<AsyncResult<T>> handler) {
    return context.owner().promise(handler);
  }

  /**
   * Create a connection and connect to the database server.
   *
   * @param context the connection context
   * @param completionHandler the handler completed with the result
   */
  public abstract void connect(ContextInternal context, Handler<AsyncResult<Connection>> completionHandler);

  @Override
  public void getConnection(Handler<AsyncResult<SqlConnection>> handler) {
    Future<SqlConnection> fut = getConnection();
    if (handler != null) {
      fut.setHandler(handler);
    }
  }

  @Override
  public Future<SqlConnection> getConnection() {
    ContextInternal current = context.owner().getOrCreateContext();
    Promise<Connection> promise = current.promise();
    context.dispatch(promise, pool::acquire);
    return promise.future().map(conn -> {
      SqlConnectionImpl wrapper = wrap(current, conn);
      conn.init(wrapper);
      return wrapper;
    });
  }

  @Override
  public Future<Transaction> begin() {
    Future<SqlConnection> fut = getConnection();
    return fut.map(c -> {
      SqlConnectionImpl conn = (SqlConnectionImpl) c;
      return conn.begin(true);
    });
  }

  @Override
  public void begin(Handler<AsyncResult<Transaction>> handler) {
    Future<Transaction> fut = begin();
    if (handler != null) {
      fut.setHandler(handler);
    }
  }

  @Override
  public <R> void schedule(CommandBase<R> cmd, Promise<R> handler) {
    ContextInternal current = context.owner().getOrCreateContext();
    PromiseInternal<R> promise = current.promise(handler);
    if (current == context) {
      biltoSchedule(cmd, promise);
    } else {
      context.runOnContext(v -> biltoSchedule(cmd, promise));
    }
  }

  private <R> void biltoSchedule(CommandBase<R> cmd, Promise<R> promise) {
    pool.acquire(new CommandWaiter() {
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
    public void handleNotification(int processId, String channel, String payload) {
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
    pool.close();
    if (closeVertx) {
      context.owner().close();
    }
  }

  @Override
  public void close() {
    Context current = Vertx.currentContext();
    if (current == context) {
      doClose();
    } else {
      context.runOnContext(v -> doClose());
    }
  }
}
