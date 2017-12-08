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

package com.julienviet.pgclient;

import com.julienviet.pgclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * Defines the operations with a Postgres DB.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgOperations {

  /**
   * Execute a simple query.
   *
   * @param sql the query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgOperations query(String sql, Handler<AsyncResult<PgResult<Tuple>>> handler);

  /**
   * Prepare and execute a query in a single operation.
   *
   * @param sql the prepared query SQL
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgOperations preparedQuery(String sql, Handler<AsyncResult<PgResult<Tuple>>> handler) {
    return preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  /**
   * Prepare and execute a query in a single operation.
   *
   * @param sql the prepared query SQL
   * @param arguments the list of arguments
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgOperations preparedQuery(String sql, Tuple arguments, Handler<AsyncResult<PgResult<Tuple>>> handler);

  /**
   * Prepare and execute a createBatch in a single operation.
   *
   * @param sql the prepared query SQL
   * @param batch the createBatch of tuples
   * @param handler the handler notified with the execution result
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgOperations preparedBatch(String sql, List<Tuple> batch, Handler<AsyncResult<PgBatchResult<Tuple>>> handler);

}
