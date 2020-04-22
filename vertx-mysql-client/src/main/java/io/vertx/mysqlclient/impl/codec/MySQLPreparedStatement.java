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

import io.vertx.mysqlclient.impl.MySQLParamDesc;
import io.vertx.mysqlclient.impl.MySQLRowDesc;
import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.TupleInternal;

class MySQLPreparedStatement implements PreparedStatement {

  final long statementId;
  final String sql;
  final MySQLParamDesc paramDesc;
  final MySQLRowDesc rowDesc;
  final boolean cacheable;

  boolean isCursorOpen;

  MySQLPreparedStatement(String sql, long statementId, MySQLParamDesc paramDesc, MySQLRowDesc rowDesc, boolean cacheable) {
    this.statementId = statementId;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
    this.cacheable = cacheable;
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
  public String prepare(TupleInternal values) {
    return paramDesc.prepare(values);
  }

  @Override
  public boolean cacheable() {
    return cacheable;
  }
}
