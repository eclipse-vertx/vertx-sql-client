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

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.Arrays;
import java.util.Collections;
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
  default void preparedQuery(String sql, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Collections.emptyList(), handler);
  }

  /**
   * @param param1 the first argument of the query
   */
  default void preparedQuery(String sql, Object param1, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Collections.singletonList(param1), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   */
  default void preparedQuery(String sql, Object param1, Object param2, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Arrays.asList(param1, param2), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   */
  default void preparedQuery(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Arrays.asList(param1, param2, param3), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   */
  default void preparedQuery(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   */
  default void preparedQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   * @param param6 the sixth argument of the query
   */
  default void preparedQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<PgResult>> handler) {
    preparedQuery(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  /**
   * @param params the list of arguments
   */
  @GenIgnore
  void preparedQuery(String sql, List<Object> params, Handler<AsyncResult<PgResult>> handler);

  /**
   */
  void update(String sql, Handler<AsyncResult<PgResult>> handler);

  /**
   * @param param1 the first argument of the update query
   */
  default void preparedUpdate(String sql, Object param1, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Collections.singletonList(param1), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   */
  default void preparedUpdate(String sql, Object param1, Object param2, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Arrays.asList(param1, param2), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   */
  default void preparedUpdate(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Arrays.asList(param1, param2, param3), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   * @param param4 the fourth argument of the update query
   */
  default void preparedUpdate(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   * @param param4 the fourth argument of the update query
   * @param param5 the fifth argument of the update query
   */
  default void preparedUpdate(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   * @param param4 the fourth argument of the update query
   * @param param5 the fifth argument of the update query
   * @param param6 the sixth argument of the update query
   * create a query from this statement with six arguments
   */
  default void preparedUpdate(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<PgResult>> handler) {
    preparedUpdate(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  /**
   * @param params the list of arguments
   */
  @GenIgnore
  void preparedUpdate(String sql, List<Object> params, Handler<AsyncResult<PgResult>> handler);

  @GenIgnore
  void preparedBatchUpdate(String sql, List<List<Object>> list, Handler<AsyncResult<PgResult>> handler);

}
