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
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgConnectionImpl extends PgClientBase<PgConnectionImpl> implements PgConnection, Connection.Holder {

  private final Context context;
  public final Connection conn;
  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> closeHandler;
  private Transaction tx;

  public PgConnectionImpl(Context context, Connection conn) {
    this.context = context;
    this.conn = conn;
  }

  @Override
  public PgQuery createQuery(String sql) {
    return new SimplePgQueryImpl(sql, this::schedule);
  }


  @Override
  public Connection connection() {
    return conn;
  }

  @Override
  public void handleClosed() {
    Handler<Void> handler = closeHandler;
    if (handler != null) {
      context.runOnContext(handler);
    }
  }

  @Override
  protected void schedule(CommandBase<?> cmd) {
    if (tx != null) {
      tx.schedule(cmd);
    } else {
      conn.schedule(cmd);
    }
  }

  @Override
  public void handleException(Throwable err) {
    Handler<Throwable> handler = exceptionHandler;
    if (handler != null) {
      context.runOnContext(v -> {
        handler.handle(err);
      });
    } else {
      err.printStackTrace();
    }
  }

  @Override
  public boolean isSSL() {
    return conn.isSsl();
  }

  @Override
  public PgConnection closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return this;
  }

  @Override
  public PgConnection exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgTransaction begin() {
    if (tx != null) {
      throw new IllegalStateException();
    }
    tx = new Transaction(context, conn, v -> {
      tx = null;
    });
    return tx;
  }

  @Override
  public void close() {
    if (tx != null) {
      tx.rollback(ar -> conn.close(this));
      tx = null;
    } else {
      conn.close(this);
    }
  }

  @Override
  public PgConnection prepare(String sql, Handler<AsyncResult<PgPreparedStatement>> handler) {
    schedule(new PrepareStatementCommand(sql, ar -> {
      if (ar.succeeded()) {
        handler.handle(Future.succeededFuture(new PgPreparedStatementImpl(conn, ar.result())));
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    }));
    return this;
  }
}
