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

  PgQuery query(String sql);

  /**
   */
  default void preparedQuery(String sql, Handler<AsyncResult<PgResult<Tuple>>> handler) {
    preparedQuery(sql, ArrayTuple.EMPTY, handler);
  }

  /**
   * @param params the list of arguments
   */
  void preparedQuery(String sql, Tuple params, Handler<AsyncResult<PgResult<Tuple>>> handler);

  void preparedBatch(String sql, List<Tuple> list, Handler<AsyncResult<PgBatchResult<Tuple>>> handler);

}
