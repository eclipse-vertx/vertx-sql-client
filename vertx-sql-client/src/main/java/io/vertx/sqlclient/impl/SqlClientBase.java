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

import io.vertx.core.Promise;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.tracing.SqlTracer;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public abstract class SqlClientBase<C extends SqlClient> implements SqlClientInternal, CommandScheduler {

  protected final SqlTracer tracer;

  public SqlClientBase(SqlTracer tracer) {
    this.tracer = tracer;
  }

  @Override
  public int appendQueryPlaceholder(StringBuilder queryBuilder, int index, int current) {
    queryBuilder.append("?");
    return current;
  }

  protected abstract <T> Promise<T> promise();

  protected abstract <T> Promise<T> promise(Handler<AsyncResult<T>> handler);

  @Override
  public Query<RowSet<Row>> query(String sql) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(tracer, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new QueryImpl<>(autoCommit(), false, sql, builder);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(tracer, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedQueryImpl<>(autoCommit(), false, sql, builder);
  }

  boolean autoCommit() {
    return true;
  }

  private class QueryImpl<T, R extends SqlResult<T>> extends QueryBase<T, R> {

    protected final boolean autoCommit;
    protected final boolean singleton;
    protected final String sql;

    private QueryImpl(boolean autoCommit, boolean singleton, String sql, SqlResultBuilder<T, ?, R> builder) {
      super(builder);
      this.autoCommit = autoCommit;
      this.singleton = singleton;
      this.sql = sql;
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
      return new QueryImpl<>(autoCommit, singleton, sql, builder);
    }

    @Override
    public void execute(Handler<AsyncResult<R>> handler) {
      execute(promise(handler));
    }

    @Override
    public Future<R> execute() {
      Promise<R> promise = promise();
      execute(promise);
      return promise.future();
    }

    protected void execute(Promise<R> promise) {
      builder.executeSimpleQuery(SqlClientBase.this, sql, autoCommit, singleton, promise);
    }
  }

  private class PreparedQueryImpl<T, R extends SqlResult<T>> extends QueryImpl<T, R> implements PreparedQuery<R> {

    private PreparedQueryImpl(boolean autoCommit, boolean singleton, String sql, SqlResultBuilder<T, ?, R> builder) {
      super(autoCommit, singleton, sql, builder);
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
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
      return new PreparedQueryImpl<>(autoCommit, singleton, sql, builder);
    }

    @Override
    protected void execute(Promise<R> promise) {
      execute(ArrayTuple.EMPTY, promise);
    }

    private void execute(Tuple arguments, Promise<R> promise) {
      builder.executeExtendedQuery(SqlClientBase.this, sql, autoCommit, arguments, promise);
    }

    @Override
    public void execute(Tuple tuple, Handler<AsyncResult<R>> handler) {
      execute(tuple, promise(handler));
    }

    @Override
    public Future<R> execute(Tuple tuple) {
      Promise<R> promise = promise();
      execute(tuple, promise);
      return promise.future();
    }

    @Override
    public void executeBatch(List<Tuple> batch, Handler<AsyncResult<R>> handler) {
      executeBatch(batch, promise(handler));
    }

    @Override
    public Future<R> executeBatch(List<Tuple> batch) {
      Promise<R> promise = promise();
      executeBatch(batch, promise);
      return promise.future();
    }

    private void executeBatch(List<Tuple> batch, Promise<R> promise) {
      builder.executeBatchQuery(SqlClientBase.this, sql, autoCommit, batch, promise);
    }
  }
}
