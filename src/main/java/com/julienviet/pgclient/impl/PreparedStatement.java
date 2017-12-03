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

package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Column;
import com.julienviet.pgclient.codec.DataFormat;
import com.julienviet.pgclient.codec.decoder.message.ParameterDescription;
import com.julienviet.pgclient.codec.decoder.message.RowDescription;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PreparedStatement {

  final String sql;
  final ByteBuf statement;
  final ParameterDescription paramDesc;
  final RowDescription rowDesc;

  public PreparedStatement(String sql, String statement, ParameterDescription paramDesc, RowDescription rowDesc) {

    // Fix to use binary
    if (rowDesc != null) {
      rowDesc = new RowDescription(Arrays.stream(rowDesc.getColumns())
        .map(c -> new Column(c.getName(), c.getRelationId(), c.getRelationAttributeNo(), c.getDataType(), c.getLength(), c.getTypeModifier(), DataFormat.BINARY))
        .toArray(Column[]::new));
    }

    this.sql = sql;
    this.statement = statement != null ? Unpooled.copiedBuffer(statement, StandardCharsets.UTF_8).writeByte(0).asReadOnly() : null;
    this.paramDesc = paramDesc;
    this.rowDesc = rowDesc;
  }
}
