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

import java.util.List;

/**
 * Defines common SQL client operations with a database server.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface SqlClient {

  /**
   * Create a query, the {@link Query#execute} method must be called to execute the query.
   *
   * Create a query.
   *
   * @return the query
   */
  Query<RowSet<Row>> query(String sql);

  /**
   * Create a prepared query, one of the {@link PreparedQuery#execute}, {@link PreparedQuery#executeBatch}
   * methods must be called to execute the query.
   *
   * @return the prepared query
   */
  PreparedQuery<RowSet<Row>> preparedQuery(String sql);

  /**
   * Close the client and release the associated resources.
   */
  void close();

}
