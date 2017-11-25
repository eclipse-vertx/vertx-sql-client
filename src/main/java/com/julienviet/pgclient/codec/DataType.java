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

package com.julienviet.pgclient.codec;

import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public enum DataType {
  // 1 byte
  BOOL(16),
  BOOL_ARRAY(1000),
  // 2 bytes
  INT2(21),
  INT2_ARRAY(1005),
  // 4 bytes
  INT4(23),
  INT4_ARRAY(1007),
  // 8 bytes
  INT8(20),
  INT8_ARRAY(1016),
  // 4 bytes single-precision floating point number
  FLOAT4(700),
  FLOAT4_ARRAY(1021),
  // 8 bytes double-precision floating point number
  FLOAT8(701),
  FLOAT8_ARRAY(1022),
  // User specified precision
  NUMERIC(1700),
  NUMERIC_ARRAY(1231),
  // 8 bytes double
  MONEY(790),
  MONEY_ARRAY(791),
  // Fixed length bit string
  BIT(1560),
  BIT_ARRAY(1561),
  // Limited length bit string
  VARBIT(1562),
  VARBIT_ARRAY(1563),
  // Single length character
  CHAR(18),
  CHAR_ARRAY(1002),
  // Limited length string
  VARCHAR(1043),
  VARCHAR_ARRAY(1015),
  // Limited blank padded length string
  BPCHAR(1042),
  BPCHAR_ARRAY(1014),
  // Unlimited length string
  TEXT(25),
  TEXT_ARRAY(1009),
  // 63 bytes length string (internal type for object names)
  NAME(19),
  NAME_ARRAY(1003),
  // 4 bytes date (no time of day)
  DATE(1082),
  DATE_ARRAY(1182),
  // 8 bytes time of day (no date) without time zone
  TIME(1083),
  TIME_ARRAY(1183),
  // 12 bytes time of day (no date) with time zone
  TIMETZ(1266),
  TIMETZ_ARRAY(1270),
  // 8 bytes date and time without time zone
  TIMESTAMP(1114),
  TIMESTAMP_ARRAY(1115),
  // 8 bytes date and time with time zone
  TIMESTAMPTZ(1184),
  TIMESTAMPTZ_ARRAY(1185),
  // 16 bytes time interval
  INTERVAL(1186),
  INTERVAL_ARRAY(1187),
  // 1 or 4 bytes plus the actual binary string
  BYTEA(17),
  BYTEA_ARRAY(1001),
  // 6 bytes MAC address (XX:XX:XX:XX:XX:XX)
  MACADDR(829),
  // 7 or 19 bytes (IPv4 and IPv6 hosts and networks)
  INET(869),
  // 7 or 19 bytes (IPv4 and IPv6 networks)
  CIDR(650),
  // 8 bytes MAC address (XX:XX:XX:XX:XX:XX:XX:XX)
  MACADDR8(774),
  // UUID
  UUID(2950),
  UUID_ARRAY(2951),
  // Text JSON
  JSON(114),
  // Binary JSON
  JSONB(3802),
  // XML
  XML(142),
  XML_ARRAY(143),
  // Geometric point (x, y)
  POINT(600),
  // Geometric box (lower left, upper right)
  BOX(603),
  HSTORE(33670),
  // Object identifier
  OID(26),
  OID_ARRAY(1028),
  VOID(2278),
  UNKNOWN(705);

  static IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();

  static {
    for (DataType type : values()) {
      oidToDataType.put(type.id, type);
    }
  }

  private final int id;
  DataType(int id) {
    this.id = id;
  }

  public static DataType valueOf(int id) {
    DataType value = oidToDataType.get(id);
    return value != null ? value : DataType.UNKNOWN;
  }
}
