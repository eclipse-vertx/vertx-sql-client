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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonObject;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collector;

/**
 * A connection to database server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@VertxGen
public interface SqlConnection extends SqlClient {

  /**
   * Create a prepared query.
   *
   * @param sql the sql
   * @param handler the handler notified with the prepared query asynchronously
   */
  @Fluent
  SqlConnection prepare(String sql, Handler<AsyncResult<PreparedQuery>> handler);

  /**
   * Set an handler called with connection errors.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlConnection exceptionHandler(Handler<Throwable> handler);

  /**
   * Set an handler called when the connection is closed.
   *
   * @param handler the handler
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlConnection closeHandler(Handler<Void> handler);

  /**
   * Begin a transaction and returns a {@link Transaction} for controlling and tracking
   * this transaction.
   * <p/>
   * When the connection is explicitely closed, any inflight transaction is rollbacked.
   *
   * @return the transaction instance
   */
  Transaction begin();

  /**
   * @return whether the connection uses SSL
   */
  boolean isSSL();

  /**
   * Close the current connection after all the pending commands have been processed.
   */
  void close();

  @Override
  SqlConnection preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @Override
  <R> SqlConnection preparedQuery(String sql, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Override
  <R> SqlConnection preparedQuery(String sql, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  @Override
  @GenIgnore
  <R> SqlConnection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  SqlConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  @Override
  <R> SqlConnection query(String sql, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Override
  <R> SqlConnection query(String sql, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  @Override
  @GenIgnore
  <R> SqlConnection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  SqlConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  @Override
  <R> SqlConnection preparedQuery(String sql, Tuple arguments, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Override
  <R> SqlConnection preparedQuery(String sql, Tuple arguments, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  @Override
  @GenIgnore
  <R> SqlConnection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  @Override
  SqlConnection preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  @Override
  <R> SqlConnection preparedBatch(String sql, List<Tuple> batch, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Override
  <R> SqlConnection preparedBatch(String sql, List<Tuple> batch, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  @Override
  @GenIgnore
  <R> SqlConnection preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
}
