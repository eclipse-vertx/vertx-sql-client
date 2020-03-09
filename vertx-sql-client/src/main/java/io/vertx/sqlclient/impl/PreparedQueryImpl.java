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
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
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
class PreparedQueryImpl implements PreparedQuery {

  private final Connection conn;
  private final ContextInternal context;
  private final PreparedStatement ps;
  private final AtomicBoolean closed = new AtomicBoolean();

  PreparedQueryImpl(Connection conn, ContextInternal context, PreparedStatement ps) {
    this.conn = conn;
    this.context = context;
    this.ps = ps;
  }

  @Override
  public PreparedQuery execute(Tuple args, Handler<AsyncResult<RowSet<Row>>> handler) {
    return execute((TupleInternal)args, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, context.promise(handler));
  }

  @Override
  public Future<RowSet<Row>> execute(Tuple args) {
    Promise<RowSet<Row>> promise = context.promise();
    execute((TupleInternal)args, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise);
    return promise.future();
  }

  @Override
  public <R> PreparedQuery execute(Tuple args, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return execute((TupleInternal)args, true, SqlResultImpl::new, collector, context.promise(handler));
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> PreparedQuery execute(
    TupleInternal args,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> handler) {
    SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, handler);
    return execute(args, 0, null, false, collector, b, b);
  }

  <A, R> PreparedQuery execute(TupleInternal args,
                               int fetch,
                               String cursorId,
                               boolean suspended,
                               Collector<Row, A, R> collector,
                               QueryResultHandler<R> resultHandler,
                               Promise<Boolean> handler) {
    if (context == Vertx.currentContext()) {
      String msg = ps.prepare(args);
      if (msg != null) {
        handler.handle(Future.failedFuture(msg));
      } else {
        ExtendedQueryCommand<R> cmd = new ExtendedQueryCommand<>(
          ps,
          args,
          fetch,
          cursorId,
          suspended,
          collector,
          resultHandler);
        conn.schedule(cmd, handler);
      }
    } else {
      context.runOnContext(v -> execute(args, fetch, cursorId, suspended, collector, resultHandler, handler));
    }
    return this;
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

  public PreparedQuery batch(List<Tuple> argsList, Handler<AsyncResult<RowSet<Row>>> handler) {
    Future<RowSet<Row>> fut = batch(argsList);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public Future<RowSet<Row>> batch(List<Tuple> argsList) {
    Promise<RowSet<Row>> promise = context.promise();
    batch(argsList, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise);
    return promise.future();
  }

  @Override
  public <R> PreparedQuery batch(List<Tuple> argsList, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    Future<SqlResult<R>> fut = batch(argsList, collector);
    if (handler != null) {
      fut.onComplete(handler);
    }
    return this;
  }

  @Override
  public <R> Future<SqlResult<R>> batch(List<Tuple> argsList, Collector<Row, ?, R> collector) {
    Promise<SqlResult<R>> promise = context.promise();
    batch(argsList, SqlResultImpl::new, collector, promise);
    return promise.future();
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> PreparedQuery batch(
    List<Tuple> argsList,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> handler) {
    for  (Tuple args : argsList) {
      String msg = ps.prepare((TupleInternal)args);
      if (msg != null) {
        handler.handle(Future.failedFuture(msg));
        return this;
      }
    }
    Promise<R3> p = context.promise(handler);
    SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, p);
    ExtendedBatchQueryCommand<R1> cmd = new ExtendedBatchQueryCommand<>(ps, argsList, collector, b);
    conn.schedule(cmd, context.promise(b));
    return this;
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
