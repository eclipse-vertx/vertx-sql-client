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
 * A createBatch execution.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgBatch {

  /**
   * Add a command with a no arguments to this createBatch
   *
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default PgBatch add() {
    return add(ArrayTuple.EMPTY);
  }

  /**
   * Add a command with a variable number of arguments to this createBatch
   *
   * @param args the arguments of the command
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  PgBatch add(Tuple args);

  /**
   * Execute the createBatch and notifies {@code resultHandler} of the result.
   *
   * @param resultHandler the handler of the result
   */
  void execute(Handler<AsyncResult<PgBatchResult<Tuple>>> resultHandler);

}
