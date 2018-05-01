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
package io.reactiverse.pgclient.impl.codec;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.reactiverse.pgclient.Json;
import io.reactiverse.pgclient.Numeric;
import io.vertx.core.buffer.Buffer;

import java.time.*;
import java.util.UUID;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public enum DataType {

  BOOL(16, Boolean.class),
  BOOL_ARRAY(1000, boolean[].class),
  INT2(21, Short.class),
  INT2_ARRAY(1005, short[].class),
  INT4(23, Integer.class),
  INT4_ARRAY(1007, int[].class),
  INT8(20, Long.class),
  INT8_ARRAY(1016, long[].class),
  FLOAT4(700, Float.class),
  FLOAT4_ARRAY(1021, float[].class),
  FLOAT8(701, Double.class),
  FLOAT8_ARRAY(1022, double[].class),
  NUMERIC_ID(1700, Numeric.class),
  NUMERIC_ARRAY_ID(1231, Numeric[].class),
  MONEY_ID(790, Object.class),
  MONEY_ARRAY_ID(791, Object[].class),
  BITS_ID(1560, Object.class),
  BIT_ARRAY_ID(1561, Object[].class),
  VARBIT_ID(1562, Object.class),
  VARBIT_ARRAY_ID(1563, Object[].class),
  CHAR(18, Character.class),
  CHAR_ARRAY(1002, char[].class),
  VARCHAR(1043, String.class),
  VARCHAR_ARRAY(1015, String[].class),
  BPCHAR(1042, String.class),
  BPCHAR_ARRAY(1014, String[].class),
  TEXT(25, String.class),
  TEXT_ARRAY(1009, String[].class),
  NAME(19, String.class),
  NAME_ARRAY(1003, String[].class),
  DATE(1082, LocalDate.class),
  DATE_ARRAY(1182, LocalDate[].class),
  TIME(1083, LocalTime.class),
  TIME_ARRAY(1183, LocalTime[].class),
  TIMETZ(1266, OffsetTime.class),
  TIMETZ_ARRAY(1270, OffsetTime[].class),
  TIMESTAMP(1114, LocalDateTime.class),
  TIMESTAMP_ARRAY(1115, LocalDateTime[].class),
  TIMESTAMPTZ(1184, OffsetDateTime.class),
  TIMESTAMPTZ_ARRAY(1185, OffsetDateTime[].class),
  INTERVAL_ID(1186, Object.class),
  INTERVAL_ARRAY_ID(1187, Object[].class),
  BYTEA(17, Buffer.class),
  BYTEA_ARRAY(1001, Buffer[].class),
  MACADDR_ID(829, Object.class),
  INET_ID(869, Object[].class),
  CIDR_ID(650, Object.class),
  MACADDR8_ID(774, Object[].class),
  UUID(2950, UUID.class),
  UUID_ARRAY(2951, UUID[].class),
  JSON(114, Json.class),
  JSONB(3802, Json.class),
  XML_ID(142, Object.class),
  XML_ARRAY_ID(143, Object[].class),
  POINT_ID(600, Object.class),
  BOX_ID(603, Object.class),
  HSTORE_ID(33670, Object.class),
  OID_ID(26, Object.class),
  OID_ARRAY_ID(1028, Object[].class),
  VOID_ID(2278, Object.class),
  UNKNOWN_ID(705, Object.class);

  public final int value;
  public Class<?> type;

  DataType(int value, Class<?> type) {
    this.value = value;
    this.type = type;
  }

  public static DataType valueOf(int id) {
    DataType value = oidToDataType.get(id);
    return value != null ? value : UNKNOWN_ID;
  }

  private static IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();

  static {
    for (DataType dataType : values()) {
      oidToDataType.put(dataType.value, dataType);
    }
  }
}
