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

import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

import static io.vertx.pgclient.impl.codec.DataFormat.BINARY;
import static io.vertx.pgclient.impl.codec.DataFormat.TEXT;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

class PgColumnDesc implements ColumnDescriptor {

  public static final PgColumnDesc[] EMPTY_COLUMNS = new PgColumnDesc[0];

  final String name;
  final int relationId;
  final DataType dataType;
  final DataFormat dataFormat; // are we sure that ????
  final short relationAttributeNo;
  final short length;
  final int typeModifier;

  PgColumnDesc(String name, int relationId, short relationAttributeNo, DataType dataType, short length, int typeModifier, DataFormat dataFormat) {
    this.name = name;
    this.dataType = dataType;
    this.dataFormat = dataFormat;
    this.length = length;
    this.relationId = relationId;
    this.relationAttributeNo = relationAttributeNo;
    this.typeModifier = typeModifier;
  }

  boolean supportsBinary() {
    return dataType.supportsBinary;
  }

  boolean hasTextFormat() {
    return dataFormat == TEXT;
  }

  PgColumnDesc toBinaryDataFormat() {
    return new PgColumnDesc(
      name,
      relationId,
      relationAttributeNo,
      dataType,
      length,
      typeModifier,
      BINARY);
  }

  @Override
  public String name() {
    return name;
  }

  @Override
  public boolean isArray() {
    return dataType.array;
  }

  @Override
  public String typeName() {
    return dataType.toString();
  }

  @Override
  public JDBCType jdbcType() {
    return dataType.jdbcType;
  }
}
