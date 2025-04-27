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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.PreparedStatement;
import io.vertx.sqlclient.internal.TupleInternal;

import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CursorImpl implements Cursor {

  private final Connection conn;
  private final PreparedStatementBase ps;
  private final ContextInternal context;
  private final boolean autoCommit;
  private final TupleInternal params;

  private String id;
  private boolean closed;
  QueryResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> result;

  CursorImpl(PreparedStatementBase ps, Connection conn, ContextInternal context, boolean autoCommit, TupleInternal params) {
    this.ps = ps;
    this.conn = conn;
    this.context = context;
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
  public synchronized Future<RowSet<Row>> read(int count) {
    PromiseInternal<RowSet<Row>> promise = context.promise();
    boolean suspended;
    if (id == null) {
      id = UUID.randomUUID().toString();
      suspended = false;
    } else {
      suspended = true;
    }
    ps.readCursor(this, id, suspended, params, count, promise);
    return promise.future();
  }

  @Override
  public synchronized boolean isClosed() {
    return closed;
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
