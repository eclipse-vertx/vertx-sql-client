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
import com.julienviet.pgclient.impl.pool.ConcurrentConnectionPool;
import com.julienviet.pgclient.impl.pool.ConnectionPool;
import com.julienviet.pgclient.impl.pool.SharedConnectionPool;
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
public class PgPoolImpl extends PgOperationsImpl implements PgPool {

  private final Context context;
  private final ConnectionPool pool;

  public PgPoolImpl(Context context, PgClientImpl client, int maxSize, PoolingMode mode) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.context = context;
    this.pool = mode == PoolingMode.STATEMENT ? new SharedConnectionPool(client::_connect) : new ConcurrentConnectionPool(client::_connect, maxSize);
  }

  @Override
  public void getConnection(Handler<AsyncResult<PgConnection>> handler) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pool.acquire(new ConnectionWaiter(handler));
    } else {
      context.runOnContext(v -> getConnection(handler));
    }
  }

  @Override
  protected void schedule(CommandBase cmd) {
    Context current = Vertx.currentContext();
    if (current == context) {
      pool.acquire(new CommandWaiter(cmd));
    } else {
      context.runOnContext(v -> schedule(cmd));
    }
  }

  private class CommandWaiter implements ConnectionHolder, Handler<AsyncResult<Connection>> {

    private final CommandBase cmd;
    private Connection conn;

    private CommandWaiter(CommandBase cmd) {
      this.cmd = cmd;
    }

    @Override
    public void handle(AsyncResult<Connection> ar) {
      if (ar.succeeded()) {
        Connection conn = ar.result();
        this.conn = conn;
        conn.init(this);
        conn.schedule(cmd, v -> {
          conn.close(this);
        });
      } else {
        throw new UnsupportedOperationException("Handle me gracefully");
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
    pool.close();
  }
}
