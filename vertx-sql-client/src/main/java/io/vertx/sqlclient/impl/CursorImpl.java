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

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CursorImpl implements Cursor {

  private final PreparedStatementImpl ps;
  private final Connection conn;
  private final boolean autoCommit;
  private final TupleInternal params;

  private String id;
  private boolean closed;
  private SqlResultHandler<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> result;

  CursorImpl(PreparedStatementImpl ps, Connection conn, boolean autoCommit, TupleInternal params) {
    this.ps = ps;
    this.conn = conn;
    this.autoCommit = autoCommit;
    this.params = params;
  }

  @Override
  public synchronized boolean hasMore() {
    if (result == null) {
      throw new IllegalStateException("No current cursor read");
    }
    return result.isSuspended();
  }

  @Override
  public synchronized void read(int count, Handler<AsyncResult<RowSet<Row>>> handler) {
    ps.withPreparedStatement(params, ar -> {
      if (ar.succeeded()) {
        PreparedStatement preparedStatement = ar.result();
        SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
        SqlResultHandler<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> resultHandler = builder.createHandler(handler);
        ExtendedQueryCommand<RowSet<Row>> cmd;
        if (id == null) {
          String msg = preparedStatement.prepare(params);
          if (msg != null) {
            handler.handle(Future.failedFuture(msg));
            return;
          }
          id = UUID.randomUUID().toString();
          cmd = builder.createExtendedQuery(preparedStatement, params, count, id, false, autoCommit, resultHandler);
        } else if (result.isSuspended()) {
          cmd = builder.createExtendedQuery(preparedStatement, params, count, id, true, autoCommit, resultHandler);
        } else {
          throw new IllegalStateException();
        }
        result = resultHandler;
        conn.schedule(cmd, resultHandler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public synchronized void close(Handler<AsyncResult<Void>> completionHandler) {
    if (!closed) {
      closed = true;
      if (id == null) {
        completionHandler.handle(Future.succeededFuture());
      } else {
        String id = this.id;
        this.id = null;
        result = null;
        ps.closeCursor(id, completionHandler);
      }
    }
  }
}
