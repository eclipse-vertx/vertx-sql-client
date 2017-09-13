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

import io.vertx.codegen.annotations.Fluent;
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
 * A connection to Postgres.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
@VertxGen
public interface PgConnection {

  // for multiple sql statements
  @Fluent
  PgConnection execute(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "reading" such as SELECT probably the internal command will be ReadCommand instead of QueryCommand
  @Fluent
  PgConnection query(String sql, Handler<AsyncResult<ResultSet>> handler);

  // for "writing" such as INSERT, UPDATE and DELETE probably the internal command will be WriteCommand instead of UpdateCommand
  @Fluent
  PgConnection update(String sql, Handler<AsyncResult<UpdateResult>> handler);

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Collections.singletonList(param), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4,
                       Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Fluent
  default PgConnection prepareAndQuery(String sql, Object param1, Object param2, Object param3, Object param4, Object param5,
                       Object param6, Handler<AsyncResult<ResultSet>> handler) {
    return prepareAndQuery(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @GenIgnore
  @Fluent
  PgConnection prepareAndQuery(String sql, List<Object> params, Handler<AsyncResult<ResultSet>> handler);

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Collections.singletonList(param), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5), handler);
  }

  @Fluent
  default PgConnection prepareAndExecute(String sql, Object param1, Object param2, Object param3, Object param4, Object param5, Object param6, Handler<AsyncResult<UpdateResult>> handler) {
    return prepareAndExecute(sql, Arrays.asList(param1, param2, param3, param4, param5, param6), handler);
  }

  @GenIgnore
  @Fluent
  PgConnection prepareAndExecute(String sql, List<Object> params, Handler<AsyncResult<UpdateResult>> handler);

  PgPreparedStatement prepare(String sql);

  @Fluent
  PgConnection exceptionHandler(Handler<Throwable> handler);

  @Fluent
  PgConnection closeHandler(Handler<Void> handler);

  void close();

}
