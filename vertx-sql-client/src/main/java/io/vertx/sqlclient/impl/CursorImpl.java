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
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.UUID;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CursorImpl implements Cursor {

  private final Connection conn;
  private final QueryTracer tracer;
  private final ClientMetrics metrics;
  private final PreparedStatementImpl ps;
  private final ContextInternal context;
  private final boolean autoCommit;
  private final TupleInternal params;

  private String id;
  private boolean closed;
  private QueryResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> result;

  CursorImpl(PreparedStatementImpl ps, Connection conn, QueryTracer tracer, ClientMetrics metrics, ContextInternal context, boolean autoCommit, TupleInternal params) {
    this.ps = ps;
    this.conn = conn;
    this.tracer = tracer;
    this.metrics = metrics;
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
  public void read(int count, Handler<AsyncResult<RowSet<Row>>> handler) {
    Future<RowSet<Row>> fut = read(count);
    if (handler != null) {
      fut.onComplete(handler);
    }
  }

  @Override
  public synchronized Future<RowSet<Row>> read(int count) {
    Promise<RowSet<Row>> promise = context.promise();
    ps.withPreparedStatement(params, ar -> {
      if (ar.succeeded()) {
        PreparedStatement preparedStatement = ar.result();
        QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(tracer, metrics, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
        if (id == null) {
          id = UUID.randomUUID().toString();
          this.result = builder.executeExtendedQuery(conn, preparedStatement, autoCommit, params, count, id, false, promise);
        } else if (this.result.isSuspended()) {
          this.result = builder.executeExtendedQuery(conn, preparedStatement, autoCommit, params, count, id, true, promise);
        } else {
          throw new IllegalStateException();
        }
      } else {
        promise.fail(ar.cause());
      }
    });
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
