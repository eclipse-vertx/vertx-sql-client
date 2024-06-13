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

package io.vertx.mysqlclient.impl.datatype;

import io.netty.util.collection.ShortObjectHashMap;
import io.netty.util.collection.ShortObjectMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.logging.Logger;
import io.vertx.core.internal.logging.LoggerFactory;
import io.vertx.mysqlclient.data.spatial.Geometry;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigInteger;
import java.sql.JDBCType;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public enum DataType {

  // unsigned int
  U_INT8(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY, Short.class, JDBCType.SMALLINT),
  U_INT16(ColumnDefinition.ColumnType.MYSQL_TYPE_SHORT, Integer.class, JDBCType.INTEGER),
  U_INT24(ColumnDefinition.ColumnType.MYSQL_TYPE_INT24, Integer.class, JDBCType.INTEGER),
  U_INT32(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG, Long.class, JDBCType.BIGINT),
  U_INT64(ColumnDefinition.ColumnType.MYSQL_TYPE_LONGLONG, BigInteger.class, JDBCType.NUMERIC),
  // signed int
  INT8(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY, Byte.class, JDBCType.TINYINT),
  INT16(ColumnDefinition.ColumnType.MYSQL_TYPE_SHORT, Short.class, JDBCType.SMALLINT),
  INT24(ColumnDefinition.ColumnType.MYSQL_TYPE_INT24, Integer.class, JDBCType.INTEGER),
  INT32(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG, Integer.class, JDBCType.INTEGER),
  INT64(ColumnDefinition.ColumnType.MYSQL_TYPE_LONGLONG, Long.class, JDBCType.BIGINT),
  // numeric
  BIT(ColumnDefinition.ColumnType.MYSQL_TYPE_BIT, Long.class, JDBCType.BIT),
  DOUBLE(ColumnDefinition.ColumnType.MYSQL_TYPE_DOUBLE, Double.class, JDBCType.DOUBLE),
  FLOAT(ColumnDefinition.ColumnType.MYSQL_TYPE_FLOAT, Float.class, JDBCType.FLOAT),
  NUMERIC(ColumnDefinition.ColumnType.MYSQL_TYPE_NEWDECIMAL, Numeric.class, JDBCType.DECIMAL),
  // string
  STRING(ColumnDefinition.ColumnType.MYSQL_TYPE_STRING, String.class, JDBCType.CHAR),
  VARSTRING(ColumnDefinition.ColumnType.MYSQL_TYPE_VAR_STRING, String.class, JDBCType.VARCHAR),
  // clob
  TINY_TEXT(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY_BLOB, String.class, JDBCType.CLOB),
  TEXT(ColumnDefinition.ColumnType.MYSQL_TYPE_BLOB, String.class, JDBCType.CLOB),
  MEDIUM_TEXT(ColumnDefinition.ColumnType.MYSQL_TYPE_MEDIUM_BLOB, String.class, JDBCType.CLOB),
  LONG_TEXT(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG_BLOB, String.class, JDBCType.CLOB),
  // binary
  BINARY(ColumnDefinition.ColumnType.MYSQL_TYPE_STRING, Buffer.class, JDBCType.BINARY),
  VARBINARY(ColumnDefinition.ColumnType.MYSQL_TYPE_VAR_STRING, Buffer.class, JDBCType.VARBINARY),
  // blob
  TINY_BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY_BLOB, Buffer.class, JDBCType.BLOB),
  BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_BLOB, Buffer.class, JDBCType.BLOB),
  MEDIUM_BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_MEDIUM_BLOB, Buffer.class, JDBCType.BLOB),
  LONG_BLOB(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG_BLOB, Buffer.class, JDBCType.BLOB),
  // time
  DATE(ColumnDefinition.ColumnType.MYSQL_TYPE_DATE, LocalDate.class, JDBCType.DATE),
  TIME(ColumnDefinition.ColumnType.MYSQL_TYPE_TIME, Duration.class, JDBCType.TIME),
  DATETIME(ColumnDefinition.ColumnType.MYSQL_TYPE_DATETIME, LocalDateTime.class, JDBCType.TIMESTAMP),
  YEAR(ColumnDefinition.ColumnType.MYSQL_TYPE_YEAR, Short.class, JDBCType.SMALLINT),
  TIMESTAMP(ColumnDefinition.ColumnType.MYSQL_TYPE_TIMESTAMP, LocalDateTime.class, JDBCType.TIMESTAMP),

  JSON(ColumnDefinition.ColumnType.MYSQL_TYPE_JSON, Object.class, JDBCType.OTHER),
  GEOMETRY(ColumnDefinition.ColumnType.MYSQL_TYPE_GEOMETRY, Geometry.class, JDBCType.OTHER),
  NULL(ColumnDefinition.ColumnType.MYSQL_TYPE_NULL, Object.class, JDBCType.OTHER), // useful for mariadb prepare statement response
  UNBIND((short) -1, Object.class, JDBCType.OTHER); // useful for binding param values
  ;

  private static final Logger LOGGER = LoggerFactory.getLogger(DataType.class);

  // protocol id
  private final short columnType;

  private final Class<?> javaType;

  private final JDBCType jdbcType;

  DataType(short columnType, Class<?> javaType, JDBCType jdbcType) {
    this.columnType = columnType;
    this.javaType = javaType;
    this.jdbcType = jdbcType;
  }

  public int getColumnType() {
    return columnType;
  }

  public Class<?> getJavaType() {
    return javaType;
  }

  public JDBCType getJdbcType() {
    return jdbcType;
  }

  public static DataType parseDataType(short type, int characterSet, int flags) {
    // integer
    if (COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.containsKey(type)) {
      return isUnsignedNumeric(flags) ? COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.get(type).get(0) : COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.get(type).get(1);
    }

    // string
    if (COLUMN_TYPE_TO_STRING_TYPE_MAPPING.containsKey(type)) {
      return isText(characterSet) ? COLUMN_TYPE_TO_STRING_TYPE_MAPPING.get(type).get(0) : COLUMN_TYPE_TO_STRING_TYPE_MAPPING.get(type).get(1);
    }

    // others
    DataType dataType = COLUMN_TYPE_TO_DATA_TYPE_MAPPING.get(type);
    if (dataType == null) {
      LOGGER.warn(String.format("MySQL data type Id =[%d] not handled - using string type instead", type));
      return STRING;
    } else {
      return dataType;
    }
  }

  private static final ShortObjectMap<List<DataType>> COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING;
  private static final ShortObjectMap<List<DataType>> COLUMN_TYPE_TO_STRING_TYPE_MAPPING;
  private static final ShortObjectMap<DataType> COLUMN_TYPE_TO_DATA_TYPE_MAPPING;

  static {
    // integer
    // column type -> uint, int
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING = new ShortObjectHashMap<>(5, 1.0f);
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY, Arrays.asList(U_INT8, INT8));
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_SHORT, Arrays.asList(U_INT16, INT16));
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_INT24, Arrays.asList(U_INT24, INT24));
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG, Arrays.asList(U_INT32, INT32));
    COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_LONGLONG, Arrays.asList(U_INT64, INT64));

    // string
    // column type -> clob, blob
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING = new ShortObjectHashMap<>(6, 1.0f);
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_STRING, Arrays.asList(STRING, BINARY));
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_VAR_STRING, Arrays.asList(VARSTRING, VARBINARY));
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_TINY_BLOB, Arrays.asList(TINY_TEXT, TINY_BLOB));
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_BLOB, Arrays.asList(TEXT, BLOB));
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_MEDIUM_BLOB, Arrays.asList(MEDIUM_TEXT, MEDIUM_BLOB));
    COLUMN_TYPE_TO_STRING_TYPE_MAPPING.put(ColumnDefinition.ColumnType.MYSQL_TYPE_LONG_BLOB, Arrays.asList(LONG_TEXT, LONG_BLOB));

    // others
    COLUMN_TYPE_TO_DATA_TYPE_MAPPING = new ShortObjectHashMap<>(13, 1.0f);
    for (DataType dataType : DataType.values()) {
        if (COLUMN_TYPE_TO_INTEGER_TYPE_MAPPING.containsKey(dataType.columnType) || COLUMN_TYPE_TO_STRING_TYPE_MAPPING.containsKey(dataType.columnType)) {
          continue;
        }

      COLUMN_TYPE_TO_DATA_TYPE_MAPPING.put(dataType.columnType, dataType);
    }
  }

  private static boolean isText(int collationId) {
    return collationId != MySQLCollation.binary.collationId();
  }

  private static boolean isUnsignedNumeric(int columnDefinitionFlags) {
    return (columnDefinitionFlags & ColumnDefinition.ColumnDefinitionFlags.UNSIGNED_FLAG) != 0;
  }

}
