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

import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public abstract class SqlClientBase<C extends SqlClient> implements SqlClient, CommandScheduler {

  @Override
  public Query<RowSet<Row>> query(String sql) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new QueryImpl<>( false, sql, builder);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedQueryImpl<>( false, sql, builder);
  }

  private class QueryImpl<T, R extends SqlResult<T>> extends QueryBase<T, R> {

    protected final boolean singleton;
    protected final String sql;

    private QueryImpl(boolean singleton, String sql, SqlResultBuilder<T, ?, R> builder) {
      super(builder);
      this.singleton = singleton;
      this.sql = sql;
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
      return new QueryImpl<>(singleton, sql, builder);
    }

    @Override
    public void execute(Handler<AsyncResult<R>> handler) {
      SqlResultHandler resultHandler = builder.createHandler(handler);
      schedule(builder.createSimpleQuery(sql, singleton, resultHandler), resultHandler);
    }
  }

  private class PreparedQueryImpl<T, R extends SqlResult<T>> extends QueryImpl<T, R> implements PreparedQuery<R> {

    private PreparedQueryImpl(boolean singleton, String sql, SqlResultBuilder<T, ?, R> builder) {
      super(singleton, sql, builder);
    }

    @Override
    public <U> PreparedQuery<SqlResult<U>> collecting(Collector<Row, ?, U> collector) {
      return (PreparedQuery<SqlResult<U>>) super.collecting(collector);
    }

    @Override
    public void execute(Handler<AsyncResult<R>> handler) {
      execute(ArrayTuple.EMPTY, handler);
    }

    @Override
    public <U> PreparedQuery<RowSet<U>> mapping(Function<Row, U> mapper) {
      return (PreparedQuery<RowSet<U>>) super.mapping(mapper);
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
      return new PreparedQueryImpl<>(singleton, sql, builder);
    }

    @Override
    public void execute(Tuple arguments, Handler<AsyncResult<R>> handler) {
      SqlResultHandler resultHandler = builder.createHandler(handler);
      schedule(new PrepareStatementCommand(sql, true), cr -> {
        if (cr.succeeded()) {
          PreparedStatement ps = cr.result();
          String msg = ps.prepare((TupleInternal) arguments);
          if (msg != null) {
            handler.handle(Future.failedFuture(msg));
          } else {
            schedule(builder.createExtendedQuery(ps, arguments, resultHandler), resultHandler);
          }
        } else {
          handler.handle(Future.failedFuture(cr.cause()));
        }
      });
    }

    @Override
    public void executeBatch(List<Tuple> batch, Handler<AsyncResult<R>> handler) {
      SqlResultHandler resultHandler = builder.createHandler(handler);
      schedule(new PrepareStatementCommand(sql, true), cr -> {
        if (cr.succeeded()) {
          PreparedStatement ps = cr.result();
          for  (Tuple args : batch) {
            String msg = ps.prepare((TupleInternal) args);
            if (msg != null) {
              handler.handle(Future.failedFuture(msg));
              return;
            }
          }
          schedule(builder.createBatchCommand(ps, batch, resultHandler), resultHandler);
        } else {
          handler.handle(Future.failedFuture(cr.cause()));
        }
      });
    }
  }
}
