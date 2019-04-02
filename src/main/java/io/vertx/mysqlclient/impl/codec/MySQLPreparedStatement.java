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

package io.vertx.mysqlclient.impl.codec;

import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.List;

public class MySQLPreparedStatement implements PreparedStatement {

  final long statementId;
  final String sql;
  final MySQLParamDesc paramDesc;
  final MySQLRowDesc rowDesc;

  public MySQLPreparedStatement(String sql, long statementId, MySQLParamDesc paramDesc, MySQLRowDesc rowDesc) {
    this.statementId = statementId;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
  }

  @Override
  public ParamDesc paramDesc() {
    return paramDesc;
  }

  @Override
  public RowDesc rowDesc() {
    return rowDesc;
  }

  @Override
  public String sql() {
    return sql;
  }

  @Override
  public String prepare(List<Object> values) {
    return paramDesc.prepare(values);
  }
}
