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
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.sqlclient.PreparedQuery;
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
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.tracing.QueryTracer;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedStatementImpl implements PreparedStatement {

  static PreparedStatement create(Connection conn, QueryTracer tracer, ClientMetrics metrics, ContextInternal context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    return new PreparedStatementImpl(conn, tracer, metrics, context, ps, autoCommit);
  }

  static PreparedStatement create(Connection conn, QueryTracer tracer, ClientMetrics metrics, ContextInternal context, String sql, boolean autoCommit) {
    return new PreparedStatementImpl(conn, tracer, metrics, context, sql, autoCommit);
  }

  private final Connection conn;
  private final QueryTracer tracer;
  private final ClientMetrics metrics;
  private final ContextInternal context;
  private final String sql;
  private Promise<io.vertx.sqlclient.impl.PreparedStatement> promise;
  private Future<io.vertx.sqlclient.impl.PreparedStatement> future;
  private final boolean autoCommit;
  private final AtomicBoolean closed = new AtomicBoolean();

  private PreparedStatementImpl(Connection conn, QueryTracer tracer, ClientMetrics metrics, ContextInternal context, io.vertx.sqlclient.impl.PreparedStatement ps, boolean autoCommit) {
    this.conn = conn;
    this.tracer = tracer;
    this.metrics = metrics;
    this.context = context;
    this.sql = null;
    this.promise = null;
    this.future = Future.succeededFuture(ps);
    this.autoCommit = autoCommit;
  }

  private PreparedStatementImpl(Connection conn,
                                QueryTracer tracer,
                                ClientMetrics metrics,
                                ContextInternal context,
                                String sql,
                                boolean autoCommit) {
    this.conn = conn;
    this.tracer = tracer;
    this.metrics = metrics;
    this.context = context;
    this.sql = sql;
    this.promise = Promise.promise();
    this.autoCommit = autoCommit;
  }

  @Override
  public PreparedQuery<RowSet<Row>> query() {
    QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(
      tracer,
      metrics,
      RowSetImpl.FACTORY,
      RowSetImpl.COLLECTOR);
    return new PreparedStatementQuery<>(builder);
  }

  void withPreparedStatement(Tuple args, Handler<AsyncResult<io.vertx.sqlclient.impl.PreparedStatement>> handler) {
    if (context == Vertx.currentContext()) {
      if (future == null) {
        // Lazy statement;
        PrepareStatementCommand prepare = new PrepareStatementCommand(sql, true, args.types());
        conn.schedule(context, prepare).onComplete(promise);
        future = promise.future();
      }
      future.onComplete(handler);
    } else {
      context.runOnContext(v -> withPreparedStatement(args, handler));
    }
  }

  <R, F extends SqlResult<R>> void execute(Tuple args,
                                           int fetch,
                                           String cursorId,
                                           boolean suspended,
                                           QueryExecutor<R, ?, F> builder,
                                           PromiseInternal<F> p) {
    withPreparedStatement(args, ar -> {
      if (ar.succeeded()) {
        builder.executeExtendedQuery(
          conn,
          ar.result(),
          autoCommit,
          args,
          fetch,
          cursorId,
          suspended,
          p);
      } else {
        p.fail(ar.cause());
      }
    });
  }

  <R, F extends SqlResult<R>> void executeBatch(List<Tuple> argsList,
                                                QueryExecutor<R, ?, F> builder,
                                                PromiseInternal<F> p) {
    withPreparedStatement(argsList.get(0), ar -> {
      if (ar.succeeded()) {
        builder.executeBatchQuery(conn, ar.result(), autoCommit, argsList, p);
      } else {
        p.fail(ar.cause());
      }
    });
  }

  @Override
  public Cursor cursor(Tuple args) {
    return cursor((TupleInternal) args);
  }

  private Cursor cursor(TupleInternal args) {
    return new CursorImpl(this, conn, tracer, metrics, context, autoCommit, args);
  }

  @Override
  public Future<Void> close() {
    if (closed.compareAndSet(false, true)) {
      Promise<Void> promise = context.promise();
      if (this.promise == null) {
        CloseStatementCommand cmd = new CloseStatementCommand(future.result());
        conn.schedule(context, cmd).onComplete(promise);
      } else {
        if (future == null) {
          future = this.promise.future();
          this.promise.fail("Closed");
        }
        future.onComplete(ar -> {
          if (ar.succeeded()) {
            CloseStatementCommand cmd = new CloseStatementCommand(ar.result());
            conn.schedule(context, cmd).onComplete(promise);
          } else {
            promise.complete();
          }
        });
      }
      return promise.future();
    } else {
      return context.failedFuture("Already closed");
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
    future.onComplete(ar -> {
      if (ar.succeeded()) {
        CloseCursorCommand cmd = new CloseCursorCommand(cursorId, ar.result());
        conn.schedule(context, cmd).onComplete(promise);
      } else {
        promise.fail(ar.cause());
      }
    });
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
    public void execute(Handler<AsyncResult<R>> handler) {
      execute(ArrayTuple.EMPTY, handler);
    }

    @Override
    public Future<R> execute() {
      return execute(ArrayTuple.EMPTY);
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

    private void execute(Tuple args, PromiseInternal<R> promise) {
      PreparedStatementImpl.this.execute(args, 0, null, false, builder, promise);
    }

    public void executeBatch(List<Tuple> argsList, Handler<AsyncResult<R>> handler) {
      executeBatch(argsList, context.promise(handler));
    }

    @Override
    public Future<R> executeBatch(List<Tuple> argsList) {
      Promise<R> promise = context.promise();
      executeBatch(argsList, promise);
      return promise.future();
    }

    private void executeBatch(List<Tuple> argsList, PromiseInternal<R> promise) {
      if (argsList.isEmpty()) {
        promise.fail("Empty batch");
      } else {
        PreparedStatementImpl.this.executeBatch(argsList, builder, promise);
      }
    }
  }
}
