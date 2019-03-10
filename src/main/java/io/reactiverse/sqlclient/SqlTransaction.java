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
package io.reactiverse.sqlclient;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgPreparedQuery;
import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

/**
 * A transaction that allows to control the transaction and receive events.
 */
@VertxGen
public interface SqlTransaction extends SqlClient {

  /**
   * Create a prepared query.
   *
   * @param sql the sql
   * @param handler the handler notified with the prepared query asynchronously
   */
  @Fluent
  SqlTransaction prepare(String sql, Handler<AsyncResult<PgPreparedQuery>> handler);

  /**
   * Commit the current transaction.
   */
  void commit();

  /**
   * Like {@link #commit} with an handler to be notified when the transaction commit has completed
   */
  void commit(Handler<AsyncResult<Void>> handler);

  /**
   * Rollback the current transaction.
   */
  void rollback();

  /**
   * Like {@link #rollback} with an handler to be notified when the transaction rollback has completed
   */
  void rollback(Handler<AsyncResult<Void>> handler);

  /**
   * Set an handler to be called when the transaction is aborted.
   *
   * @param handler the handler
   */
  @Fluent
  SqlTransaction abortHandler(Handler<Void> handler);

  @Override
  SqlTransaction query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> SqlTransaction query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  SqlTransaction preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> SqlTransaction preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  SqlTransaction preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> SqlTransaction preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  SqlTransaction preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> SqlTransaction preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);
}
