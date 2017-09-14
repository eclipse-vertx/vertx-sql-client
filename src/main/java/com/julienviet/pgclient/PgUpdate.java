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

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.UpdateResult;

/**
 * The binding of a {@link PgPreparedStatement} and a set of parameters that can be executed once.
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

@VertxGen
public interface PgUpdate {
  /**
   * Executes the query, the {@code handler} will be called when the update result is available or
   * if an error occurs.
   * <p/>
   *
   * @param handler the handler to call back
   */
  void execute(Handler<AsyncResult<UpdateResult>> handler);
}