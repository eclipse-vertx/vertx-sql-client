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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.*;
import com.julienviet.pgclient.impl.provider.ConnectionPoolProvider;
import com.julienviet.pgclient.impl.provider.ConnectionProvider;
import com.julienviet.pgclient.impl.provider.SharedConnectionProvider;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;

import java.util.function.Function;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public class PgPoolImpl extends PgOperationsImpl<PgPoolImpl> implements PgPool {

  private final Context context;
  private final ConnectionProvider provider;

  public PgPoolImpl(Context context, PgClientImpl client, int maxSize, PoolingMode mode) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.context = context;
    this.provider = mode == PoolingMode.STATEMENT ? new SharedConnectionProvider(client::_connect) : new ConnectionPoolProvider(client::_connect, maxSize);
  }

  @Override
  public PgPoolImpl query(String sql, Handler<AsyncResult<PgResult<Tuple>>> handler) {
    return (PgPoolImpl) super.query(sql, handler);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      provider.acquire(new ConnectionWaiter(handler));
    } else {
      context.runOnContext(v -> getConnection(handler));
    }
  }

  @Override
  protected void schedulePrepared(String sql, Function<AsyncResult<PreparedStatement>, CommandBase> supplier) {
    Context current = Vertx.currentContext();
    if (current == context) {
      provider.acquire(new CommandWaiter() {
        @Override
        protected void onSuccess(Connection conn) {
          conn.schedulePrepared(sql, supplier, v -> {
            conn.close(this);
          });
        }
        @Override
        protected void onFailure(Throwable cause) {
          CommandBase cmd = supplier.apply(Future.failedFuture(cause));
          if (cmd != null) {
            cmd.fail(cause);
          }
        }
      });
    } else {
      context.runOnContext(v -> schedulePrepared(sql, supplier));
    }
  }

  @Override
  protected void schedule(CommandBase cmd) {
    Context current = Vertx.currentContext();
    if (current == context) {
      provider.acquire(new CommandWaiter() {
        @Override
        protected void onSuccess(Connection conn) {
          conn.schedule(cmd, v -> {
            conn.close(this);
          });
        }
        @Override
        protected void onFailure(Throwable cause) {
          cmd.fail(cause);
        }
      });
    } else {
      context.runOnContext(v -> schedule(cmd));
    }
  }

  private abstract class CommandWaiter implements Connection.Holder, Handler<AsyncResult<Connection>> {

    private Connection conn;

    protected abstract void onSuccess(Connection conn);

    protected abstract void onFailure(Throwable cause);

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        this.conn = conn;
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

    @Override
    public Connection connection() {
      return conn;
    }
  }

  private class ConnectionWaiter implements Handler<AsyncResult<Connection>> {

    private final Handler<AsyncResult<PgConnection>> handler;

    private ConnectionWaiter(Handler<AsyncResult<PgConnection>> handler) {
      this.handler = handler;
    }

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        PgConnectionImpl holder = new PgConnectionImpl(context, conn);
        conn.init(holder);
        handler.handle(Future.succeededFuture(holder));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }
  }

  @Override
  public void close() {
    provider.close();
  }
}
