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

/**
 * A postgres prepared statement.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgPreparedStatement {

  /**
   * @return create a query from this statement with no arguments
   */
  default PgQuery query() {
    return query(ArrayTuple.EMPTY);
  }

  /**
   * @param args the list of arguments
   * @return create a query from this statement with a variable list of arguments
   */
  PgQuery query(Tuple args);

  /**
   * Create a new batch.
   *
   * @return the batch
   */
  PgBatch batch();

  /**
   * Close the prepared statement and release its resources.
   */
  void close();

  /**
   * Like {@link #close()} but notifies the {@code completionHandler} when it's closed.
   */
  void close(Handler<AsyncResult<Void>> completionHandler);

}
