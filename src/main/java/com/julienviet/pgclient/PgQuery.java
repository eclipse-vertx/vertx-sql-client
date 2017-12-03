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
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

@VertxGen
public interface PgQuery {

  /**
   * Set the fetch size of the query when executed.
   *
   * It is only valid for prepared queries executed with the streaming API.
   *
   * @param size the fetch size
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgQuery fetch(int size);

  void execute(Handler<AsyncResult<PgResult<Tuple>>> handler);

  boolean hasNext();

  void next(Handler<AsyncResult<PgResult<Tuple>>> handler);

  default void close() {
    close(ar -> {});
  }

  void close(Handler<AsyncResult<Void>> completionHandler);

}
