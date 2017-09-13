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

import com.julienviet.pgclient.PgBatch;
import com.julienviet.pgclient.PgPreparedStatement;
import com.julienviet.pgclient.PgQuery;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PgPreparedStatement {

  private final DbConnection conn;
  final String sql;
  final AtomicBoolean closed = new AtomicBoolean();
  boolean parsed;
  final String stmt;
  final boolean cached;

  PreparedStatementImpl(DbConnection conn, String sql, String stmt, boolean cached) {
    this.conn = conn;
    this.sql = sql;
    this.stmt = stmt;
    this.cached = cached;
  }

  @Override
  public PgQuery query(List<Object> params) {
    return new PreparedQuery(this, params);
  }

  @Override
  public PgBatch batch() {
    return new BatchImpl(this);
  }

  @Override
  public void close() {
    close(ar -> {
    });
  }

  void execute(List<Object> params,
               int fetch,
               String portal,
               boolean suspended,
               QueryResultHandler handler) {
    boolean parse;
    if (!parsed) {
      parsed = true;
      parse = true;
    } else {
      parse = false;
    }
    conn.schedule(new PreparedQueryCommand(parse, sql, params, fetch, stmt, portal, suspended, handler));
  }

  void update(List<List<Object>> paramsList, Handler<AsyncResult<List<UpdateResult>>> handler) {
    boolean parse;
    if (!parsed) {
      parsed = true;
      parse = true;
    } else {
      parse = false;
    }
    conn.schedule(new PreparedUpdateCommand(parse, sql, stmt, paramsList, handler));
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (cached) {
      completionHandler.handle(Future.succeededFuture());
    } else if (closed.compareAndSet(false, true)) {
      conn.schedule(new CloseStatementCommand(stmt, completionHandler));
    } else {
      completionHandler.handle(Future.failedFuture("Already closed"));
    }
  }

  void closePortal(String portal, Handler<AsyncResult<Void>> handler) {
    conn.schedule(new CloseStatementCommand(portal, handler));
  }
}
