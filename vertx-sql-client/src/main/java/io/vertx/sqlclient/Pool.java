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
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.List;

/**
 * A connection pool which reuses a number of SQL connections.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Pool extends SqlClient {

  /**
   * Borrows a connection from the connection pool, the connection will be used to execute a simple query using the given {@code sql} string,
   * the connection will be returned to the pool when the execution completes and the asynchronous result is represented as a {@link RowSet}.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @Override
  Pool query(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #query(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @Override
  Future<RowSet<Row>> query(String sql);

  /**
   * Borrows a connection from the connection pool, the connection will be used to execute the given {@code sql} string using a prepared statement without any parameter,
   * the connection will be returned to the pool when the execution completes and the asynchronous result is represented as a {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @Override
  Pool preparedQuery(String sql, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedQuery(String, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @Override
  Future<RowSet<Row>> preparedQuery(String sql);

  /**
   * Borrows a connection from the connection pool, the connection will be used to execute the given {@code sql} string using a prepared statement with parameters set in the {@code Tuple},
   * the connection will be returned to the pool when the execution completes and the asynchronous result is represented as a {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @Override
  Pool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedQuery(String, Tuple, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @Override
  Future<RowSet<Row>> preparedQuery(String sql, Tuple arguments);

  /**
   * Borrows a connection from the connection pool, the connection will be used to execute the given {@code sql} string using a prepared statement with a batch of parameters set in the {@code List},
   * the connection will be returned to the pool when the execution completes and the asynchronous result is represented as a {@link RowSet}.
   *
   * @param sql the prepared query SQL
   * @param batch the batch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  @Override
  Pool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<RowSet<Row>>> handler);

  /**
   * Like {@link #preparedBatch(String, List, Handler)} but returns a {@code Future} of the asynchronous result
   */
  @Override
  Future<RowSet<Row>> preparedBatch(String sql, List<Tuple> batch);

  /**
   * Get a connection from the pool.
   *
   * @param handler the handler that will get the connection result
   */
  void getConnection(Handler<AsyncResult<SqlConnection>> handler);

  /**
   * Like {@link #getConnection(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<SqlConnection> getConnection();

  /**
   * Borrow a connection from the pool and begin a transaction, the underlying connection will be returned
   * to the pool when the transaction ends.
   *
   * @return the transaction
   */
  void begin(Handler<AsyncResult<Transaction>> handler);

  /**
   * Like {@link #begin(Handler)} but returns a {@code Future} of the asynchronous result
   */
  Future<Transaction> begin();

  /**
   * Close the pool and release the associated resources.
   */
  void close();

}
