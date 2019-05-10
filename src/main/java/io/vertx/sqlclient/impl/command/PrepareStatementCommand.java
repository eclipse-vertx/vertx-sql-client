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

import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.sqlclient.impl.PreparedStatement;

public class PrepareStatementCommand extends CommandBase<PreparedStatement> {

  private final String sql;
  public long statement; // 0 means unamed statement otherwise CString
  public PgSocketConnection.CachedPreparedStatement cached;

  public PrepareStatementCommand(String sql) {
    this.sql = sql;
  }

  public String sql() {
    return sql;
  }

  public long statement() {
    return statement;
  }

}
