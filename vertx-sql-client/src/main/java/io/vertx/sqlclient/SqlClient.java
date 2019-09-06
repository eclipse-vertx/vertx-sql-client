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
 * Defines the client operations with a database server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface SqlClient {

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Execute a simple query.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@code mapping} function.
   *
   * @param sql the query SQL
   * @param mapping the mapping function
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   * @param <R> the POJO type mapped by the {@code mapping} function
   */
  @Fluent
  <R> SqlClient query(String sql, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Execute a simple query.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@link JsonObject#mapTo(Class)} with a {@link JsonObject}
   * created by the row values keyed by the row names.
   *
   * @param sql the query SQL
   * @param type the mapping type
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   * @param <R> the POJO type mapped by {@link JsonObject#mapTo(Class)}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  @Fluent
  <R> SqlClient query(String sql, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Prepare and execute a query.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@code mapping} function.
   *
   * @param sql the prepared query SQL
   * @param mapping the mapping function
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  <R> SqlClient preparedQuery(String sql, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare and execute a query.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@link JsonObject#mapTo(Class)} with a {@link JsonObject}
   * created by the row values keyed by the row names.
   *
   * @param sql the prepared query SQL
   * @param type the mapping type
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <R> SqlClient preparedQuery(String sql, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Prepare, execute a query and provide a POJO mapped row result.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@code mapping} function.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param mapping the mapping function
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  <R> SqlClient preparedQuery(String sql, Tuple arguments, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare, execute a query and provide a POJO mapped row result.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@link JsonObject#mapTo(Class)} with a {@link JsonObject}
   * created by the row values keyed by the row names.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param type the mapping type
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <R> SqlClient preparedQuery(String sql, Tuple arguments, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare and execute a query.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Prepare and execute a batch.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@code mapping} function.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param mapping the mapping function
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  <R> SqlClient preparedBatch(String sql, List<Tuple> batch, Function<JsonObject, R> mapping, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare and execute a batch.
   * <br/>
   * The row set is mapped to the POJO {@code <R>}.
   * <br/>
   * The POJO is mapped by the {@link JsonObject#mapTo(Class)} with a {@link JsonObject}
   * created by the row values keyed by the row names.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param type the mapping type
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  <R> SqlClient preparedBatch(String sql, List<Tuple> batch, Class<R> type, Handler<AsyncResult<RowSet<R>>> handler);

  /**
   * Prepare and execute a createBatch.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Close the client and release the associated resources.
   */
  void close();

}
