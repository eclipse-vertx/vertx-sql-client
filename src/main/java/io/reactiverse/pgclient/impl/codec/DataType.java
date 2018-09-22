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
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Point;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;

import java.time.*;
import java.util.UUID;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public enum DataType {

  BOOL(16, true, Boolean.class),
  BOOL_ARRAY(1000, true, Boolean[].class),
  INT2(21, true, Short.class, Number.class),
  INT2_ARRAY(1005, true, Short[].class, Number[].class),
  INT4(23, true, Integer.class, Number.class),
  INT4_ARRAY(1007, true, Integer[].class, Number[].class),
  INT8(20, true, Long.class, Number.class),
  INT8_ARRAY(1016, true, Long[].class, Number[].class),
  FLOAT4(700, true, Float.class, Number.class),
  FLOAT4_ARRAY(1021, true, Float[].class, Number[].class),
  FLOAT8(701, true, Double.class, Number.class),
  FLOAT8_ARRAY(1022, true, Double[].class, Number[].class),
  NUMERIC(1700, false, Numeric.class, Number.class),
  NUMERIC_ARRAY(1231, false, Numeric[].class, Number[].class),
  MONEY(790, true, Object.class),
  MONEY_ARRAY(791, true, Object[].class),
  BIT(1560, true, Object.class),
  BIT_ARRAY(1561, true, Object[].class),
  VARBIT(1562, true, Object.class),
  VARBIT_ARRAY(1563, true, Object[].class),
  CHAR(18, true, String.class),
  CHAR_ARRAY(1002, true, String[].class),
  VARCHAR(1043, true, String.class),
  VARCHAR_ARRAY(1015, true, String[].class),
  BPCHAR(1042, true, String.class),
  BPCHAR_ARRAY(1014, true, String[].class),
  TEXT(25, true, String.class),
  TEXT_ARRAY(1009, true, String[].class),
  NAME(19, true, String.class),
  NAME_ARRAY(1003, true, String[].class),
  DATE(1082, true, LocalDate.class),
  DATE_ARRAY(1182, true, LocalDate[].class),
  TIME(1083, true, LocalTime.class),
  TIME_ARRAY(1183, true, LocalTime[].class),
  TIMETZ(1266, true, OffsetTime.class),
  TIMETZ_ARRAY(1270, true, OffsetTime[].class),
  TIMESTAMP(1114, true, LocalDateTime.class),
  TIMESTAMP_ARRAY(1115, true, LocalDateTime[].class),
  TIMESTAMPTZ(1184, true, OffsetDateTime.class),
  TIMESTAMPTZ_ARRAY(1185, true, OffsetDateTime[].class),
  INTERVAL(1186, true, Interval.class),
  INTERVAL_ARRAY(1187, true, Interval[].class),
  BYTEA(17, true, Buffer.class),
  BYTEA_ARRAY(1001, true, Buffer[].class),
  MACADDR(829, true, Object.class),
  INET(869, true, Object[].class),
  CIDR(650, true, Object.class),
  MACADDR8(774, true, Object[].class),
  UUID(2950, true, UUID.class),
  UUID_ARRAY(2951, true, UUID[].class),
  JSON(114, true, Json.class),
  JSON_ARRAY(199, true, Json[].class),
  JSONB(3802, true, Json.class),
  JSONB_ARRAY(3807, true, Json[].class),
  XML(142, true, Object.class),
  XML_ARRAY(143, true, Object[].class),
  POINT(600, true, Point.class),
  POINT_ARRAY(1017, true, Point[].class),
  BOX(603, true, Object.class),
  HSTORE(33670, true, Object.class),
  OID(26, true, Object.class),
  OID_ARRAY(1028, true, Object[].class),
  VOID(2278, true, Object.class),
  UNKNOWN(705, false, String.class);

  private static final Logger logger = LoggerFactory.getLogger(DataType.class);

  public final int id;
  public final boolean supportsBinary;
  public final Class<?> encodingType; // Not really used for now
  public final Class<?> decodingType;

  DataType(int id, boolean supportsBinary, Class<?> type) {
    this.id = id;
    this.supportsBinary = supportsBinary;
    this.decodingType = type;
    this.encodingType = type;
  }

  DataType(int id, boolean supportsBinary, Class<?> encodingType, Class<?> decodingType) {
    this.id = id;
    this.supportsBinary = supportsBinary;
    this.encodingType = encodingType;
    this.decodingType = decodingType;
  }

  public static DataType valueOf(int oid) {
    DataType value = oidToDataType.get(oid);
    if (value == null) {
      logger.warn("Postgres type OID=" + oid + " not handled - using unknown type instead");
      return UNKNOWN;
    } else {
      return value;
    }
  }

  private static IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();

  static {
    for (DataType dataType : values()) {
      oidToDataType.put(dataType.id, dataType);
    }
  }
}
