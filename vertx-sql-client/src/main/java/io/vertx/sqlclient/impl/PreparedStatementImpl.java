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

import io.vertx.core.impl.ContextInternal;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PreparedStatement {

  static PreparedStatement create(Connection conn, ContextInternal context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    return new PreparedStatementImpl(conn, context, ps, autoCommit);
  }

  final Connection conn;
  final ContextInternal context;
  final io.vertx.sqlclient.impl.PreparedStatement ps;
  final boolean autoCommit;
  private final AtomicBoolean closed = new AtomicBoolean();

  private PreparedStatementImpl(Connection conn, ContextInternal context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    this.conn = conn;
    this.context = context;
    this.ps = ps;
    this.autoCommit = autoCommit;
  }

  @Override
  public Query<RowSet<Row>> query() {
    return PreparedQuery.create(this, autoCommit);
  }

  <R, F extends SqlResult<R>> void execute(Tuple args,
                                           int fetch,
                                           String cursorId,
                                           boolean suspended,
                                           SqlResultBuilder<R, ?, F> builder,
                                           Promise<F> p) {
    if (context == Vertx.currentContext()) {
      SqlResultHandler handler = builder.createHandler(p);
      builder.execute(
        conn,
        ps,
        autoCommit,
        args,
        fetch,
        cursorId,
        suspended,
        handler);
    } else {
      context.runOnContext(v -> execute(args, fetch, cursorId, suspended, builder, p));
    }
  }

  @Override
  public Cursor cursor(Tuple args) {
    return cursor((TupleInternal) args);
  }

  private Cursor cursor(TupleInternal args) {
    String msg = ps.prepare(args);
    if (msg != null) {
      throw new IllegalArgumentException(msg);
    }
    return new CursorImpl(this, context, args);
  }

  @Override
  public Future<Void> close() {
    if (closed.compareAndSet(false, true)) {
      Promise<Void> promise = context.promise();
      CloseStatementCommand cmd = new CloseStatementCommand(ps);
      conn.schedule(cmd, promise);
      return promise.future();
    } else {
      return context.failedFuture("Already closed");
    }
  }

  <R, F extends SqlResult<R>> void batch(List<Tuple> argsList,
                                         SqlResultBuilder<R, ?, F> builder,
                                         Promise<F> p) {
    if (context == Vertx.currentContext()) {
      SqlResultHandler handler = builder.createHandler(p);
      builder.batch(conn, ps, autoCommit, argsList, handler);
    } else {
      context.runOnContext(v -> batch(argsList, builder, p));
    }
  }

  @Override
  public RowStream<Row> createStream(int fetch, Tuple args) {
    return new RowStreamImpl(this, context, fetch, args);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    Future<Void> fut = close();
    if (completionHandler != null) {
      fut.onComplete(completionHandler);
    }
  }

  void closeCursor(String cursorId, Promise<Void> promise) {
    CloseCursorCommand cmd = new CloseCursorCommand(cursorId, ps);
    conn.schedule(cmd, promise);
  }
}
