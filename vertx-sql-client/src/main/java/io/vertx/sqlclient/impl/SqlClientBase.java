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
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.impl.command.BiCommand;
import io.vertx.sqlclient.impl.command.CommandScheduler;
import io.vertx.sqlclient.impl.command.ExtendedBatchQueryCommand;
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

  protected abstract <T> Promise<T> promise();

  protected abstract <T> Promise<T> promise(Handler<AsyncResult<T>> handler);

  @Override
  public Query<RowSet<Row>> createQuery(String sql) {
    return QueryImpl.create(this, autoCommit(), false, false, sql);
  }

  @Override
  public C query(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    createQuery(sql).execute(handler);
    return (C) this;
  }

  @Override
  public Future<RowSet<Row>> query(String sql) {
    return createQuery(sql).execute();
  }

  @Override
  public C preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler) {
    createPreparedQuery(sql).execute(arguments, handler);
    return (C) this;
  }

  @Override
  public Future<RowSet<Row>> preparedQuery(String sql, Tuple arguments) {
    return createPreparedQuery(sql).execute(arguments);
  }

  @Override
  public C preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  @Override
  public Query<RowSet<Row>> createPreparedQuery(String sql) {
    return QueryImpl.create(this, autoCommit(), false, true, sql);
  }

  @Override
  public Future<RowSet<Row>> preparedQuery(String sql) {
    return preparedQuery(sql, ArrayTuple.EMPTY);
  }

  @Override
  public C preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler) {
    createPreparedQuery(sql).batch(batch, handler);
    return (C) this;
  }

  @Override
  public Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> batch) {
    return createPreparedQuery(sql).batch(batch);
  }

  private <R1, R2 extends SqlResultBase<R1>, R3 extends SqlResult<R1>> C preparedBatch(
    String sql,
    List<Tuple> batch,
    Function<R1, R2> factory,
    Collector<Row, ?, R1> collector,
    Promise<R3> handler) {
    SqlResultHandler<R1, R2, R3> builder = new SqlResultHandler<>(factory, handler);
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
