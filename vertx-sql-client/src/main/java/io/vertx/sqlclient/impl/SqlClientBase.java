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
import io.vertx.sqlclient.impl.command.BiCommand;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;
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

  protected abstract <T> Promise<T> promise();

  protected abstract <T> Promise<T> promise(Handler<AsyncResult<T>> handler);

  @Override
  public C query(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    return query(sql, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise(handler));
  }

  @Override
  public Future<RowSet<Row>> query(String sql) {
    Promise<RowSet<Row>> promise = promise();
    query(sql, false, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise);
    return promise.future();
  }

  @Override
  public <R> C query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return query(sql, true, SqlResultImpl::new, collector, promise(handler));
  }

  @Override
  public <R> Future<SqlResult<R>> query(String sql, Collector<Row, ?, R> collector) {
    Promise<SqlResult<R>> promise = promise();
    query(sql, true, SqlResultImpl::new, collector, promise);
    return promise.future();
  }

  private <R1, R2 extends SqlResultBase<R1>, R3 extends SqlResult<R1>> C query(
    String sql,
    boolean singleton,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> promise) {
    SqlResultBuilder<R1, R2, R3> b = new SqlResultBuilder<>(factory, promise);
    schedule(new SimpleQueryCommand<>(sql, singleton, autoCommit(), collector, b), b);
    return (C) this;
  }

  @Override
  public C preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler) {
    return preparedQuery(sql, (TupleInternal)arguments, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise(handler));
  }

  @Override
  public Future<RowSet<Row>> preparedQuery(String sql, Tuple arguments) {
    Promise<RowSet<Row>> promise = promise();
    preparedQuery(sql, (TupleInternal)arguments, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise);
    return promise.future();
  }

  @Override
  public <R> C preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedQuery(sql, (TupleInternal)arguments, SqlResultImpl::new, collector, promise(handler));
  }

  @Override
  public <R> Future<SqlResult<R>> preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector) {
    Promise<SqlResult<R>> promise = promise();
    preparedQuery(sql, (TupleInternal)arguments, SqlResultImpl::new, collector, promise);
    return promise.future();
  }

  private <R1, R2 extends SqlResultBase<R1>, R3 extends SqlResult<R1>> C preparedQuery(
    String sql,
    TupleInternal arguments,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> promise) {
    SqlResultBuilder<R1, R2, R3> builder = new SqlResultBuilder<>(factory, promise);
    BiCommand<PreparedStatement, Boolean> abc = new BiCommand<>(new PrepareStatementCommand(sql), ps -> {
      String msg = ps.prepare(arguments);
      if (msg != null) {
        return Future.failedFuture(msg);
      } else {
        return Future.succeededFuture(new ExtendedQueryCommand<>(ps, arguments, autoCommit(), collector, builder));
      }
    });
    schedule(abc, builder);
    return (C) this;
  }

  @Override
  public C preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  @Override
  public Future<RowSet<Row>> preparedQuery(String sql) {
    Promise<RowSet<Row>> promise = promise();
    preparedQuery(sql, ArrayTuple.EMPTY, promise);
    return promise.future();
  }

  @Override
  public <R> C preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, collector, handler);
  }

  @Override
  public <R> Future<SqlResult<R>> preparedQuery(String sql, Collector<Row, ?, R> collector) {
    Promise<SqlResult<R>> promise = promise();
    preparedQuery(sql, ArrayTuple.EMPTY, collector, promise);
    return promise.future();
  }

  @Override
  public C preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler) {
    return preparedBatch(sql, batch, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise(handler));
  }

  @Override
  public Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> batch) {
    Promise<RowSet<Row>> promise = promise();
    preparedBatch(sql, batch, RowSetImpl.FACTORY, RowSetImpl.COLLECTOR, promise);
    return promise.future();
  }

  @Override
  public <R> C preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler) {
    return preparedBatch(sql, batch, SqlResultImpl::new, collector, promise(handler));
  }

  @Override
  public <R> Future<SqlResult<R>> preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector) {
    Promise<SqlResult<R>> promise = promise();
    preparedBatch(sql, batch, SqlResultImpl::new, collector, promise);
    return promise.future();
  }

  private <R1, R2 extends SqlResultBase<R1>, R3 extends SqlResult<R1>> C preparedBatch(
    String sql,
    List<Tuple> batch,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> handler) {
    SqlResultBuilder<R1, R2, R3> builder = new SqlResultBuilder<>(factory, handler);
    BiCommand<PreparedStatement, Boolean> abc = new BiCommand<>(new PrepareStatementCommand(sql), ps -> {
      for  (Tuple args : batch) {
        String msg = ps.prepare((TupleInternal) args);
        if (msg != null) {
          return Future.failedFuture(msg);
        }
      }
      return Future.succeededFuture(new ExtendedBatchQueryCommand<>(
        ps,
        batch,
        autoCommit(),
        collector,
        builder));
    });
    schedule(abc, builder);
    return (C) this;
  }

  boolean autoCommit() {
    return true;
  }
}
