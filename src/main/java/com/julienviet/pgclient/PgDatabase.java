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
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.UpdateResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgDatabase {

  /**
   */
  void query(String sql, Handler<AsyncResult<ResultSet>> handler);

  /**
   * @param param1 the first argument of the query
   */
  default void query(String sql, Object param1, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Collections.singletonList(param1), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   */
  default void query(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Arrays.asList(param1, param2), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   */
  default void query(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Arrays.asList(param1, param2, param3), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   */
  default void query(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   */
  default void query(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  /**
   * @param param1 the first argument of the query
   * @param param2 the second argument of the query
   * @param param3 the third argument of the query
   * @param param4 the fourth argument of the query
   * @param param5 the fifth argument of the query
   * @param param6 the sixth argument of the query
   */
  default void query(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<ResultSet>> handler) {
    query(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  /**
   * @param params the list of arguments
   */
  @GenIgnore
  void query(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler);

  /**
   */
  void update(String sql, Handler<AsyncResult<UpdateResult>> handler);

  /**
   * @param param1 the first argument of the update query
   */
  default void update(String sql, Object param1, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Collections.singletonList(param1), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   */
  default void update(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Arrays.asList(param1, param2), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   */
  default void update(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Arrays.asList(param1, param2, param3), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   * @param param4 the fourth argument of the update query
   */
  default void update(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  /**
   * @param param1 the first argument of the update query
   * @param param2 the second argument of the update query
   * @param param3 the third argument of the update query
   * @param param4 the fourth argument of the update query
   * @param param5 the fifth argument of the update query
   */
  default void update(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
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
  default void update(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
    update(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  /**
   * @param params the list of arguments
   */
  @GenIgnore
  void update(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler);

}
