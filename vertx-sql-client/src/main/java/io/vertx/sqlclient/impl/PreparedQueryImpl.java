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
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.*;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedQueryImpl<T, R extends SqlResult<T>> implements PreparedQuery<R> {

  static PreparedQuery<RowSet<Row>> create(Connection conn, ContextInternal context, PreparedStatement ps, boolean autoCommit) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedQueryImpl<>(conn, context, ps, autoCommit, builder);
  }

  final Connection conn;
  private final ContextInternal context;
  final PreparedStatement ps;
  final boolean autoCommit;
  private final AtomicBoolean closed = new AtomicBoolean();
  private SqlResultBuilder<T, ?, R> builder;

  private PreparedQueryImpl(Connection conn, ContextInternal context, PreparedStatement ps, boolean autoCommit, SqlResultBuilder<T, ?, R> builder) {
    this.conn = conn;
    this.context = context;
    this.ps = ps;
    this.autoCommit = autoCommit;
    this.builder = builder;
  }

  @Override
  public <R2> PreparedQuery<SqlResult<R2>> collecting(Collector<Row, ?, R2> collector) {
    SqlResultBuilder<R2, SqlResultImpl<R2>, SqlResult<R2>> builder = new SqlResultBuilder<>(SqlResultImpl::new, collector);
    return new PreparedQueryImpl<>(conn, context, ps, autoCommit, builder);
  }

  @Override
  public <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper) {
    SqlResultBuilder<RowSet<U>, RowSetImpl<U>, RowSet<U>> builder = new SqlResultBuilder<>(RowSetImpl.factory(), RowSetImpl.collector(mapper));
    return new PreparedQueryImpl<>(conn, context, ps, autoCommit, builder);
  }

  @Override
  public void execute(Tuple args, Handler<AsyncResult<R>> handler) {
    execute(args, context.promise(handler));
  }

  @Override
  public Future<R> execute(Tuple args) {
    Promise<R> promise = context.promise();
    execute(args, promise);
    return promise.future();
  }

  private void execute(Tuple args, Promise<R> promise) {
    execute(args, 0, null, false, builder, promise);
  }

  <R, F extends SqlResult<R>> void execute(Tuple args,
                                           int fetch,
                                           String cursorId,
                                           boolean suspended,
                                           SqlResultBuilder<R, ?, F> builder,
                                           Promise<F> p) {
    if (context == Vertx.currentContext()) {
      builder.execute(
        conn,
        ps,
        autoCommit,
        args,
        fetch,
        cursorId,
        suspended,
        p);
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

  public void batch(List<Tuple> argsList, Handler<AsyncResult<R>> handler) {
    batch(argsList, context.promise(handler));
  }

  @Override
  public Future<R> batch(List<Tuple> argsList) {
    Promise<R> promise = context.promise();
    batch(argsList, promise);
    return promise.future();
  }

  private void batch(List<Tuple> argsList, Promise<R> promise) {
    batch(argsList, builder, promise);
  }

  <R, F extends SqlResult<R>> void batch(List<Tuple> argsList,
                                         SqlResultBuilder<R, ?, F> builder,
                                         Promise<F> p) {
    if (context == Vertx.currentContext()) {
      builder.batch(conn, ps, autoCommit, argsList, p);
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
