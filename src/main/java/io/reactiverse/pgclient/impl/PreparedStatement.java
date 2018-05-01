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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.impl.codec.DataFormat;
import io.reactiverse.pgclient.impl.codec.decoder.message.ParameterDescription;
import io.reactiverse.pgclient.impl.codec.decoder.message.RowDescription;

import java.util.Arrays;

public class PreparedStatement {

  private static final ColumnDesc[] EMPTY_COLUMNS = new ColumnDesc[0];

  final String sql;
  final long statement;
  final ParameterDescription paramDesc;
  final RowDescription rowDesc;



  public PreparedStatement(String sql, long statement, ParameterDescription paramDesc, RowDescription rowDesc) {

    // Fix to use binary when possible
    if (rowDesc != null) {
      rowDesc = new RowDescription(Arrays.stream(rowDesc.columns())
        .map(c -> new ColumnDesc(
          c.getName(),
          c.getRelationId(),
          c.getRelationAttributeNo(),
          c.getDataType(),
          c.getLength(),
          c.getTypeModifier(),
          c.getDataType().supportsBinary ? DataFormat.BINARY : DataFormat.TEXT))
        .toArray(ColumnDesc[]::new));
    }

    this.sql = sql;
    this.statement = statement;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
  }

  ColumnDesc[] columnDescs() {
    return rowDesc != null ? rowDesc.columns() : EMPTY_COLUMNS;
  }
}
