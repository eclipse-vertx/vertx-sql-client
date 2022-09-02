/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.impl.protocol;

import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.sql.JDBCType;

public final class ColumnDefinition implements ColumnDescriptor {

  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_com_query_response_text_resultset_column_definition.html
   */
  private final String name;
  private final int characterSet;
  private final DataType type;
  private final int flags;

  public ColumnDefinition(String name, int characterSet, DataType type, int flags) {
    this.name = name;
    this.characterSet = characterSet;
    this.type = type;
    this.flags = flags;
  }

  public String name() {
    return name;
  }

  public int characterSet() {
    return characterSet;
  }

  public DataType type() {
    return type;
  }

  public int flags() {
    return flags;
  }

  @Override
  public boolean isArray() {
    // MySQL array not yet supported
    return false;
  }

  @Override
  public String typeName() {
    return type.toString();
  }

  @Override
  public JDBCType jdbcType() {
    return type.jdbcType;
  }

  @Override
  public String toString() {
    return "ColumnDefinition{" +
      ", name='" + name + '\'' +
      ", characterSet=" + characterSet +
      ", type=" + type +
      ", flags=" + flags +
      '}';
  }

  /*
    Type of column definition
    https://dev.mysql.com/doc/dev/mysql-server/latest/binary__log__types_8h.html#aab0df4798e24c673e7686afce436aa85
   */
  @SuppressWarnings("unused")
  public static final class ColumnType {
    public static final int MYSQL_TYPE_DECIMAL = 0x00;
    public static final int MYSQL_TYPE_TINY = 0x01;
    public static final int MYSQL_TYPE_SHORT = 0x02;
    public static final int MYSQL_TYPE_LONG = 0x03;
    public static final int MYSQL_TYPE_FLOAT = 0x04;
    public static final int MYSQL_TYPE_DOUBLE = 0x05;
    public static final int MYSQL_TYPE_NULL = 0x06;
    public static final int MYSQL_TYPE_TIMESTAMP = 0x07;
    public static final int MYSQL_TYPE_LONGLONG = 0x08;
    public static final int MYSQL_TYPE_INT24 = 0x09;
    public static final int MYSQL_TYPE_DATE = 0x0A;
    public static final int MYSQL_TYPE_TIME = 0x0B;
    public static final int MYSQL_TYPE_DATETIME = 0x0C;
    public static final int MYSQL_TYPE_YEAR = 0x0D;
    public static final int MYSQL_TYPE_VARCHAR = 0x0F;
    public static final int MYSQL_TYPE_BIT = 0x10;
    public static final int MYSQL_TYPE_JSON = 0xF5;
    public static final int MYSQL_TYPE_NEWDECIMAL = 0xF6;
    public static final int MYSQL_TYPE_ENUM = 0xF7;
    public static final int MYSQL_TYPE_SET = 0xF8;
    public static final int MYSQL_TYPE_TINY_BLOB = 0xF9;
    public static final int MYSQL_TYPE_MEDIUM_BLOB = 0xFA;
    public static final int MYSQL_TYPE_LONG_BLOB = 0xFB;
    public static final int MYSQL_TYPE_BLOB = 0xFC;
    public static final int MYSQL_TYPE_VAR_STRING = 0xFD;
    public static final int MYSQL_TYPE_STRING = 0xFE;
    public static final int MYSQL_TYPE_GEOMETRY = 0xFF;

    /*
      Internal to MySQL Server
     */
    private static final int MYSQL_TYPE_NEWDATE = 0x0E;
    private static final int MYSQL_TYPE_TIMESTAMP2 = 0x11;
    private static final int MYSQL_TYPE_DATETIME2 = 0x12;
    private static final int MYSQL_TYPE_TIME2 = 0x13;
  }

  /*
    https://dev.mysql.com/doc/dev/mysql-server/latest/group__group__cs__column__definition__flags.html
   */
  @SuppressWarnings("unused")
  public static final class ColumnDefinitionFlags {
    public static final int NOT_NULL_FLAG = 0x00000001;
    public static final int PRI_KEY_FLAG = 0x00000002;
    public static final int UNIQUE_KEY_FLAG = 0x00000004;
    public static final int MULTIPLE_KEY_FLAG = 0x00000008;
    public static final int BLOB_FLAG = 0x00000010;
    public static final int UNSIGNED_FLAG = 0x00000020;
    public static final int ZEROFILL_FLAG = 0x00000040;
    public static final int BINARY_FLAG = 0x00000080;
    public static final int ENUM_FLAG = 0x00000100;
    public static final int AUTO_INCREMENT_FLAG = 0x00000200;
    public static final int TIMESTAMP_FLAG = 0x00000400;
    public static final int SET_FLAG = 0x00000800;
    public static final int NO_DEFAULT_VALUE_FLAG = 0x00001000;
    public static final int ON_UPDATE_NOW_FLAG = 0x00002000;
    public static final int NUM_FLAG = 0x00008000;
    public static final int PART_KEY_FLAG = 0x00004000;
    public static final int GROUP_FLAG = 0x00008000;
    public static final int UNIQUE_FLAG = 0x00010000;
    public static final int BINCMP_FLAG = 0x00020000;
    public static final int GET_FIXED_FIELDS_FLAG = 0x00040000;
    public static final int FIELD_IN_PART_FUNC_FLAG = 0x00080000;
    public static final int FIELD_IN_ADD_INDEX = 0x00100000;
    public static final int FIELD_IS_RENAMED = 0x00200000;
    public static final int FIELD_FLAGS_STORAGE_MEDIA = 22;
    public static final int FIELD_FLAGS_STORAGE_MEDIA_MASK = 3 << FIELD_FLAGS_STORAGE_MEDIA;
    public static final int FIELD_FLAGS_COLUMN_FORMAT = 24;
    public static final int FIELD_FLAGS_COLUMN_FORMAT_MASK = 3 << FIELD_FLAGS_COLUMN_FORMAT;
    public static final int FIELD_IS_DROPPED = 0x04000000;
    public static final int EXPLICIT_NULL_FLAG = 0x08000000;
    public static final int FIELD_IS_MARKED = 0x10000000;
  }
}
