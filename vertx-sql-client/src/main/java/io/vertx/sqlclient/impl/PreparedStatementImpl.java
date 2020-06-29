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
import io.vertx.core.Context;
import io.vertx.core.Promise;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PreparedStatement {

  static io.vertx.sqlclient.PreparedStatement create(Connection conn, Context context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    return new PreparedStatementImpl(conn, context, ps, autoCommit);
  }

  static io.vertx.sqlclient.PreparedStatement create(Connection conn, Context context, String sql, boolean autoCommit) {
    return new PreparedStatementImpl(conn, context, sql, autoCommit);
  }

  private final Connection conn;
  private final Context context;
  private final String sql;
  private Promise<io.vertx.sqlclient.impl.PreparedStatement> promise;
  private Future<io.vertx.sqlclient.impl.PreparedStatement> future;
  private final boolean autoCommit;
  private final AtomicBoolean closed = new AtomicBoolean();

  private PreparedStatementImpl(Connection conn, Context context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    this.conn = conn;
    this.context = context;
    this.sql = null;
    this.promise = null;
    this.future = Future.succeededFuture(ps);
    this.autoCommit = autoCommit;
  }

  private PreparedStatementImpl(Connection conn, Context context, String sql, boolean autoCommit) {
    this.conn = conn;
    this.context = context;
    this.sql = sql;
    this.promise = Promise.promise();
    this.autoCommit = autoCommit;
  }

  @Override
  public PreparedQuery<RowSet<Row>> query() {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedStatementQuery<>(builder);
  }

  void withPreparedStatement(Tuple args, Handler<AsyncResult<io.vertx.sqlclient.impl.PreparedStatement>> handler) {
    if (context == Vertx.currentContext()) {
      if (future == null) {
        // Lazy statement;
        Promise<io.vertx.sqlclient.impl.PreparedStatement> promise = Promise.promise();
        PrepareStatementCommand prepare = new PrepareStatementCommand(sql, true, args.types());
        conn.schedule(prepare, promise);
        future = promise.future();
      }
      future.onComplete(handler);
    } else {
      context.runOnContext(v -> withPreparedStatement(args, handler));
    }
  }

  <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> void execute(
    TupleInternal args,
    int fetch,
    String cursorId,
    boolean suspended,
    SqlResultBuilder<R1, R2, R3> resultBuilder,
    Handler<AsyncResult<R3>> handler) {
    withPreparedStatement(args, ar -> {
      if (ar.succeeded()) {
        io.vertx.sqlclient.impl.PreparedStatement ps = ar.result();
        String msg = ps.prepare(args);
        if (msg != null) {
          handler.handle(Future.failedFuture(msg));
        } else {
          SqlResultHandler<R1, R2, R3> resultHandler = resultBuilder.createHandler(handler);
          conn.schedule(resultBuilder.createExtendedQuery(ps, args, fetch, cursorId, suspended, autoCommit, resultHandler), resultHandler);
        }
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public Cursor cursor(Tuple args) {
    return cursor((TupleInternal) args);
  }

  private Cursor cursor(TupleInternal args) {
    return new CursorImpl(this, conn, autoCommit, args);
  }

  @Override
  public void close() {
    close(ar -> {
    });
  }

  private <R1, R2 extends SqlResultBase<R1, R2>, R3 extends SqlResult<R1>> void batch(
    List<Tuple> argsList,
    SqlResultBuilder<R1, R2, R3> builder,
    Handler<AsyncResult<R3>> handler) {
    if (argsList.isEmpty()) {
      handler.handle(Future.failedFuture("Empty batch"));
      return;
    }
    withPreparedStatement(argsList.get(0), ar -> {
      if (ar.succeeded()) {
        io.vertx.sqlclient.impl.PreparedStatement ps = ar.result();
        for  (Tuple args : argsList) {
          String msg = ps.prepare((TupleInternal)args);
          if (msg != null) {
            handler.handle(Future.failedFuture(msg));
            return;
          }
        }
        SqlResultHandler resultHandler = builder.createHandler(handler);
        conn.schedule(builder.createBatchCommand(ps, argsList, autoCommit, resultHandler), resultHandler);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  @Override
  public RowStream<Row> createStream(int fetch, Tuple args) {
    return new RowStreamImpl(this, fetch, args);
  }

  @Override
  public void close(Handler<AsyncResult<Void>> completionHandler) {
    if (closed.compareAndSet(false, true)) {
      if (this.promise == null) {
        CloseStatementCommand cmd = new CloseStatementCommand(future.result());
        conn.schedule(cmd, completionHandler);
      } else {
        if (future == null) {
          future = this.promise.future();
          this.promise.fail("Closed");
        }
        future.onComplete(ar -> {
          if (ar.succeeded()) {
            CloseStatementCommand cmd = new CloseStatementCommand(ar.result());
            conn.schedule(cmd, completionHandler);
          } else {
            completionHandler.handle(Future.failedFuture(ar.cause()));
          }
        });
      }
    } else {
      completionHandler.handle(Future.failedFuture("Already closed"));
    }
  }

  void closeCursor(String cursorId, Handler<AsyncResult<Void>> handler) {
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        CloseCursorCommand cmd = new CloseCursorCommand(cursorId, ar.result());
        cmd.handler = handler;
        conn.schedule(cmd);
      } else {
        handler.handle(Future.failedFuture(ar.cause()));
      }
    });
  }

  private class PreparedStatementQuery<T, R extends SqlResult<T>> extends QueryBase<T, R> implements PreparedQuery<R> {

    public PreparedStatementQuery(SqlResultBuilder<T, ?, R> builder) {
      super(builder);
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
      return new PreparedStatementQuery<>(builder);
    }

    @Override
    public <U> PreparedQuery<SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
      return (PreparedQuery<SqlResult<U>>) super.collecting(collector);
    }

    @Override
    public <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper) {
      return (PreparedQuery<RowSet<U>>) super.mapping(mapper);
    }

    @Override
    public void execute(Handler<AsyncResult<R>> handler) {
      execute(ArrayTuple.EMPTY, handler);
    }

    @Override
    public void execute(Tuple args, Handler<AsyncResult<R>> handler) {
      PreparedStatementImpl.this.execute((TupleInternal) args, 0, null, false, builder, handler);
    }

    @Override
    public void executeBatch(List<Tuple> argsList, Handler<AsyncResult<R>> handler) {
      PreparedStatementImpl.this.batch(argsList, builder, handler);
    }
  }
}
