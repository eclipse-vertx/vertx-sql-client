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

package io.vertx.myclient.impl.codec;

import io.vertx.sqlclient.impl.ParamDesc;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.List;

public class MyPreparedStatement implements PreparedStatement {

  final long statementId;
  final String sql;
  final MyParamDesc paramDesc;
  final MyRowDesc rowDesc;

  public MyPreparedStatement(String sql, long statementId, MyParamDesc paramDesc, MyRowDesc rowDesc) {
    this.statementId = statementId;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
  }

  public ParamDesc paramDesc() {
    return paramDesc;
  }

  public RowDesc rowDesc() {
    return rowDesc;
  }

  public String sql() {
    return sql;
  }

  public String prepare(List<Object> values) {
    return paramDesc.prepare(values);
  }
}
