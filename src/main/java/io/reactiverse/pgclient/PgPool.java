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

package io.reactiverse.pgclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;
import java.util.stream.Collector;

/**
 * A pool of connection.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgPool extends PgClient {

  @Override
  PgPool preparedQuery(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> PgPool preparedQuery(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  PgPool query(String sql, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> PgPool query(String sql, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  PgPool preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> PgPool preparedQuery(String sql, Tuple arguments, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  @Override
  PgPool preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgRowSet>> handler);

  @Override
  @GenIgnore
  <R> PgPool preparedBatch(String sql, List<Tuple> batch, Collector<Row, ?, R> collector, Handler<AsyncResult<PgResult<R>>> handler);

  /**
   * Get a connection from the pool.
   *
   * @param handler the handler that will get the connection result
   */
  void getConnection(Handler<AsyncResult<PgConnection>> handler);

  /**
   * Borrow a connection from the pool and begin a transaction, the underlying connection will be returned
   * to the pool when the transaction ends.
   *
   * @return the transaction
   */
  void begin(Handler<AsyncResult<PgTransaction>> handler);

  /**
   * Close the pool and release the associated resources.
   */
  void close();

}
