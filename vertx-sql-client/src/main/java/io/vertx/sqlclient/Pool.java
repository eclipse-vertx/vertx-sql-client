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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.function.Function;

/**
 * A connection pool which reuses a number of SQL connections.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface Pool extends SqlClient {

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
   * {@inheritDoc}
   *
   * A connection is borrowed from the connection pool when the query is executed and then immediately returned
   * to the pool after it completes.
   */
  @Override
  Query<RowSet<Row>> query(String sql);

  /**
   * {@inheritDoc}
   *
   * A connection is borrowed from the connection pool when the query is executed and then immediately returned
   * to the pool after it completes.
   */
  @Override
  PreparedQuery<RowSet<Row>> preparedQuery(String sql);

  /**
   * Execute the given {@code function} within a transaction.
   *
   * <p>The {@code function} is passed a client executing all operations within a transaction.
   * When the future returned by the function
   * <ul>
   *   <li>succeeds the transaction commits</li>
   *   <li>fails the transaction rollbacks</li>
   * </ul>
   *
   * <p>The {@code handler} is given a success result when the function returns a succeeded futures and the transaction commits.
   * Otherwise it is given a failure result.
   *
   * @param function the code to execute
   * @param handler the result handler
   */
  <T> void withTransaction(Function<SqlClient, Future<T>> function, Handler<AsyncResult<T>> handler);

  /**
   * Like {@link #withTransaction(Function, Handler)} but returns a {@code Future} of the asynchronous result
   */
  <T> Future<T> withTransaction(Function<SqlClient, Future<T>> function);

  /**
   * Close the pool and release the associated resources.
   */
  void close();

}
