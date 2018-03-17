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

package com.julienviet.pgclient.impl.codec;

import com.fasterxml.jackson.databind.JsonNode;
import com.julienviet.pgclient.Numeric;
import io.netty.buffer.ByteBuf;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;

import static com.julienviet.pgclient.impl.codec.formatter.DateTimeFormatter.*;
import static com.julienviet.pgclient.impl.codec.formatter.TimeFormatter.*;
import static javax.xml.bind.DatatypeConverter.*;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataType<T> {

  public interface Encoder<T> {
    void encode(T value, ByteBuf buff);
  }

  public interface Decoder<T> {
    T decode(int len, ByteBuf buff);
  }

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
      return (short)DataType.decodeDecStringToLong(len, buff);
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
      return (int)DataType.decodeDecStringToLong(len, buff);
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
      return DataType.decodeDecStringToLong(len, buff);
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
      return Numeric.parse(cs.toString());
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
    @Override
    public void encodeBinary(String value, ByteBuf buff) {
      super.encodeText(value, buff);
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
    @Override
    public void encodeBinary(String value, ByteBuf buff) {
      super.encodeText(value, buff);
    }
  };
  public static DataType<Object> NAME_ARRAY = new DataType<>(Object.class,1003);

  // 4 bytes date (no time of day)
  public static DataType<LocalDate> DATE = new DataType<LocalDate>(LocalDate.class,1082) {
    final LocalDate PG_EPOCH = LocalDate.of(2000, 1, 1);
    @Override
    public LocalDate decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDate.parse(cs);
    }
    @Override
    public LocalDate decodeBinary(int len, ByteBuf buff) {
      return PG_EPOCH.plus(buff.readInt(), ChronoUnit.DAYS);
    }
    @Override
    public void encodeBinary(LocalDate value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeInt((int) -value.until(PG_EPOCH, ChronoUnit.DAYS));
    }
  };
  public static DataType<Object> DATE_ARRAY = new DataType<>(Object.class,1182);

  // 8 bytes time of day (no date) without time zone
  public static DataType<LocalTime> TIME = new DataType<LocalTime>(LocalTime.class,1083) {
    @Override
    public LocalTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalTime.parse(cs);
    }
    @Override
    public LocalTime decodeBinary(int len, ByteBuf buff) {
      // micros to nanos
      return LocalTime.ofNanoOfDay(buff.readLong() * 1000);
    }
    @Override
    public void encodeBinary(LocalTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(value.getLong(ChronoField.MICRO_OF_DAY));
    }
  };
  public static DataType<Object> TIME_ARRAY = new DataType<>(Object.class,1183);

  // 12 bytes time of day (no date) with time zone
  public static final DataType<OffsetTime> TIMETZ = new DataType<OffsetTime>(OffsetTime.class,1266) {
    @Override
    public OffsetTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetTime.parse(cs, TIMETZ_FORMAT);
    }
    @Override
    public OffsetTime decodeBinary(int len, ByteBuf buff) {
      // micros to nanos
      return OffsetTime.of(LocalTime.ofNanoOfDay(buff.readLong() * 1000),
        // zone offset in seconds (should we change it to UTC ?)
        ZoneOffset.ofTotalSeconds(-buff.readInt()));
    }
    @Override
    public void encodeBinary(OffsetTime value, ByteBuf buff) {
      buff.writeInt(12);
      buff.writeLong(value.toLocalTime().getLong(ChronoField.MICRO_OF_DAY));
      // zone offset in seconds (should we change it to UTC ?)
      buff.writeInt(-value.getOffset().getTotalSeconds());
    }
  };
  public static DataType<Object> TIMETZ_ARRAY = new DataType<>(Object.class,1270);

  // 8 bytes date and time without time zone
  public static final DataType<LocalDateTime> TIMESTAMP = new DataType<LocalDateTime>(LocalDateTime.class,1114) {
    final LocalDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    @Override
    public LocalDateTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDateTime.parse(cs, TIMESTAMP_FORMAT);
    }
    @Override
    public LocalDateTime decodeBinary(int len, ByteBuf buff) {
      return PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
    }
    @Override
    public void encodeBinary(LocalDateTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(-value.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static DataType<Object> TIMESTAMP_ARRAY = new DataType<>(Object.class,1115);

  // 8 bytes date and time with time zone
  public static final DataType<OffsetDateTime> TIMESTAMPTZ = new DataType<OffsetDateTime>(OffsetDateTime.class,1184) {
    final OffsetDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);
    @Override
    public OffsetDateTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetDateTime.parse(cs, TIMESTAMPTZ_FORMAT);
    }
    @Override
    public OffsetDateTime decodeBinary(int len, ByteBuf buff) {
      return PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
    }
    @Override
    public void encodeBinary(OffsetDateTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(-value.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static DataType<Object> TIMESTAMPTZ_ARRAY = new DataType<>(Object.class,1185);
  // 16 bytes time interval
  public static DataType<Object> INTERVAL = new DataType<>(Object.class,1186);
  public static DataType<Object> INTERVAL_ARRAY = new DataType<>(Object.class,1187);

  // 1 or 4 bytes plus the actual binary string
  public static final DataType<Buffer> BYTEA = new DataType<Buffer>(Buffer.class, 17) {
    @Override
    public Buffer decodeText(int len, ByteBuf buff) {
      buff.readByte(); // \
      buff.readByte(); // x
      return Buffer.buffer(DataType.decodeHexStringToBytes(len - 2, buff));
    }
    @Override
    public void encodeText(Buffer value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.setByte(index + 4, '\\');
      buff.setByte(index + 5, 'x');
      // todo : optimize - no need to create an intermediate string here
      int len = buff.setCharSequence(index + 6, printHexBinary(value.getBytes()), StandardCharsets.UTF_8);
      buff.writeInt(2 + len);
      buff.writerIndex(index + 2 + len);
    }
    @Override
    public Buffer decodeBinary(int len, ByteBuf buff) {
      return Buffer.buffer(buff.readBytes(len));
    }
    @Override
    public void encodeBinary(Buffer value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.writeInt(0);
      ByteBuf byteBuf = value.getByteBuf();
      int len = byteBuf.readableBytes();
      buff.writeBytes(byteBuf);
      buff.setInt(index, len);
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
  public static DataType<java.util.UUID> UUID = new DataType<java.util.UUID>(java.util.UUID.class, 2950) {
    @Override
    public java.util.UUID decodeText(int len, ByteBuf buff) {
      return java.util.UUID.fromString(buff.readCharSequence(len, StandardCharsets.UTF_8).toString());
    }
    @Override
    public java.util.UUID decodeBinary(int len, ByteBuf buff) {
      return new java.util.UUID(buff.readLong(), buff.readLong());
    }
    @Override
    public void encodeBinary(java.util.UUID uuid, ByteBuf buff) {
      buff.writeInt(16);
      buff.writeLong(uuid.getMostSignificantBits());
      buff.writeLong(uuid.getLeastSignificantBits());
    }
  };
  public static DataType<Object> UUID_ARRAY = new DataType<>(Object.class,2951);

  public static final DataType<Object> JSON = new DataType<Object>(Object.class,114) {
    @Override
    public Object decodeText(int len, ByteBuf buff) {
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return decodeJson(cs.toString());
    }

    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      if(JsonObject.class.equals(value.getClass())) {
        int index = buff.writerIndex();
        buff.writeInt(0);
        ByteBuf byteBuf = ((JsonObject) value).toBuffer().getByteBuf();
        int len = byteBuf.readableBytes();
        buff.writeBytes(byteBuf);
        buff.setInt(index, len);

      } else if (String.class.equals(value.getClass())) {
        byte[] bytes = ((String)value).getBytes();
        buff.writeInt(bytes.length);
        buff.writeBytes(bytes);
      } else{
        super.encodeBinary(value, buff);
      }
    }

    @Override
    public Object decodeBinary(int len, ByteBuf buff) {
      if (len == 0) {
        return null;
      }
      byte[] jsonBytes = new byte[len];
      buff.readBytes(jsonBytes, 0, len);
      // Could use improvement as we're creating a String from the bytes
      return new JsonObject(new String(jsonBytes, StandardCharsets.UTF_8));
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

  /**
   * Decode the specified {@code buff} formatted as a decimal string starting at the readable index
   * with the specified {@code length} to a long.
   *
   * @param len the hex string length
   * @param buff the byte buff to read from
   * @return the decoded value as a long
   */
  private static long decodeDecStringToLong(int len, ByteBuf buff) {
    long value = 0;
    for (int i = 0;i < len;i++) {
      byte ch = buff.readByte();
      byte nibble = (byte)(ch - '0');
      value = value * 10 + nibble;
    }
    return value;
  }

  /**
   * Decode the specified {@code buff} formatted as an hex string starting at the buffer readable index
   * with the specified {@code length} to a byte array.
   *
   * @param len the hex string length
   * @param buff the byte buff to read from
   * @return the decoded value as a byte array
   */
  private static byte[] decodeHexStringToBytes(int len, ByteBuf buff) {
    len = len >> 1;
    byte[] bytes = new byte[len];
    for (int i = 0;i < len;i++) {
      byte b0 = decodeHexChar(buff.readByte());
      byte b1 = decodeHexChar(buff.readByte());
      bytes[i] = (byte)(b0 * 16 + b1);
    }
    return bytes;
  }

  private static byte decodeHexChar(byte ch) {
    return (byte)(((ch & 0x1F) + ((ch >> 6) * 0x19) - 0x10) & 0x0F);
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
  public final Encoder<T> textEncoder;
  public final Decoder<T> textDecoder;
  public final Encoder<T> binaryEncoder;
  public final Decoder<T> binaryDecoder;

  private DataType(Class<T> javaType, int id) {
    this.javaType = javaType;
    this.id = id;
    this.textDecoder = this::decodeText;
    this.textEncoder = this::encodeText;
    this.binaryDecoder = this::decodeBinary;
    this.binaryEncoder = this::encodeBinary;
  }

  public DataType(Class<T> javaType, int id,
                  Encoder<T> textEncoder,
                  Decoder<T> textDecoder,
                  Encoder<T> binaryEncoder,
                  Decoder<T> binaryDecoder) {
    this.javaType = javaType;
    this.id = id;
    this.textEncoder = textEncoder;
    this.textDecoder = textDecoder;
    this.binaryEncoder = binaryEncoder;
    this.binaryDecoder = binaryDecoder;
  }

  public Class<T> getJavaType() {
    return javaType;
  }

  public static DataType valueOf(int id) {
    DataType value = oidToDataType.get(id);
    return value != null ? value : DataType.UNKNOWN;
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

  public T decodeText(int len, ByteBuf buff) {
    // Default to null
    buff.readerIndex(buff.readerIndex() + len);
    return null;
  }

  public void encodeText(T value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = String.valueOf(value);
    buff.writeInt(0); // Undetermined yet
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len);
  }
}
