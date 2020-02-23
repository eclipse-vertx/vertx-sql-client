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
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

/**
 * A common interface which defines the SQL client operations with a database server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface SqlClient {

  /**
   * Execute a simple query using the given SQL string, the asynchronous result is represented as {@link RowSet}.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #query(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RowSet<Row>> query(String sql);

  /**
   * Execute a simple query using the given SQL string, the asynchronous result is represented as a collection of elements transformed by the provided {@link java.util.stream.Collector}.
   *
   * @param sql the query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Like {@link #query(String, Collector, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore
  <R> Future<SqlResult<R>> query(String sql, Collector<Row, ?, R> collector);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement without any parameter, the asynchronous result is represented as {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedQuery(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RowSet<Row>> preparedQuery(String sql);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement without any parameter, the asynchronous result is represented as a collection of elements transformed by the provided {@link java.util.stream.Collector}.
   *
   * @param sql the prepared query SQL
   * @param collector the collector
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  <R> SqlClient preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);

  /**
   * Like {@link #preparedQuery(String, Collector, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore
  <R> Future<SqlResult<R>> preparedQuery(String sql, Collector<Row, ?, R> collector);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement with parameters set in the {@code Tuple}, the asynchronous result is represented as {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedQuery(String, Tuple, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RowSet<Row>> preparedQuery(String sql, Tuple arguments);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement with parameters set in the {@code Tuple}, the asynchronous result is represented as a collection of elements transformed by the provided {@link java.util.stream.Collector}.
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
   * Like {@link #preparedQuery(String, Tuple, Collector, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore
  <R> Future<SqlResult<R>> preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement with a batch of parameters set in the {@code List}, the asynchronous result is represented as {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  SqlClient preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedBatch(String, List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> batch);

  /**
   * Prepare a statement using the given SQL string and execute the prepared statement with a batch of parameters set in the {@code List}, the asynchronous result is represented as a collection of elements transformed by the provided {@link java.util.stream.Collector}.
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
   * Like {@link #preparedBatch(String, List, Collector, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @GenIgnore
  <R> Future<SqlResult<R>> preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector);

  /**
   * Close the client and release the associated resources.
   */
  void close();

}
