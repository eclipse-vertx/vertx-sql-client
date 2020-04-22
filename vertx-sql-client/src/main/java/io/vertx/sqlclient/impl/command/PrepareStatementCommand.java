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

package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.impl.PreparedStatement;

public class PrepareStatementCommand extends CommandBase<PreparedStatement> {

  private final String sql;
  private final boolean cacheable;

  public PrepareStatementCommand(String sql, boolean cacheable) {
    this.sql = sql;
    this.cacheable = cacheable;
  }

  public String sql() {
    return sql;
  }

  /**
   * Indicate whether the prepared statement will be cached or not.
   *
   * The prepared statement won't be cached if the command is scheduled from {@link io.vertx.sqlclient.SqlConnection#prepare(String)}
   * since the lifecycle of those statements should not be managed by this client.
   *
   * @return true if the command is scheduled from {@link io.vertx.sqlclient.SqlClient#preparedQuery(String) one-shot preparedQuery.
   *
   */
  public boolean cacheable() {
    return cacheable;
  }

}
