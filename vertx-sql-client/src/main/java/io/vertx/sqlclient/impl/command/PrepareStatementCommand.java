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

import io.vertx.sqlclient.PrepareOptions;
import io.vertx.sqlclient.impl.PreparedStatement;

import java.util.List;

public class PrepareStatementCommand extends CommandBase<PreparedStatement> {

  private final String sql;
  private final PrepareOptions options;
  private final boolean managed;
  private final List<Class<?>> parameterTypes;

  public PrepareStatementCommand(String sql, PrepareOptions options, boolean managed) {
    this(sql, options, managed, null);
  }

  public PrepareStatementCommand(String sql, PrepareOptions options, boolean managed, List<Class<?>> parameterTypes) {
    this.options = options;
    this.sql = sql;
    this.managed = managed;
    this.parameterTypes = parameterTypes;
  }

  public String sql() {
    return sql;
  }

  public PrepareOptions options() {
    return options;
  }

  /**
   * @return the list of the prepared statement parameter types or {@code null} when they are not yet determined.
   */
  public List<Class<?>> parameterTypes() {
    return parameterTypes;
  }

  /**
   * Indicate whether the prepared statement will be managed by the connection
   *
   * <p>Managed prepared statements survive a single interactions with the database and will be closed
   * at some time by the connection (either with a cache eviction or when the prepared statement is closed).
   *
   * <p>Otherwise the prepared statement is ephermal and valid only for a single execution. It should
   * be disposed after the prepared statement has been executed.
   */
  public boolean isManaged() {
    return managed;
  }
}
