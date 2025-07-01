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

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.internal.ArrayTuple;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.protocol.CloseCursorCommand;
import io.vertx.sqlclient.spi.protocol.CloseStatementCommand;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PreparedStatement;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.RowStream;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.*;
import io.vertx.sqlclient.spi.protocol.PrepareStatementCommand;
import io.vertx.sqlclient.internal.TupleBase;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class PreparedStatementBase implements PreparedStatement {

  public static PreparedStatement create(Connection conn,
                                         ContextInternal context,
                                         io.vertx.sqlclient.internal.PreparedStatement preparedStatement,
                                         boolean autoCommit) {
    return new PreparedStatementBase(conn, context, autoCommit) {
      @Override
      protected <R, F extends SqlResult<R>> void executeBatch(List<Tuple> argsList, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p) {
        builder.executeBatchQuery(conn, null, preparedStatement, autoCommit, argsList, p);
      }
      @Override
      protected <R, F extends SqlResult<R>> void execute(Tuple args, int fetch, String cursorId, boolean suspended, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p) {
        builder.executeExtendedQuery(conn, preparedStatement, null, autoCommit, args, fetch, cursorId, suspended,  p);
      }
      @Override
      protected void close(Promise<Void> promise) {
        conn.schedule(new CloseStatementCommand(preparedStatement), promise);
      }
      @Override
      protected void closeCursor(String cursorId, Promise<Void> promise) {
        CloseCursorCommand cmd = new CloseCursorCommand(cursorId, preparedStatement);
        conn.schedule(cmd, promise);
      }
      @Override
      protected void readCursor(CursorImpl cursor, String id, boolean suspended, TupleBase params, int count, PromiseInternal<RowSet<Row>> promise) {
        QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
        cursor.result = builder.executeExtendedQuery(conn, preparedStatement, null, autoCommit, params, count, id, suspended, promise);
      }
    };
  }

  public static PreparedStatement create(Connection conn, ContextInternal context, PrepareOptions options, String sql, boolean autoCommit) {
    return new PreparedStatementBase(conn, context, autoCommit) {
      Future<io.vertx.sqlclient.internal.PreparedStatement> future;
      void withPreparedStatement(PrepareOptions options, Tuple args, Handler<AsyncResult<io.vertx.sqlclient.internal.PreparedStatement>> handler) {
        if (context.inThread()) {
          if (future == null) {
            Promise<io.vertx.sqlclient.internal.PreparedStatement> promise = context.promise();
            PrepareStatementCommand prepare = new PrepareStatementCommand(sql, options, true, args.types());
            conn.schedule(prepare, promise);
            future = promise.future();
          }
          future.onComplete(handler);
        } else {
          context.runOnContext(v -> withPreparedStatement(options, args, handler));
        }
      }
      @Override
      protected <R, F extends SqlResult<R>> void executeBatch(List<Tuple> argsList, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p) {
        withPreparedStatement(options, argsList.get(0), ar -> {
          if (ar.succeeded()) {
            builder.executeBatchQuery(conn, options, ar.result(), autoCommit, argsList, p);
          } else {
            p.fail(ar.cause());
          }
        });
      }
      @Override
      protected <R, F extends SqlResult<R>> void execute(Tuple args, int fetch, String cursorId, boolean suspended, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p) {
        withPreparedStatement(options, args, ar -> {
          if (ar.succeeded()) {
            builder.executeExtendedQuery(conn, ar.result(), options, autoCommit, args, fetch, cursorId, suspended,  p);
          } else {
            p.fail(ar.cause());
          }
        });
      }
      @Override
      protected void readCursor(CursorImpl cursor, String id, boolean suspended, TupleBase params, int count, PromiseInternal<RowSet<Row>> promise) {
        withPreparedStatement(options, params, ar -> {
          if (ar.succeeded()) {
            QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
            cursor.result = builder.executeExtendedQuery(conn, ar.result(), options, autoCommit, params, count, id, suspended, promise);
          } else {
            promise.fail(ar.cause());
          }
        });
      }
      @Override
      protected void close(Promise<Void> promise) {
        if (future != null) {
          future.onComplete(ar -> {
            if (ar.succeeded()) {
              CloseStatementCommand cmd = new CloseStatementCommand(ar.result());
              conn.schedule(cmd, promise);
            } else {
              promise.fail(ar.cause());
            }
          });
        }
      }
      @Override
      protected void closeCursor(String cursorId, Promise<Void> promise) {
        if (future != null) {
          future.onComplete(ar -> {
            if (ar.succeeded()) {
              CloseCursorCommand cmd = new CloseCursorCommand(cursorId, ar.result());
              conn.schedule(cmd, promise);
            } else {
              promise.fail(ar.cause());
            }
          });
        } else {
          promise.fail("Invalid");
        }
      }
    };
  }

  private final Connection conn;
  private final ContextInternal context;
  private final boolean autoCommit;
  private final AtomicBoolean closed;

  private PreparedStatementBase(Connection conn, ContextInternal context, boolean autoCommit) {
    this.conn = conn;
    this.context = context;
    this.autoCommit = autoCommit;
    this.closed = new AtomicBoolean();
  }

  protected abstract <R, F extends SqlResult<R>> void execute(Tuple args, int fetch, String cursorId, boolean suspended, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p);
  protected abstract <R, F extends SqlResult<R>> void executeBatch(List<Tuple> argsList, QueryExecutor<R, ?, F> builder, PromiseInternal<F> p);
  protected abstract void close(Promise<Void> promise);
  protected abstract void closeCursor(String cursorId, Promise<Void> promise);
  protected abstract void readCursor(CursorImpl cursor, String id, boolean suspended, TupleBase params, int count, PromiseInternal<RowSet<Row>> promise);

  @Override
  public final PreparedQuery<RowSet<Row>> query() {
    return new PreparedStatementQuery<>(new QueryExecutor<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR));
  }

  @Override
  public final Cursor cursor(Tuple args) {
    return new CursorImpl(this, conn, context, autoCommit, (TupleBase) args);
  }

  @Override
  public final Future<Void> close() {
    if (closed.compareAndSet(false, true)) {
      Promise<Void> promise = context.promise();
      close(promise);
      return promise.future();
    } else {
      return context.failedFuture("Already closed");
    }
  }

  @Override
  public final RowStream<Row> createStream(int fetch, Tuple args) {
    return new RowStreamImpl(this, context, fetch, args);
  }

  private class PreparedStatementQuery<T, R extends SqlResult<T>> extends QueryBase<T, R> implements PreparedQuery<R> {

    public PreparedStatementQuery(QueryExecutor<T, ?, R> builder) {
      super(builder);
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(QueryExecutor<T2, ?, R2> builder) {
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
    public Future<R> execute() {
      return execute(ArrayTuple.EMPTY);
    }

    @Override
    public Future<R> execute(Tuple args) {
      PromiseInternal<R> promise = context.promise();
      PreparedStatementBase.this.execute(args, 0, null, false, builder, promise);
      return promise.future();
    }

    @Override
    public Future<R> executeBatch(List<Tuple> argsList) {
      if (argsList.isEmpty()) {
        return context.failedFuture("Empty batch");
      } else {
        PromiseInternal<R> promise = context.promise();
        PreparedStatementBase.this.executeBatch(argsList, builder, promise);
        return promise.future();
      }
    }
  }
}
