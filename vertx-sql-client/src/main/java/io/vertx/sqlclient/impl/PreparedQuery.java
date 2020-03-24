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
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.sqlclient.Query;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class PreparedQuery<T, R extends SqlResult<T>> extends QueryBase<T, R> {

  static Query<RowSet<Row>> create(PreparedStatementImpl client, boolean autoCommit) {
    SqlResultBuilder<RowSet<Row>, RowSetImpl<Row>, RowSet<Row>> builder = new SqlResultBuilder<>(RowSetImpl.FACTORY, RowSetImpl.COLLECTOR);
    return new PreparedQuery<>(client, autoCommit, builder);
  }

  private final PreparedStatementImpl preparedStatement;

  public PreparedQuery(PreparedStatementImpl client, boolean autoCommit, SqlResultBuilder<T, ?, R> builder) {
    super(autoCommit, builder);
    this.preparedStatement = client;
  }

  @Override
  protected <T2, R2 extends SqlResult<T2>> QueryBase<T2, R2> copy(SqlResultBuilder<T2, ?, R2> builder) {
    return new PreparedQuery<>(preparedStatement, autoCommit, builder);
  }

  @Override
  public void execute(Tuple args, Handler<AsyncResult<R>> handler) {
    execute(args, preparedStatement.context.promise(handler));
  }

  @Override
  public Future<R> execute(Tuple args) {
    Promise<R> promise = preparedStatement.context.promise();
    execute(args, promise);
    return promise.future();
  }

  private void execute(Tuple args, Promise<R> promise) {
    preparedStatement.execute(args, 0, null, false, builder, promise);
  }

  public void batch(List<Tuple> argsList, Handler<AsyncResult<R>> handler) {
    batch(argsList, preparedStatement.context.promise(handler));
  }

  @Override
  public Future<R> batch(List<Tuple> argsList) {
    Promise<R> promise = preparedStatement.context.promise();
    batch(argsList, promise);
    return promise.future();
  }

  private void batch(List<Tuple> argsList, Promise<R> promise) {
    preparedStatement.batch(argsList, builder, promise);
  }
}
