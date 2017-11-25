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

import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;

import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.TIMESTAMPTZ_FORMAT;
import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.TIMESTAMP_FORMAT;
import static com.julienviet.pgclient.codec.formatter.TimeFormatter.TIMETZ_FORMAT;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public enum DataType {
  // 1 byte
  BOOL(16) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      if(buff.readByte() == 't') {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }
  },
  BOOL_ARRAY(1000),
  // 2 bytes
  INT2(21) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      return (short)DataType.decodeInt(len, buff);
    }
  },
  INT2_ARRAY(1005),
  // 4 bytes
  INT4(23) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      return (int)DataType.decodeInt(len, buff);
    }
  },
  INT4_ARRAY(1007),
  // 8 bytes
  INT8(20) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      return DataType.decodeInt(len, buff);
    }
  },
  INT8_ARRAY(1016),
  // 4 bytes single-precision floating point number
  FLOAT4(700) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Float.parseFloat(cs.toString());
    }
  },
  FLOAT4_ARRAY(1021),
  // 8 bytes double-precision floating point number
  FLOAT8(701) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Double.parseDouble(cs.toString());
    }
  },
  FLOAT8_ARRAY(1022),
  // User specified precision
  NUMERIC(1700) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      BigDecimal big = new BigDecimal(cs.toString());
      // julien : that does not seem consistent to either return a Double or BigInteger
      if (big.scale() == 0) {
        return big.toBigInteger();
      } else {
        // we might loose precision here
        return big.doubleValue();
      }
    }
  },
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
  CHAR(18) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      return (char)buff.readByte();
    }
  },
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
  TIMETZ(1266) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetTime.parse(cs, TIMETZ_FORMAT).toString(); // julien: why toString ?
    }
  },
  TIMETZ_ARRAY(1270),
  // 8 bytes date and time without time zone
  TIMESTAMP(1114) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDateTime.parse(cs, TIMESTAMP_FORMAT).toInstant(ZoneOffset.UTC);
    }
  },
  TIMESTAMP_ARRAY(1115),
  // 8 bytes date and time with time zone
  TIMESTAMPTZ(1184) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetDateTime.parse(cs, TIMESTAMPTZ_FORMAT).toInstant();
    }
  },
  TIMESTAMPTZ_ARRAY(1185),
  // 16 bytes time interval
  INTERVAL(1186),
  INTERVAL_ARRAY(1187),
  // 1 or 4 bytes plus the actual binary string
  BYTEA(17) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      buff.readByte(); // \
      buff.readByte(); // x
      len = (len - 2) / 2;
      byte[] bytes = new byte[len];
      for (int i = 0;i < len;i++) {
        byte b0 = decodeHexChar(buff.readByte());
        byte b1 = decodeHexChar(buff.readByte());
        bytes[i] = (byte)(b0 * 16 + b1);
      }
      return bytes;
    }
    private byte decodeHexChar(byte b) {
      if (b >= '0' && b <= '9') {
        return (byte)(b - '0');
      } else {
        return (byte)(b - 'a' + 10);
      }
    }
  },
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
  JSON(114) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return decodeJson(cs.toString());
    }
  },
  // Binary JSON
  JSONB(3802) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Not sure this is correct
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return decodeJson(cs.toString());
    }
  },
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

  private static long decodeInt(int len, ByteBuf buff) {
    long value = 0;
    for (int i = 0;i < len;i++) {
      value = value * 10 + (buff.readUnsignedByte() -'0');
    }
    return value;
  }

  private static Object decodeJson(String value) {
    if(value.charAt(0)== '{') {
      return new JsonObject(value);
    } else {
      return new JsonArray(value);
    }
  }

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

  public Object decodeText(int len, ByteBuf buff) {
    // Default best effort implementation
    return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
  }

  public Object decodeBinary(int len, ByteBuf buff) {
    byte[] data = new byte[len];
    buff.readBytes(data);
    return decodeBinary(data);
  }

  public Object decodeBinary(byte[] data) {
    // Not implemented
    return null;
  }
}
