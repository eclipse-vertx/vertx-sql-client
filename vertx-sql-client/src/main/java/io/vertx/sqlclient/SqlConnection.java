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
 * A connection to the database server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@VertxGen
public interface SqlConnection extends SqlClient {

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

//  @Fluent
//  @Override
//  SqlConnection preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);
//
//  @Override
//  Future<RowSet<Row>> preparedQuery(String sql);
//
//  @Fluent
//  @GenIgnore
//  @Override
//  <R> SqlConnection preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
//
//  @GenIgnore
//  @Override
//  <R> Future<SqlResult<R>> preparedQuery(String sql, Collector<Row, ?, R> collector);
//
//  @Fluent
//  @Override
//  SqlConnection query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);
//
//  @Override
//  Future<RowSet<Row>> query(String sql);
//
//  @Fluent
//  @GenIgnore
//  @Override
//  <R> SqlConnection query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
//
//  @GenIgnore
//  @Override
//  <R> Future<SqlResult<R>> query(String sql, Collector<Row, ?, R> collector);
//
//  @Fluent
//  @Override
//  SqlConnection preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);
//
//  @Override
//  Future<RowSet<Row>> preparedQuery(String sql, Tuple arguments);
//
//  @GenIgnore
//  @Override
//  <R> SqlConnection preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
//
//  @GenIgnore
//  @Override
//  <R> Future<SqlResult<R>> preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector);
//
//  @Fluent
//  @Override
//  SqlConnection preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);
//
//  @Override
//  Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> batch);
//
//  @Fluent
//  @GenIgnore
//  @Override
//  <R> SqlConnection preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<SqlResult<R>>> handler);
//
//  @GenIgnore
//  @Override
//  <R> Future<SqlResult<R>> preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector);
}
