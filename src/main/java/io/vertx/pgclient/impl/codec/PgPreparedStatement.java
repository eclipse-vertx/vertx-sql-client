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

package io.vertx.pgclient.impl.codec;

import io.vertx.sqlclient.impl.PreparedStatement;

import java.util.Arrays;
import java.util.List;

class PgPreparedStatement implements PreparedStatement {

  private static final PgColumnDesc[] EMPTY_COLUMNS = new PgColumnDesc[0];

  final String sql;
  final Bind bind;
  final PgParamDesc paramDesc;
  final PgRowDesc rowDesc;

  PgPreparedStatement(String sql, long statement, PgParamDesc paramDesc, PgRowDesc rowDesc) {

    // Fix to use binary when possible
    if (rowDesc != null) {
      rowDesc = new PgRowDesc(Arrays.stream(rowDesc.columns)
        .map(c -> new PgColumnDesc(
          c.name,
          c.relationId,
          c.relationAttributeNo,
          c.dataType,
          c.length,
          c.typeModifier,
          c.dataType.supportsBinary ? DataFormat.BINARY : DataFormat.TEXT))
        .toArray(PgColumnDesc[]::new));
    }

    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
    this.sql = sql;
    this.bind = new Bind(statement, paramDesc != null ? paramDesc.paramDataTypes() : null, rowDesc != null ? rowDesc.columns : EMPTY_COLUMNS);
  }

  public PgRowDesc rowDesc() {
    return rowDesc;
  }

  public String sql() {
    return sql;
  }

  public String prepare(List<Object> values) {
    return paramDesc.prepare(values);
  }
}
