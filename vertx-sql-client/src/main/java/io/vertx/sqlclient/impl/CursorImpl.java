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
import io.vertx.sqlclient.Tuple;

import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CursorImpl implements Cursor {

  private final PreparedQueryImpl ps;
  private final TupleInternal params;

  private String id;
  private boolean closed;
  private SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> result;

  CursorImpl(PreparedQueryImpl ps, TupleInternal params) {
    this.ps = ps;
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
    if (id == null) {
      id = UUID.randomUUID().toString();
      result = new SqlResultBuilder<>(RowSetImpl.FACTORY, handler);
      ps.execute(params, count, id, false, RowSetImpl.COLLECTOR, result, result);
    } else if (result.isSuspended()) {
      result = new SqlResultBuilder<>(RowSetImpl.FACTORY, handler);
      ps.execute(params, count, id, true, RowSetImpl.COLLECTOR, result, result);
    } else {
      throw new IllegalStateException();
    }
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
