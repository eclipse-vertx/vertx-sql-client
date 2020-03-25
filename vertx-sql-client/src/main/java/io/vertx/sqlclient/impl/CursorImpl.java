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
import io.vertx.core.Promise;
import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;

import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CursorImpl implements Cursor {

  private final PreparedStatementImpl ps;
  private final ContextInternal context;
  private final TupleInternal params;

  private String id;
  private boolean closed;
  private SqlResultHandler<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> result;

  CursorImpl(PreparedStatementImpl ps, ContextInternal context, TupleInternal params) {
    this.ps = ps;
    this.context = context;
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
  public void read(int count, Handler<AsyncResult<RowSet<Row>>> handler) {
    Future<RowSet<Row>> fut = read(count);
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public synchronized Future<RowSet<Row>> read(int count) {
    Promise<RowSet<Row>> promise = context.promise();
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    SqlResultHandler<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> handler = builder.createHandler(promise);
    if (id == null) {
      id = UUID.randomUUID().toString();
      result = builder.execute(ps.conn, ps.ps, ps.autoCommit, params, count, id, false, handler);
    } else if (result.isSuspended()) {
      result = builder.execute(ps.conn, ps.ps, ps.autoCommit, params, count, id, true, handler);
    } else {
      throw new IllegalStateException();
    }
    return promise.future();
  }

  @Override
  public synchronized void close(Handler<AsyncResult<Void>> completionHandler) {
    close (context.promise(completionHandler));
  }

  @Override
  public synchronized Future<Void> close() {
    Promise<Void> promise = context.promise();
    close (promise);
    return promise.future();
  }

  private synchronized void close(Promise<Void> promise) {
    if (!closed) {
      closed = true;
      if (id == null) {
        promise.complete();
      } else {
        String id = this.id;
        this.id = null;
        result = null;
        ps.closeCursor(id, promise);
      }
    }
  }
}
