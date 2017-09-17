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

package com.julienviet.pgclient.impl.pool;

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.PgPreparedStatement;
import io.vertx.core.AsyncResult;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class Proxy implements PgConnection, PgPoolImpl.Holder {

  private final Context context;
  private final Handler<AsyncResult<PgConnection>> handler;
  private PgConnection conn;
  private final AtomicBoolean closed = new AtomicBoolean();
  private volatile Handler<Throwable> exceptionHandler;
  private volatile Handler<Void> closeHandler;
  private PgPoolImpl pool;

  Proxy(PgPoolImpl pool, Context context, Handler<AsyncResult<PgConnection>> handler) {
    this.pool = pool;
    this.context = context;
    this.handler = handler;
  }

  public void handleException(Throwable err) {
    Handler<Throwable> handler = this.exceptionHandler;
    if (!closed.get() && handler != null) {
      handler.handle(err);
    }
  }

  @Override
  public Context context() {
    return context;
  }

  @Override
  public PgConnection connection() {
    return conn;
  }

  @Override
  public void handle(AsyncResult<PgConnection> event) {
    conn = event.result();
    handler.handle(event.map(this));
  }

  public void handleClosed() {
    Handler<Void> handler = this.closeHandler;
    if (closed.compareAndSet(false, true) && handler != null) {
      handler.handle(null);
    }
  }

  private void checkClosed() {
    if (closed.get()) {
      throw new IllegalStateException("Connection closed");
    }
  }

  @Override
  public void query(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler) {
    if (closed.get()) {
      handler.handle(Future.failedFuture("Connection closed"));
      return;
    }
    conn.query(sql, handler);
  }

  @Override
  public void update(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler) {
    if (closed.get()) {
      handler.handle(Future.failedFuture("Connection closed"));
      return;
    }
    conn.update(sql, handler);
  }

  @Override
  public PgConnection execute(String sql, Handler<AsyncResult<ResultSet>> handler) {
    if (closed.get()) {
      handler.handle(Future.failedFuture("Connection closed"));
      return this;
    }
    conn.execute(sql, handler);
    return this;
  }

  @Override
  public void update(String sql, Handler<AsyncResult<UpdateResult>> handler) {
    if (closed.get()) {
      handler.handle(Future.failedFuture("Connection closed"));
      return;
    }
    conn.update(sql, handler);
  }

  @Override
  public void query(String sql, Handler<AsyncResult<ResultSet>> handler) {
    if (closed.get()) {
      handler.handle(Future.failedFuture("Connection closed"));
      return;
    }
    conn.query(sql, handler);
  }

  @Override
  public PgConnection exceptionHandler(Handler<Throwable> handler) {
    exceptionHandler = handler;
    return this;
  }

  @Override
  public PgConnection closeHandler(Handler<Void> handler) {
    closeHandler = handler;
    return this;
  }

  @Override
  public PgPreparedStatement prepare(String sql) {
    checkClosed();
    return conn.prepare(sql);
  }

  @Override
  public boolean isSSL() {
    checkClosed();
    return conn.isSSL();
  }

  @Override
  public void close() {
    if (closed.compareAndSet(false, true)) {
      pool.close(this);
    }
  }
}
