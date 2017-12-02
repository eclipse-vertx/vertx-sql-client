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

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;

import static com.julienviet.pgclient.codec.formatter.DateTimeFormatter.*;
import static com.julienviet.pgclient.codec.formatter.TimeFormatter.*;
import static javax.xml.bind.DatatypeConverter.*;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataType<T> {

  // 1 byte
  public static DataType<Boolean> BOOL = new DataType<Boolean>(Boolean.class,16) {
    @Override
    public Boolean decodeText(int len, ByteBuf buff) {
      if(buff.readByte() == 't') {
        return Boolean.TRUE;
      } else {
        return Boolean.FALSE;
      }
    }
    public Boolean decodeBinary(int len, ByteBuf buff) {
      return buff.readBoolean();
    }
    @Override
    public void encodeText(Boolean value, ByteBuf buff) {
      buff.writeInt(1);
      buff.writeByte(value == Boolean.TRUE ? 't' : 'f');
    }
    @Override
    public void encodeBinary(Boolean value, ByteBuf buff) {
      buff.writeInt(1);
      buff.writeBoolean(value);
    }
  };

  public static DataType<boolean[]> BOOL_ARRAY = new DataType<>(boolean[].class, 1000);

  // 2 bytes
  public static final DataType<Short> INT2 = new DataType<Short>(Short.class,21) {
    @Override
    public Short decodeText(int len, ByteBuf buff) {
      return (short)DataType.decodeInt(len, buff);
    }
    @Override
    public Short decodeBinary(int len, ByteBuf buff) {
      return buff.readShort();
    }
    @Override
    public void encodeBinary(Short value, ByteBuf buff) {
      buff.writeInt(2);
      buff.writeShort(value);
    }
  };

  public static DataType<short[]> INT2_ARRAY = new DataType<>(short[].class, 1005);

  // 4 bytes
  public static final DataType<Integer> INT4 = new DataType<Integer>(Integer.class,23) {
    @Override
    public Integer decodeText(int len, ByteBuf buff) {
      return (int)DataType.decodeInt(len, buff);
    }
    @Override
    public Integer decodeBinary(int len, ByteBuf buff) {
      return buff.readInt();
    }
    @Override
    public void encodeBinary(Integer value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeInt(value);
    }
  };

  public static DataType<int[]> INT4_ARRAY = new DataType<>(int[].class, 1007);

  // 8 bytes
  public static final DataType<Long> INT8 = new DataType<Long>(Long.class,20) {
    @Override
    public Long decodeText(int len, ByteBuf buff) {
      return DataType.decodeInt(len, buff);
    }
    @Override
    public Long decodeBinary(int len, ByteBuf buff) {
      return buff.readLong();
    }
    @Override
    public void encodeBinary(Long value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(value);
    }
  };

  public static DataType<int[]> INT8_ARRAY = new DataType<>(int[].class, 1016);

  // 4 bytes single-precision floating point number
  public static final DataType<Float> FLOAT4 = new DataType<Float>(Float.class, 700) {
    @Override
    public Float decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Float.parseFloat(cs.toString());
    }
    @Override
    public Float decodeBinary(int len, ByteBuf buff) {
      return buff.readFloat();
    }
    @Override
    public void encodeBinary(Float value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeFloat(value);
    }
  };

  public static DataType<float[]> FLOAT4_ARRAY = new DataType<>(float[].class, 1021);

  // 8 bytes double-precision floating point number
  public static final DataType<Double> FLOAT8 = new DataType<Double>(Double.class,701) {
    @Override
    public Double decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Double.parseDouble(cs.toString());
    }
    @Override
    public Double decodeBinary(int len, ByteBuf buff) {
      return buff.readDouble();
    }
    @Override
    public void encodeBinary(Double value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeDouble(value);
    }
  };

  public static DataType<double[]> FLOAT8_ARRAY = new DataType<>(double[].class, 1022);

  // User specified precision
  public static final DataType<Number> NUMERIC = new DataType<Number>(Number.class,1700) {
    @Override
    public Number decodeText(int len, ByteBuf buff) {
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
  };

  public static DataType<double[]> NUMERIC_ARRAY = new DataType<>(double[].class, 1231);

  // 8 bytes double
  public static DataType<Object> MONEY = new DataType<>(Object.class,790);
  public static DataType<Object> MONEY_ARRAY = new DataType<>(Object.class,791);

  // Fixed length bit string
  public static DataType<Object> BITS = new DataType<>(Object.class,1560);
  public static DataType<Object> BIT_ARRAY = new DataType<>(Object.class,1561);

  // Limited length bit string
  public static DataType<Object> VARBIT = new DataType<>(Object.class,1562);
  public static DataType<Object> VARBIT_ARRAY = new DataType<>(Object.class,1563);

  // Single length character
  public static final DataType<Character> CHAR = new DataType<Character>(Character.class, 18) {
    @Override
    public Character decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public Character decodeBinary(int len, ByteBuf buff) {
      return (char)buff.readByte();
    }
  };

  public static DataType<Object> CHAR_ARRAY = new DataType<>(Object.class,1002);

  // Limited length string
  public static final DataType<String> VARCHAR = new DataType<String>(String.class,1043) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
    @Override
    public void encodeBinary(String value, ByteBuf buff) {
      super.encodeText(value, buff);
    }
  };

  public static DataType<Object> VARCHAR_ARRAY = new DataType<>(Object.class,1015);

  // Limited blank padded length string
  public static DataType<Object> BPCHAR = new DataType<Object>(Object.class,1042) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
  };
  public static DataType<Object> BPCHAR_ARRAY = new DataType<>(Object.class,1014);

  // Unlimited length string
  public static DataType<String> TEXT = new DataType<String>(String.class,25) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
  };
  public static DataType<Object> TEXT_ARRAY = new DataType<>(Object.class,1009);

  // 63 bytes length string (internal type for object names)
  public static DataType<String> NAME = new DataType<String>(String.class,19) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
  };
  public static DataType<Object> NAME_ARRAY = new DataType<>(Object.class,1003);

  // 4 bytes date (no time of day)
  public static DataType<Object> DATE = new DataType<Object>(Object.class,1082) {
    final LocalDate PG_EPOCH = LocalDate.of(2000, 1, 1);
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDate.parse(cs).toString();
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      LocalDate date = PG_EPOCH.plus(buff.readInt(), ChronoUnit.DAYS);
      return date.toString();
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeInt((int) -LocalDate.parse(value.toString()).until(PG_EPOCH, ChronoUnit.DAYS));
    }
  };
  public static DataType<Object> DATE_ARRAY = new DataType<>(Object.class,1182);

  // 8 bytes time of day (no date) without time zone
  public static DataType<Object> TIME = new DataType<Object>(Object.class,1083) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalTime.parse(cs).toString();
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      // micros to nanos
      LocalTime time = LocalTime.ofNanoOfDay(buff.readLong() * 1000);
      return time.toString();
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      buff.writeInt(8);
      // nanos to micros
      buff.writeLong(LocalTime.parse(value.toString()).toNanoOfDay() / 1000);
    }
  };
  public static DataType<Object> TIME_ARRAY = new DataType<>(Object.class,1183);

  // 12 bytes time of day (no date) with time zone
  public static final DataType<Object> TIMETZ = new DataType<Object>(Object.class,1266) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetTime.parse(cs, TIMETZ_FORMAT).toString(); // julien: why toString ? emad: because it's not JSON friendly!
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      // micros to nanos
      OffsetTime timetz = OffsetTime.of(LocalTime.ofNanoOfDay(buff.readLong() * 1000),
        // zone offset in seconds (should we change it to UTC ?)
        ZoneOffset.ofTotalSeconds(-buff.readInt()));
      return timetz.toString();
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      OffsetTime timetz = OffsetTime.parse(value.toString());
      buff.writeInt(12);
      buff.writeLong(timetz.toLocalTime().toNanoOfDay() / 1000);
      // zone offset in seconds (should we change it to UTC ?)
      buff.writeInt(-timetz.getOffset().getTotalSeconds());
    }
  };
  public static DataType<Object> TIMETZ_ARRAY = new DataType<>(Object.class,1270);

  // 8 bytes date and time without time zone
  public static final DataType<Object> TIMESTAMP = new DataType<Object>(Object.class,1114) {
    final LocalDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDateTime.parse(cs, TIMESTAMP_FORMAT).toString();
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      LocalDateTime timestamp = PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
      return timestamp.toString();
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      LocalDateTime timestamp = LocalDateTime.parse(value.toString());
      buff.writeInt(8);
      buff.writeLong(-timestamp.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static DataType<Object> TIMESTAMP_ARRAY = new DataType<>(Object.class,1115);

  // 8 bytes date and time with time zone
  public static final DataType<Object> TIMESTAMPTZ = new DataType<Object>(Object.class,1184) {
    final OffsetDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetDateTime.parse(cs, TIMESTAMPTZ_FORMAT).toInstant().toString();
    }
    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      OffsetDateTime timestamptz = PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
      return timestamptz.toString();
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      OffsetDateTime timestamptz = OffsetDateTime.parse(value.toString()).withOffsetSameInstant(ZoneOffset.UTC);
      buff.writeInt(8);
      buff.writeLong(-timestamptz.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static DataType<Object> TIMESTAMPTZ_ARRAY = new DataType<>(Object.class,1185);
  // 16 bytes time interval
  public static DataType<Object> INTERVAL = new DataType<>(Object.class,1186);
  public static DataType<Object> INTERVAL_ARRAY = new DataType<>(Object.class,1187);

  // 1 or 4 bytes plus the actual binary string
  public static final DataType<byte[]> BYTEA = new DataType<byte[]>(byte[].class, 17) {
    @Override
    public byte[] decodeText(int len, ByteBuf buff) {
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
    @Override
    public void encodeText(byte[] value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.setByte(index + 4, '\\');
      buff.setByte(index + 5, 'x');
      // todo : optimize - no need to create an intermediate string here
      int len = buff.setCharSequence(index + 6, printHexBinary(value), StandardCharsets.UTF_8);
      buff.writeInt(2 + len);
      buff.writerIndex(index + 2 + len);
    }
  };
  public static DataType<Object> BYTEA_ARRAY = new DataType<>(Object.class,1001);

  // 6 bytes MAC address (XX:XX:XX:XX:XX:XX)
  public static DataType<Object> MACADDR = new DataType<>(Object.class,829);

  // 7 or 19 bytes (IPv4 and IPv6 hosts and networks)
  public static DataType<Object> INET = new DataType<>(Object.class,869);

  // 7 or 19 bytes (IPv4 and IPv6 networks)
  public static DataType<Object> CIDR = new DataType<>(Object.class,650);

  // 8 bytes MAC address (XX:XX:XX:XX:XX:XX:XX:XX)
  public static DataType<Object> MACADDR8 = new DataType<>(Object.class,774);

  // UUID
  public static DataType<String> UUID = new DataType<String>(String.class, 2950) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
  };
  public static DataType<Object> UUID_ARRAY = new DataType<>(Object.class,2951);

  // Text JSON
  public static final DataType<Object> JSON = new DataType<Object>(Object.class,114) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return decodeJson(cs.toString());
    }
  };
  // Binary JSON
  public static final DataType<Object> JSONB = new DataType<Object>(Object.class,3802) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Not sure this is correct
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return decodeJson(cs.toString());
    }
  };
  // XML
  public static DataType<Object> XML = new DataType<>(Object.class,142);
  public static DataType<Object> XML_ARRAY = new DataType<>(Object.class,143);

  // Geometric point (x, y)
  public static DataType<Object> POINT = new DataType<>(Object.class,600);
  // Geometric box (lower left, upper right)
  public static DataType<Object> BOX = new DataType<>(Object.class,603);
  public static DataType<Object> HSTORE = new DataType<>(Object.class,33670);

  // Object identifier
  public static DataType<Object> OID = new DataType<>(Object.class,26);
  public static DataType<Object> OID_ARRAY = new DataType<>(Object.class,1028);
  public static DataType<Object> VOID = new DataType<>(Object.class,2278);
  public static DataType<Object> UNKNOWN = new DataType<>(Object.class,705);

  private static long decodeInt(int len, ByteBuf buff) {
    long value = 0;
    for (int i = 0;i < len;i++) {
      value = value * 10 + (buff.readUnsignedByte() -'0'); // HOT
    }
    return value;
  }

  private static Object decodeJson(String value) {
    int pos = 0;
    while (pos < value.length() && Character.isWhitespace(value.charAt(pos))) {
      pos++;
    }
    if (pos == value.length()) {
      return null;
    } else if (value.charAt(pos) == '{') {
      return new JsonObject(value);
    } else if (value.charAt(pos) == '[') {
      return new JsonArray(value);
    } else {
      try {
        JsonNode jsonNode = Json.mapper.readTree(value);
        if (jsonNode.isNumber()) {
          return jsonNode.numberValue();
        } else if (jsonNode.isBoolean()) {
          return jsonNode.booleanValue();
        } else if (jsonNode.isTextual()) {
          return jsonNode.textValue();
        }
      } catch (IOException e) {
        // do nothing
      }
    }
    return null;
  }

  private static IntObjectMap<DataType> oidToDataType = new IntObjectHashMap<>();

  static {
    DataType<?>[] all = {
      BOOL, BOOL_ARRAY,
      INT2, INT2_ARRAY, INT4, INT4_ARRAY, INT8, INT8_ARRAY,
      FLOAT4, FLOAT4_ARRAY, FLOAT8, FLOAT8_ARRAY,
      NUMERIC, NUMERIC_ARRAY,
      MONEY, MONEY_ARRAY,
      BITS, BIT_ARRAY,
      VARBIT, VARBIT_ARRAY,
      CHAR, CHAR_ARRAY,
      VARCHAR, VARCHAR_ARRAY,
      BPCHAR, BPCHAR_ARRAY,
      TEXT, TEXT_ARRAY,
      NAME, NAME_ARRAY,
      DATE, DATE_ARRAY,
      TIME, TIME_ARRAY, TIMETZ, TIMETZ_ARRAY,
      TIMESTAMP, TIMESTAMP_ARRAY, TIMESTAMPTZ, TIMESTAMPTZ_ARRAY,
      INTERVAL, INTERVAL_ARRAY,
      BYTEA, BYTEA_ARRAY,
      MACADDR, INET, CIDR, MACADDR8,
      UUID, UUID_ARRAY,
      JSON, JSONB,
      XML, XML_ARRAY,
      POINT, BOX,
      HSTORE,
      OID, OID_ARRAY,
      VOID,
      UNKNOWN
    };
    for (DataType<?> dataType : all) {
      oidToDataType.put(dataType.id, dataType);
    }
  }

  private final Class<T> javaType;
  private final int id;

  private DataType(Class<T> javaType, int id) {
    this.javaType = javaType;
    this.id = id;
  }

  public Class<T> getJavaType() {
    return javaType;
  }

  public static DataType valueOf(int id) {
    DataType value = oidToDataType.get(id);
    return value != null ? value : DataType.UNKNOWN;
  }

  public T decodeText(int len, ByteBuf buff) {
    // Default to null
    buff.readerIndex(buff.readerIndex() + len);
    return null;
  }

  public T decodeBinary(int len, ByteBuf buff) {
    // Default to null
    buff.readerIndex(buff.readerIndex() + len);
    return null;
  }

  public void encodeBinary(T value, ByteBuf buff) {
    // Default to null
    buff.writeInt(-1);
    System.out.println("Data type " + id + " does not support binary encoding");
  }

  public void encodeText(T value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = String.valueOf(value);
    int len = buff.setCharSequence(index + 4, s, StandardCharsets.UTF_8);
    buff.writeInt(len);
    buff.writerIndex(index + 4 + len);
  }
}
