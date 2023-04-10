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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.PreparedQuery;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.command.CompositeCommand;
import io.vertx.sqlclient.spi.Driver;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

public abstract class SqlClientBase implements SqlClientInternal, CommandScheduler {

  protected final Driver driver;

  public SqlClientBase(Driver driver) {
    this.driver = driver;
  }

  protected abstract ContextInternal context();

  protected abstract <T> PromiseInternal<T> promise();

  protected abstract <T> PromiseInternal<T> promise(Handler<AsyncResult<T>> handler);

  @Override
  public Driver driver() {
    return driver;
  }

  @Override
  public Query<RowSet<Row>> query(String sql) {
    QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new QueryImpl<>(autoCommit(), false, sql, builder);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql) {
    return preparedQuery(sql, null);
  }

  @Override
  public PreparedQuery<RowSet<Row>> preparedQuery(String sql, PrepareOptions options) {
    QueryExecutor<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new QueryExecutor<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedQueryImpl<>(autoCommit(), false, sql, options, builder);
  }

  boolean autoCommit() {
    return true;
  }

  private class QueryImpl<T, R extends SqlResult<T>> extends QueryBase<T, R> {

    protected final boolean autoCommit;
    protected final boolean singleton;
    protected final String sql;

    private QueryImpl(boolean autoCommit, boolean singleton, String sql, QueryExecutor<T, ?, R> builder) {
      super(builder);
      this.autoCommit = autoCommit;
      this.singleton = singleton;
      this.sql = sql;
    }

    @Override
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(QueryExecutor<T2, ?, R2> builder) {
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

    protected void execute(PromiseInternal<R> promise) {
      builder.executeSimpleQuery(SqlClientBase.this, sql, autoCommit, singleton, promise);
    }
  }

  private class PreparedQueryImpl<T, R extends SqlResult<T>> extends QueryImpl<T, R> implements PreparedQuery<R> {

    private final PrepareOptions options;

    private PreparedQueryImpl(boolean autoCommit, boolean singleton, String sql, PrepareOptions options, QueryExecutor<T, ?, R> builder) {
      super(autoCommit, singleton, sql, builder);

      this.options = options;
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
    protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(QueryExecutor<T2, ?, R2> builder) {
      return new PreparedQueryImpl<>(autoCommit, singleton, sql, options, builder);
    }

    @Override
    protected void execute(PromiseInternal<R> promise) {
      execute(ArrayTuple.EMPTY, promise);
    }

    private void execute(Tuple arguments, PromiseInternal<R> promise) {
      builder.executeExtendedQuery(SqlClientBase.this, sql, options, autoCommit, arguments, promise);
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

    private void executeBatch(List<Tuple> batch, PromiseInternal<R> promise) {
      builder.executeBatchQuery(SqlClientBase.this, sql, options, autoCommit, batch, promise);
    }
  }

  @Override
  public void group(Handler<SqlClient> block) {
    GroupingClient grouping = new GroupingClient();
    block.handle(grouping);
    schedule(context(), grouping.composite);
  }

  private class GroupingClient extends SqlClientBase {

    private CompositeCommand composite = new CompositeCommand();

    public GroupingClient() {
      super(SqlClientBase.this.driver);
    }

    @Override
    public void close(Handler<AsyncResult<Void>> handler) {
      throw new UnsupportedOperationException();
    }

    @Override
    public Future<Void> close() {
      throw new UnsupportedOperationException();
    }

    @Override
    protected ContextInternal context() {
      return SqlClientBase.this.context();
    }

    @Override
    protected <T> PromiseInternal<T> promise() {
      return SqlClientBase.this.promise();
    }

    @Override
    protected <T> PromiseInternal<T> promise(Handler<AsyncResult<T>> handler) {
      return SqlClientBase.this.promise(handler);
    }

    @Override
    public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
      return composite.add(context, cmd);
    }
  }
}
