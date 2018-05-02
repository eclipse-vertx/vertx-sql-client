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

import com.fasterxml.jackson.databind.JsonNode;
import io.netty.buffer.ByteBuf;
import io.reactiverse.pgclient.Json;
import io.reactiverse.pgclient.Numeric;
import io.reactiverse.pgclient.impl.codec.formatter.DateTimeFormatter;
import io.reactiverse.pgclient.impl.codec.formatter.TimeFormatter;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.*;
import java.time.temporal.ChronoField;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.IntFunction;

public class DataTypeCodec {

  private static final String[] empty_string_array = new String[0];
  private static final LocalDate[] empty_local_date_array = new LocalDate[0];
  private static final LocalTime[] empty_local_time_array = new LocalTime[0];
  private static final OffsetTime[] empty_offset_time_array = new OffsetTime[0];
  private static final LocalDateTime[] empty_local_date_time_array = new LocalDateTime[0];
  private static final OffsetDateTime[] empty_offset_date_time_array = new OffsetDateTime[0];
  private static final Buffer[] empty_buffer_array = new Buffer[0];
  private static final UUID[] empty_uuid_array = new UUID[0];
  private static final Numeric[] empty_numeric_array = new Numeric[0];
  private static final Boolean[] empty_boolean_array = new Boolean[0];
  private static final Integer[] empty_integer_array = new Integer[0];
  private static final Short[] empty_short_array = new Short[0];
  private static final Long[] empty_long_array = new Long[0];
  private static final Float[] empty_float_array = new Float[0];
  private static final Double[] empty_double_array = new Double[0];
  private static final Character[] empty_character_array = new Character[0];
  private static final LocalDate LOCAL_DATE_EPOCH = LocalDate.of(2000, 1, 1);
  private static final LocalDateTime LOCAL_DATE_TIME_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
  private static final OffsetDateTime OFFSET_DATE_TIME_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);

  // Sentinel used when an object is refused by the data type
  public static final Object REFUSED_SENTINEL = new Object();

  private static final IntFunction<Boolean[]> BOOLEAN_ARRAY_FACTORY = size -> size == 0 ? empty_boolean_array : new Boolean[size];
  private static final IntFunction<Short[]> SHORT_ARRAY_FACTORY = size -> size == 0 ? empty_short_array : new Short[size];
  private static final IntFunction<Integer[]> INTEGER_ARRAY_FACTORY = size -> size == 0 ? empty_integer_array : new Integer[size];
  private static final IntFunction<Long[]> LONG_ARRAY_FACTORY = size -> size == 0 ? empty_long_array : new Long[size];
  private static final IntFunction<Float[]> FLOAT_ARRAY_FACTORY = size -> size == 0 ? empty_float_array : new Float[size];
  private static final IntFunction<Double[]> DOUBLE_ARRAY_FACTORY = size -> size == 0 ? empty_double_array : new Double[size];
  private static final IntFunction<Character[]> CHARACTER_ARRAY_FACTORY = size -> size == 0 ? empty_character_array : new Character[size];
  private static final IntFunction<String[]> STRING_ARRAY_FACTORY = size -> size == 0 ? empty_string_array : new String[size];
  private static final IntFunction<LocalDate[]> LOCALDATE_ARRAY_FACTORY = size -> size == 0 ? empty_local_date_array : new LocalDate[size];
  private static final IntFunction<LocalTime[]> LOCALTIME_ARRAY_FACTORY = size -> size == 0 ? empty_local_time_array : new LocalTime[size];
  private static final IntFunction<OffsetTime[]> OFFSETTIME_ARRAY_FACTORY = size -> size == 0 ? empty_offset_time_array : new OffsetTime[size];
  private static final IntFunction<LocalDateTime[]> LOCALDATETIME_ARRAY_FACTORY = size -> size == 0 ? empty_local_date_time_array : new LocalDateTime[size];
  private static final IntFunction<OffsetDateTime[]> OFFSETDATETIME_ARRAY_FACTORY = size -> size == 0 ? empty_offset_date_time_array : new OffsetDateTime[size];
  private static final IntFunction<Buffer[]> BUFFER_ARRAY_FACTORY =size -> size == 0 ? empty_buffer_array : new Buffer[size];
  private static final IntFunction<UUID[]> UUID_ARRAY_FACTORY = size -> size == 0 ? empty_uuid_array : new UUID[size];
  private static final IntFunction<Numeric[]> NUMERIC_ARRAY_FACTORY = size -> size == 0 ? empty_numeric_array : new Numeric[size];

  public static void encodeText(DataType id, Object value, ByteBuf buff) {
    int index = buff.writerIndex();
    buff.writeInt(0);
    textEncode(id, value, buff);
    buff.setInt(index, buff.writerIndex() - index - 4);
  }

  private static void textEncode(DataType id, Object value, ByteBuf buff) {
    switch (id) {
      case NUMERIC:
        textEncodeNUMERIC((Numeric) value, buff);
        break;
      case NUMERIC_ARRAY:
        textEncodeNUMERIC_ARRAY((Numeric[]) value, buff);
        break;
      default:
        System.out.println("Data type " + id + " does not support text encoding");
        buff.writeCharSequence(String.valueOf(value), StandardCharsets.UTF_8);
        break;
    }
  }

  public static void encodeBinary(DataType id, Object value, ByteBuf buff) {
    switch (id) {
      case BOOL:
        binaryEncodeBOOL((Boolean) value, buff);
        break;
      case BOOL_ARRAY:
        binaryEncodeArray((Boolean[]) value, DataType.BOOL, buff);
        break;
      case INT2:
        binaryEncodeINT2((Short) value, buff);
        break;
      case INT2_ARRAY:
        binaryEncodeArray((Short[]) value, DataType.INT2, buff);
        break;
      case INT4:
        binaryEncodeINT4((Integer) value, buff);
        break;
      case INT4_ARRAY:
        binaryEncodeArray((Integer[]) value, DataType.INT4, buff);
        break;
      case INT8:
        binaryEncodeINT8((Long) value, buff);
        break;
      case INT8_ARRAY:
        binaryEncodeArray((Long[]) value, DataType.INT8, buff);
        break;
      case FLOAT4:
        binaryEncodeFLOAT4((Float) value, buff);
        break;
      case FLOAT4_ARRAY:
        binaryEncodeArray((Float[]) value, DataType.FLOAT4, buff);
        break;
      case FLOAT8:
        binaryEncodeFLOAT8((Double) value, buff);
        break;
      case FLOAT8_ARRAY:
        binaryEncodeArray((Double[]) value, DataType.FLOAT8, buff);
        break;
      case CHAR:
        binaryEncodeCHAR((Character) value, buff);
        break;
      case CHAR_ARRAY:
        binaryEncodeArray((Character[]) value, DataType.CHAR, buff);
        break;
      case VARCHAR:
        binaryEncodeVARCHAR((String) value, buff);
        break;
      case VARCHAR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.VARCHAR, buff);
        break;
      case BPCHAR:
        binaryEncodeBPCHAR((String) value, buff);
        break;
      case BPCHAR_ARRAY:
        binaryEncodeArray((String[]) value, DataType.BPCHAR, buff);
        break;
      case TEXT:
        binaryEncodeTEXT((String) value, buff);
        break;
      case TEXT_ARRAY:
        binaryEncodeArray((String[]) value, DataType.TEXT, buff);
        break;
      case NAME:
        binaryEncodeNAME((String) value, buff);
        break;
      case NAME_ARRAY:
        binaryEncodeArray((String[]) value, DataType.NAME, buff);
        break;
      case DATE:
        binaryEncodeDATE((LocalDate) value, buff);
        break;
      case DATE_ARRAY:
        binaryEncodeArray((LocalDate[]) value, DataType.DATE, buff);
        break;
      case TIME:
        binaryEncodeTIME((LocalTime) value, buff);
        break;
      case TIME_ARRAY:
        binaryEncodeArray((LocalTime[]) value, DataType.TIME, buff);
        break;
      case TIMETZ:
        binaryEncodeTIMETZ((OffsetTime) value, buff);
        break;
      case TIMETZ_ARRAY:
        binaryEncodeArray((OffsetTime[]) value, DataType.TIMETZ, buff);
        break;
      case TIMESTAMP:
        binaryEncodeTIMESTAMP((LocalDateTime) value, buff);
        break;
      case TIMESTAMP_ARRAY:
        binaryEncodeArray((LocalDateTime[]) value, DataType.TIMESTAMP, buff);
        break;
      case TIMESTAMPTZ:
        binaryEncodeTIMESTAMPTZ((OffsetDateTime) value, buff);
        break;
      case TIMESTAMPTZ_ARRAY:
        binaryEncodeArray((OffsetDateTime[]) value, DataType.TIMESTAMPTZ, buff);
        break;
      case BYTEA:
        binaryEncodeBYTEA((Buffer) value, buff);
        break;
      case BYTEA_ARRAY:
        binaryEncodeArray((Buffer[]) value, DataType.BYTEA, buff);
        break;
      case UUID:
        binaryEncodeUUID((UUID) value, buff);
        break;
      case UUID_ARRAY:
        binaryEncodeArray((UUID[]) value, DataType.UUID, buff);
        break;
      case JSON:
        binaryEncodeJSON((Json) value, buff);
        break;
      case JSONB:
        binaryEncodeJSONB((Json) value, buff);
        break;
      default:
        System.out.println("Data type " + id + " does not support binary encoding");
        defaultEncodeBinary(value, buff);
        break;
    }
  }

  public static Object decodeBinary(DataType id, int len, ByteBuf buff) {
    switch (id) {
      case BOOL:
        return binaryDecodeBOOL(len, buff);
      case BOOL_ARRAY:
        return binaryDecodeArray(BOOLEAN_ARRAY_FACTORY, DataType.BOOL, len, buff);
      case INT2:
        return binaryDecodeINT2(len, buff);
      case INT2_ARRAY:
        return binaryDecodeArray(SHORT_ARRAY_FACTORY, DataType.INT2, len, buff);
      case INT4:
        return binaryDecodeINT4(len, buff);
      case INT4_ARRAY:
        return binaryDecodeArray(INTEGER_ARRAY_FACTORY, DataType.INT4, len, buff);
      case INT8:
        return binaryDecodeINT8(len, buff);
      case INT8_ARRAY:
        return binaryDecodeArray(LONG_ARRAY_FACTORY, DataType.INT8, len, buff);
      case FLOAT4:
        return binaryDecodeFLOAT4(len, buff);
      case FLOAT4_ARRAY:
        return binaryDecodeArray(FLOAT_ARRAY_FACTORY, DataType.FLOAT4, len, buff);
      case FLOAT8:
        return binaryDecodeFLOAT8(len, buff);
      case FLOAT8_ARRAY:
        return binaryDecodeArray(DOUBLE_ARRAY_FACTORY, DataType.FLOAT8, len, buff);
      case CHAR:
        return binaryDecodeCHAR(len, buff);
      case CHAR_ARRAY:
        return binaryDecodeArray(CHARACTER_ARRAY_FACTORY, DataType.CHAR, len, buff);
      case VARCHAR:
        return binaryDecodeVARCHAR(len, buff);
      case VARCHAR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.VARCHAR, len, buff);
      case BPCHAR:
        return binaryDecodeBPCHAR(len, buff);
      case BPCHAR_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.BPCHAR, len, buff);
      case TEXT:
        return binaryDecodeTEXT(len, buff);
      case TEXT_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.TEXT, len, buff);
      case NAME:
        return binaryDecodeNAME(len, buff);
      case NAME_ARRAY:
        return binaryDecodeArray(STRING_ARRAY_FACTORY, DataType.NAME, len, buff);
      case DATE:
        return binaryDecodeDATE(len, buff);
      case DATE_ARRAY:
        return binaryDecodeArray(LOCALDATE_ARRAY_FACTORY, DataType.DATE, len, buff);
      case TIME:
        return binaryDecodeTIME(len, buff);
      case TIME_ARRAY:
        return binaryDecodeArray(LOCALTIME_ARRAY_FACTORY, DataType.TIME, len, buff);
      case TIMETZ:
        return binaryDecodeTIMETZ(len, buff);
      case TIMETZ_ARRAY:
        return binaryDecodeArray(OFFSETTIME_ARRAY_FACTORY, DataType.TIMETZ, len, buff);
      case TIMESTAMP:
        return binaryDecodeTIMESTAMP(len, buff);
      case TIMESTAMP_ARRAY:
        return binaryDecodeArray(LOCALDATETIME_ARRAY_FACTORY, DataType.TIMESTAMP, len, buff);
      case TIMESTAMPTZ:
        return binaryDecodeTIMESTAMPTZ(len, buff);
      case TIMESTAMPTZ_ARRAY:
        return binaryDecodeArray(OFFSETDATETIME_ARRAY_FACTORY, DataType.TIMESTAMPTZ, len, buff);
      case BYTEA:
        return binaryDecodeBYTEA(len, buff);
      case BYTEA_ARRAY:
        return binaryDecodeArray(BUFFER_ARRAY_FACTORY, DataType.BYTEA, len, buff);
      case UUID:
        return binaryDecodeUUID(len, buff);
      case UUID_ARRAY:
        return binaryDecodeArray(UUID_ARRAY_FACTORY, DataType.UUID, len, buff);
      case JSON:
        return binaryDecodeJSON(len, buff);
      case JSONB:
        return binaryDecodeJSONB(len, buff);
      default:
        System.out.println("Data type " + id + " does not support binary decoding");
        return defaultDecodeBinary(len, buff);
    }
  }

  public static Object decodeText(DataType id, int len, ByteBuf buff) {
    switch (id) {
      case BOOL:
        return textDecodeBOOL(len, buff);
      // case BOOL_ARRAY:
      //   return textDecodeBOOL_ARRAY(len, buff);
      case INT2:
        return textDecodeINT2(len, buff);
      // case INT2_ARRAY:
      //   return textDecodeINT2_ARRAY(len, buff);
      case INT4:
        return textDecodeINT4(len, buff);
      // case INT4_ARRAY:
      //   return textDecodeINT4_ARRAY(len, buff);
      case INT8:
        return textDecodeINT8(len, buff);
      // case INT8_ARRAY:
      //   return textDecodeINT8_ARRAY(len, buff);
      case FLOAT4:
        return textDecodeFLOAT4(len, buff);
      // case FLOAT4_ARRAY:
      //   return textDecodeFLOAT4_ARRAY(len, buff);
      case FLOAT8:
        return textDecodeFLOAT8(len, buff);
      // case FLOAT8_ARRAY:
      //   return textDecodeFLOAT8_ARRAY(len, buff);
      case CHAR:
        return textDecodeCHAR(len, buff);
      // case CHAR_ARRAY:
      //   return textDecodeCHAR_ARRAY(len, buff);
      case VARCHAR:
        return textDecodeVARCHAR(len, buff);
      // case VARCHAR_ARRAY:
      //   return binaryDecodeArray(STRING_ARRAY_FACTORY, DataTypeConstants.VARCHAR, len, buff);
      case BPCHAR:
        return textDecodeBPCHAR(len, buff);
      // case BPCHAR_ARRAY:
      //   return binaryDecodeArray(STRING_ARRAY_FACTORY, DataTypeConstants.BPCHAR, len, buff);
      case TEXT:
        return textdecodeTEXT(len, buff);
      // case TEXT_ARRAY:
      //   return binaryDecodeArray(STRING_ARRAY_FACTORY, DataTypeConstants.TEXT, len, buff);
      case NAME:
        return textDecodeNAME(len, buff);
      // case NAME_ARRAY:
      //   return binaryDecodeArray(STRING_ARRAY_FACTORY, DataTypeConstants.NAME, len, buff);
      case DATE:
        return textDecodeDATE(len, buff);
      // case DATE_ARRAY:
      //   return binaryDecodeArray(LOCALDATE_ARRAY_FACTORY, DataTypeConstants.DATE, len, buff);
      case TIME:
        return textDecodeTIME(len, buff);
      // case TIME_ARRAY:
      //   return binaryDecodeArray(LOCALTIME_ARRAY_FACTORY, DataTypeConstants.TIME, len, buff);
      case TIMETZ:
        return textDecodeTIMETZ(len, buff);
      // case TIMETZ_ARRAY:
      //   return binaryDecodeArray(OFFSETTIME_ARRAY_FACTORY, DataTypeConstants.TIMETZ, len, buff);
      case TIMESTAMP:
        return textDecodeTIMESTAMP(len, buff);
      // case TIMESTAMP_ARRAY:
      //   return binaryDecodeArray(LOCALDATETIME_ARRAY_FACTORY, DataTypeConstants.TIMESTAMP, len, buff);
      case TIMESTAMPTZ:
        return textDecodeTIMESTAMPTZ(len, buff);
      // case TIMESTAMPTZ_ARRAY:
      //   return binaryDecodeArray(OFFSETDATETIME_ARRAY_FACTORY, DataTypeConstants.TIMESTAMPTZ, len, buff);
      case BYTEA:
        return textDecodeBYTEA(len, buff);
      // case BYTEA_ARRAY:
      //   return binaryDecodeArray(BUFFER_ARRAY_FACTORY, DataTypeConstants.BYTEA, len, buff);
      case UUID:
        return textDecodeUUID(len, buff);
      // case UUID_ARRAY:
      //   return binaryDecodeArray(UUID_ARRAY_FACTORY, DataTypeConstants.UUID, len, buff);
      case NUMERIC:
        return textDecodeNUMERIC(len, buff);
      case NUMERIC_ARRAY:
        return textDecodeArray(NUMERIC_ARRAY_FACTORY, DataType.NUMERIC, len, buff);
      case JSON:
        return textDecodeJSON(len, buff);
      case JSONB:
         return textDecodeJSONB(len, buff);
      default:
        System.out.println("Data type " + id + " does not support text decoding");
        return defaultDecodeText(len, buff);
    }
  }

  public static Object prepare(DataType type, Object value) {
    switch (type) {
      case JSON:
      case JSONB:
        if (value instanceof Json) {
          return value;
        } else if (value instanceof String || value instanceof Boolean || value instanceof Number) {
          return Json.create(value);
        } else {
          return REFUSED_SENTINEL;
        }
      default:
        Class<?> javaType = type.type;
        return javaType.isInstance(value) ? javaType.cast(value) : REFUSED_SENTINEL;
    }
  }

  private static Object defaultDecodeText(int len, ByteBuf buff) {
    // Default to null
    buff.skipBytes(len);
    return null;
  }

  private static void defaultEncodeBinary(Object value, ByteBuf buff) {
    // Default to null
    buff.writeInt(-1);
  }

  private static Object defaultDecodeBinary(int len, ByteBuf buff) {
    // Default to null
    buff.skipBytes(len);
    return null;
  }

  private static void binaryEncodeBOOL(Boolean value, ByteBuf buff) {
    buff.writeInt(1);
    buff.writeBoolean(value);
  }

  private static Boolean binaryDecodeBOOL(int len, ByteBuf buff) {
    return buff.readBoolean();
  }

  private static Boolean textDecodeBOOL(int len, ByteBuf buff) {
    if(buff.readByte() == 't') {
      return Boolean.TRUE;
    } else {
      return Boolean.FALSE;
    }
  }

  private static Short textDecodeINT2(int len, ByteBuf buff) {
    return (short) DataTypeCodec.decodeDecStringToLong(len, buff);
  }

  private static Short binaryDecodeINT2(int len, ByteBuf buff) {
    return buff.readShort();
  }

  private static void binaryEncodeINT2(Short value, ByteBuf buff) {
    buff.writeInt(2);
    buff.writeShort(value);
  }

  private static Integer textDecodeINT4(int len, ByteBuf buff) {
    return (int) decodeDecStringToLong(len, buff);
  }

  private static Integer binaryDecodeINT4(int len, ByteBuf buff) {
    return buff.readInt();
  }

  private static void binaryEncodeINT4(Integer value, ByteBuf buff) {
    buff.writeInt(4);
    buff.writeInt(value);
  }

  private static Long textDecodeINT8(int len, ByteBuf buff) {
    return decodeDecStringToLong(len, buff);
  }

  private static Long binaryDecodeINT8(int len, ByteBuf buff) {
    return buff.readLong();
  }

  private static void binaryEncodeINT8(Long value, ByteBuf buff) {
    buff.writeInt(8);
    buff.writeLong(value);
  }

  private static Float textDecodeFLOAT4(int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return Float.parseFloat(cs.toString());
  }

  private static Float binaryDecodeFLOAT4(int len, ByteBuf buff) {
    return buff.readFloat();
  }

  private static void binaryEncodeFLOAT4(Float value, ByteBuf buff) {
    buff.writeInt(4);
    buff.writeFloat(value);
  }

  private static void binaryEncodeFLOAT8(Double value, ByteBuf buff) {
    buff.writeInt(8);
    buff.writeDouble(value);
  }

  private static Double binaryDecodeFLOAT8(int len, ByteBuf buff) {
    return buff.readDouble();
  }

  private static Double textDecodeFLOAT8(int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return Double.parseDouble(cs.toString());
  }

  private static Number textDecodeNUMERIC(int len, ByteBuf buff) {
    // Todo optimize that
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return Numeric.parse(cs.toString());
  }

  private static void textEncodeNUMERIC(Numeric value, ByteBuf buff) {
    String s = value.toString();
    buff.writeCharSequence(s, StandardCharsets.UTF_8);
  }

  private static void textEncodeNUMERIC_ARRAY(Numeric[] value, ByteBuf buff) {
    textEncodeArray(value, DataType.NUMERIC, buff);
  }

  private static void binaryEncodeCHAR(Character value, ByteBuf buff) {
    int index = buff.writerIndex();
    buff.writeInt(0);
    buff.writeChar(value);
    buff.setInt(index, buff.writerIndex() - 4 - index);
  }

  private static Character textDecodeCHAR(int len, ByteBuf buff) {
    return binaryDecodeCHAR(len, buff);
  }

  private static Character binaryDecodeCHAR(int len, ByteBuf buff) {
    return (char)buff.readByte();
  }

  private static void binaryEncodeVARCHAR(String value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = String.valueOf(value);
    buff.writeInt(0); // Undetermined yet
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len);
  }

  private static String textDecodeVARCHAR(int len, ByteBuf buff) {
    return binaryDecodeVARCHAR(len, buff);
  }

  private static String binaryDecodeVARCHAR(int len, ByteBuf buff) {
    return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
  }

  private static String textDecodeBPCHAR(int len, ByteBuf buff) {
    return binaryDecodeBPCHAR(len, buff);
  }

  private static void binaryEncodeBPCHAR(String value, ByteBuf buff) {
    int index = buff.writerIndex();
    buff.writeInt(0);
    buff.writeCharSequence(value, StandardCharsets.UTF_8);
    buff.setInt(index, buff.writerIndex() - 4 - index);
  }

  private static String binaryDecodeBPCHAR(int len, ByteBuf buff) {
    return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
  }

  private static String textdecodeTEXT(int len, ByteBuf buff) {
    return binaryDecodeTEXT(len, buff);
  }

  private static void binaryEncodeTEXT(String value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = String.valueOf(value);
    buff.writeInt(0); // Undetermined yet
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len);
  }

  private static String binaryDecodeTEXT(int len, ByteBuf buff) {
    return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
  }

  private static String textDecodeNAME(int len, ByteBuf buff) {
    return binaryDecodeNAME(len, buff);
  }


  private static void binaryEncodeNAME(String value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = String.valueOf(value);
    buff.writeInt(0); // Undetermined yet
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len);
  }

  private static String binaryDecodeNAME(int len, ByteBuf buff) {
    return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
  }

  private static void binaryEncodeDATE(LocalDate value, ByteBuf buff) {
    buff.writeInt(4);
    buff.writeInt((int) -value.until(LOCAL_DATE_EPOCH, ChronoUnit.DAYS));
  }

  private static LocalDate binaryDecodeDATE(int len, ByteBuf buff) {
    return LOCAL_DATE_EPOCH.plus(buff.readInt(), ChronoUnit.DAYS);
  }

  private static LocalDate textDecodeDATE(int len, ByteBuf buff) {
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return LocalDate.parse(cs);
  }

  private static void binaryEncodeTIME(LocalTime value, ByteBuf buff) {
    buff.writeInt(8);
    buff.writeLong(value.getLong(ChronoField.MICRO_OF_DAY));
  }

  private static LocalTime binaryDecodeTIME(int len, ByteBuf buff) {
    // micros to nanos
    return LocalTime.ofNanoOfDay(buff.readLong() * 1000);
  }

  private static LocalTime textDecodeTIME(int len, ByteBuf buff) {
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return LocalTime.parse(cs);
  }

  private static void binaryEncodeTIMETZ(OffsetTime value, ByteBuf buff) {
    buff.writeInt(12);
    buff.writeLong(value.toLocalTime().getLong(ChronoField.MICRO_OF_DAY));
    // zone offset in seconds (should we change it to UTC ?)
    buff.writeInt(-value.getOffset().getTotalSeconds());
  }

  private static OffsetTime binaryDecodeTIMETZ(int len, ByteBuf buff) {
    // micros to nanos
    return OffsetTime.of(LocalTime.ofNanoOfDay(buff.readLong() * 1000),
      // zone offset in seconds (should we change it to UTC ?)
      ZoneOffset.ofTotalSeconds(-buff.readInt()));
  }

  private static OffsetTime textDecodeTIMETZ(int len, ByteBuf buff) {
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return OffsetTime.parse(cs, TimeFormatter.TIMETZ_FORMAT);
  }

  private static void binaryEncodeTIMESTAMP(LocalDateTime value, ByteBuf buff) {
    buff.writeInt(8);
    buff.writeLong(-value.until(LOCAL_DATE_TIME_EPOCH, ChronoUnit.MICROS));
  }

  private static LocalDateTime binaryDecodeTIMESTAMP(int len, ByteBuf buff) {
    return LOCAL_DATE_TIME_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
  }

  private static LocalDateTime textDecodeTIMESTAMP(int len, ByteBuf buff) {
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return LocalDateTime.parse(cs, DateTimeFormatter.TIMESTAMP_FORMAT);
  }

  private static OffsetDateTime binaryDecodeTIMESTAMPTZ(int len, ByteBuf buff) {
    return OFFSET_DATE_TIME_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
  }

  private static void binaryEncodeTIMESTAMPTZ(OffsetDateTime value, ByteBuf buff) {
    buff.writeInt(8);
    buff.writeLong(-value.until(OFFSET_DATE_TIME_EPOCH, ChronoUnit.MICROS));
  }

  private static OffsetDateTime textDecodeTIMESTAMPTZ(int len, ByteBuf buff) {
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    return OffsetDateTime.parse(cs, DateTimeFormatter.TIMESTAMPTZ_FORMAT);
  }

  private static Buffer textDecodeBYTEA(int len, ByteBuf buff) {
    buff.readByte(); // \
    buff.readByte(); // x
    return Buffer.buffer(decodeHexStringToBytes(len - 2, buff));
  }

  private static void binaryEncodeBYTEA(Buffer value, ByteBuf buff) {
    int index = buff.writerIndex();
    buff.writeInt(0);
    ByteBuf byteBuf = value.getByteBuf();
    int len = byteBuf.readableBytes();
    buff.writeBytes(byteBuf);
    buff.setInt(index, len);
  }

  private static Buffer binaryDecodeBYTEA(int len, ByteBuf buff) {
    return Buffer.buffer(buff.readBytes(len));
  }

  private static void binaryEncodeUUID(UUID uuid, ByteBuf buff) {
    buff.writeInt(16);
    buff.writeLong(uuid.getMostSignificantBits());
    buff.writeLong(uuid.getLeastSignificantBits());
  }

  private static UUID binaryDecodeUUID(int len, ByteBuf buff) {
    return new UUID(buff.readLong(), buff.readLong());
  }

  private static UUID textDecodeUUID(int len, ByteBuf buff) {
    return java.util.UUID.fromString(buff.readCharSequence(len, StandardCharsets.UTF_8).toString());
  }

  private static Json textDecodeJSON(int len, ByteBuf buff) {
    return textDecodeJSONB(len, buff);
  }

  private static Json binaryDecodeJSON(int len, ByteBuf buff) {
    return textDecodeJSONB(len, buff);
  }

  private static void binaryEncodeJSON(Json value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = io.vertx.core.json.Json.encode(value.value());
    buff.writeInt(0); // Undetermined yet
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len);
  }

  private static Json textDecodeJSONB(int len, ByteBuf buff) {

    // Try to do without the intermediary String (?)
    CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
    Object value = null;
    String s = cs.toString();
    int pos = 0;
    while (pos < s.length() && Character.isWhitespace(s.charAt(pos))) {
      pos++;
    }
    if (pos == s.length()) {
      return null;
    } else if (s.charAt(pos) == '{') {
      value = new JsonObject(s);
    } else if (s.charAt(pos) == '[') {
      value = new JsonArray(s);
    } else {
      try {
        JsonNode jsonNode = io.vertx.core.json.Json.mapper.readTree(s);
        if (jsonNode.isNumber()) {
          value = jsonNode.numberValue();
        } else if (jsonNode.isBoolean()) {
          value = jsonNode.booleanValue();
        } else if (jsonNode.isTextual()) {
          value = jsonNode.textValue();
        }
      } catch (IOException e) {
        // do nothing
      }
    }
    return Json.create(value);
  }

  private static Json binaryDecodeJSONB(int len, ByteBuf buff) {
    buff.skipBytes(1); // Skip 1 byte for version (which is 1)
    return textDecodeJSONB(len - 1, buff);
  }

  private static void binaryEncodeJSONB(Json value, ByteBuf buff) {
    int index = buff.writerIndex();
    String s = io.vertx.core.json.Json.encode(value.value());
    buff.writeInt(0); // Undetermined yet
    buff.writeByte(1); // version
    int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
    buff.setInt(index, len + 1);
  }

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

  private static <T> T[] binaryDecodeArray(IntFunction<T[]> supplier, DataType type, int len, ByteBuf buff) {
    if (len == 12) {
      return supplier.apply(0);
    }
    buff.skipBytes(4);
    int offset = buff.readInt();
    buff.skipBytes(4);
    int length = buff.readInt();
    T[] array = supplier.apply(length);
    buff.skipBytes(offset+4);
    for (int i = 0; i < array.length; i++) {
      array[i] = (T) decodeBinary(type, buff.readInt(), buff);
    }
    return array;
  }

  private static <T> T[] textDecodeArray(IntFunction<T[]> supplier, DataType type, int len, ByteBuf buff) {
    if (len == 12) {
      return supplier.apply(0);
    }
    List<T> list = new ArrayList<>();
    buff.skipBytes(1); // {
    int from = buff.readerIndex();
    int to = buff.writerIndex() - 1;
    while (true) {
      int idx = buff.indexOf(from, to, (byte) ',');
      if (idx == -1) {
        break;
      } else {
        int l = idx - from;
        T o = (T) decodeText(type, l, buff);
        list.add(o);
        buff.readerIndex(from = idx + 1);
      }
    }
    T o = (T) decodeText(type, to - from, buff);
    list.add(o);
    return list.toArray(supplier.apply(list.size()));
  }

  private static <T> void binaryEncodeArray(T[] values, DataType type, ByteBuf buff){
    int startIndex = buff.writerIndex();
    buff.writeInt(0);
    buff.writeInt(1);
    buff.writeInt(0);
    buff.writeInt(type.id);
    buff.writeInt(values.length);
    buff.writeInt(1);
    for (T value : values) {
      encodeBinary(type, value, buff);
    }
    buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
  }

  private static <T> void textEncodeArray(T[] values, DataType type, ByteBuf buff){
    buff.writeByte('{');
    int len = values.length;
    for (int i = 0; i < len; i++) {
      if (i > 0) {
        buff.writeByte(',');
      }
      textEncode(type, values[i], buff);
    }
    buff.writeByte('}');
  }
}
