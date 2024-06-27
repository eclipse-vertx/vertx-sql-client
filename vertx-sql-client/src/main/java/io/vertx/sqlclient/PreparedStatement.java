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

package io.vertx.sqlclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.Future;
import io.vertx.sqlclient.internal.ArrayTuple;

/**
 * A prepared statement, the statement is pre-compiled and it's more efficient to execute the statement for multiple times.
 * In addition, this kind of statement provides protection against SQL injection attacks.
 *
 * <p>From a prepared statement you can
 *
 * <ul>
 *   <li>use {@link #query()} to create and execute a {@link PreparedQuery}</li>
 *   <li>use {@link #cursor()} to create a {@link Cursor}</li>
 *   <li>use {@link #createStream} to create a {@link RowStream}</li>
 * </ul>
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PreparedStatement {

  /**
   * Create a prepared query for this statement.
   *
   * @return the prepared query
   */
  PreparedQuery<RowSet<Row>> query();

  /**
   * Like {@link #cursor(Tuple)} but with empty arguments.
   */
  default Cursor cursor() {
    return cursor(ArrayTuple.EMPTY);
  }

  /**
   * Create a cursor with the provided {@code arguments}.
   *
   * @param args the list of arguments
   * @return the query
   */
  Cursor cursor(Tuple args);

  /**
   * Like {@link #createStream(int, Tuple)} but with empty arguments.
   */
  default RowStream<Row> createStream(int fetch) {
    return createStream(fetch, ArrayTuple.EMPTY);
  }

  /**
   * Execute the prepared query with a cursor and createStream the result. The createStream opens a cursor
   * with a {@code fetch} size to fetch the results.
   * <p/>
   * Note: this requires to be in a transaction, since cursors require it.
   *
   * @param fetch the cursor fetch size
   * @param args the prepared query arguments
   * @return the createStream
   */
  RowStream<Row> createStream(int fetch, Tuple args);

  /**
   * Close the prepared query and release its resources.
   */
  Future<Void> close();

}
