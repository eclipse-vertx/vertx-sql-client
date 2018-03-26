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
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
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

import static javax.xml.bind.DatatypeConverter.printHexBinary;

/**
 * PostgreSQL <a href="https://github.com/postgres/postgres/blob/master/src/include/catalog/pg_type.h">object
 * identifiers (OIDs)</a> for data types
 *
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class DataType<T> {

  public interface Decoder<T> {
    T decode(int len, ByteBuf buff);
  }
  interface ArrayFactory<T> {
    T[] create(int size);
  }

  private static ArrayFactory<String> STRING_ARRAY_FACTORY = String[]::new;
  private static ArrayFactory<LocalDate> LOCALDATE_ARRAY_FACTORY = LocalDate[]::new;
  private static ArrayFactory<LocalTime> LOCALTIME_ARRAY_FACTORY = LocalTime[]::new;
  private static ArrayFactory<OffsetTime> OFFSETTIME_ARRAY_FACTORY = OffsetTime[]::new;
  private static ArrayFactory<LocalDateTime> LOCALDATETIME_ARRAY_FACTORY = LocalDateTime[]::new;
  private static ArrayFactory<OffsetDateTime> OFFSETDATETIME_ARRAY_FACTORY = OffsetDateTime[]::new;
  private static ArrayFactory<Buffer> BUFFER_ARRAY_FACTORY = Buffer[]::new;
  private static ArrayFactory<java.util.UUID> UUID_ARRAY_FACTORY = java.util.UUID[]::new;

  // 1 byte
  public static final DataType<Boolean> BOOL = new DataType<Boolean>(Boolean.class, 16) {
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
    public void encodeBinaryInternal(Boolean value, ByteBuf buff) {
      buff.writeInt(1);
      buff.writeBoolean(value);
    }
  };
  //Commented version cause other datatypes are always the same procedure
  public static final DataType<boolean[]> BOOL_ARRAY = new DataType<boolean[]>(boolean[].class, 1000) {

    @Override
    public boolean[] decodeBinary(int len, ByteBuf buff) {
      //If length equals 12 then it is an empty array
      if (len == 12) {
        return new boolean[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);//Skip dimensions cause its always 1
      int offset = buff.readInt();//Read the offset, used to skip the bitmap
      buff.readerIndex(buff.readerIndex() + 4);//Skip the oid cause we know what type it is
      int length = buff.readInt(); //Read the length to create a fixed length array
      boolean[] array = new boolean[length];//Create the array
      buff.readerIndex(buff.readerIndex() + offset + 4);//Skip, if exists, the bitmap and the lower boundry
      for (int i = 0; i < array.length; i++) {
        array[i] = BOOL.decodeBinary(buff.readInt(), buff);//Decode the elements with the element decoder
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(boolean[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();//Mark for later to insert the length
      buff.writeInt(0);//Write 0 as placeholder
      buff.writeInt(1);//write the dimension, always 1 because we only accept 1 dimensional arrays
      buff.writeInt(0);//Write the offset to skip the bitmap, always 0 because we dont write bitmaps
      buff.writeInt(BOOL.id);//Write the oid
      buff.writeInt(values.length);//Write the length of the array
      buff.writeInt(1);//Write the lower boundry, seems to always be 1
      for (boolean value : values) {
        BOOL.encodeBinaryInternal(value, buff);//Encode the element with the element encoder
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);//Set the length of the header plus body minus the length of the length attribute
    }
  };

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
    public void encodeBinaryInternal(Short value, ByteBuf buff) {
      buff.writeInt(2);
      buff.writeShort(value);
    }
  };

  public static final DataType<short[]> INT2_ARRAY = new DataType<short[]>(short[].class, 1005) {

    @Override
    public short[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new short[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      short[] array = new short[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = INT2.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(short[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(INT2.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (short value : values) {
        INT2.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

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
    public void encodeBinaryInternal(Integer value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeInt(value);
    }
  };

  public static final DataType<int[]> INT4_ARRAY = new DataType<int[]>(int[].class, 1007) {

    @Override
    public int[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new int[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      int[] array = new int[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = INT4.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(int[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(INT4.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (Integer value : values) {
        INT4.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

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
    public void encodeBinaryInternal(Long value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(value);
    }
  };

  public static final DataType<long[]> INT8_ARRAY = new DataType<long[]>(long[].class, 1016) {

    @Override
    public long[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new long[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      long[] array = new long[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = INT8.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(long[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(INT8.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (long value : values) {
        INT8.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

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
    public void encodeBinaryInternal(Float value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeFloat(value);
    }
  };

  public static final DataType<float[]> FLOAT4_ARRAY = new DataType<float[]>(float[].class, 1021) {

    @Override
    public float[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new float[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      float[] array = new float[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = FLOAT4.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(float[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(FLOAT4.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (float value : values) {
        FLOAT4.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

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
    public void encodeBinaryInternal(Double value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeDouble(value);
    }
  };

  public static final DataType<double[]> FLOAT8_ARRAY = new DataType<double[]>(double[].class, 1022) {

    @Override
    public double[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new double[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      double[] array = new double[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = FLOAT8.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(double[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(FLOAT8.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (double value : values) {
        FLOAT8.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

  // User specified precision
  public static final DataType<Number> NUMERIC = new DataType<Number>(Number.class,1700) {
    @Override
    public Number decodeText(int len, ByteBuf buff) {
      // Todo optimize that
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Numeric.parse(cs.toString());
    }
  };

  public static final DataType<double[]> NUMERIC_ARRAY = new DataType<>(double[].class, 1231);

  // 8 bytes double
  public static final DataType<Object> MONEY = new DataType<>(Object.class, 790);
  public static final DataType<Object> MONEY_ARRAY = new DataType<>(Object.class, 791);

  // Fixed length bit string
  public static final DataType<Object> BITS = new DataType<>(Object.class, 1560);
  public static final DataType<Object> BIT_ARRAY = new DataType<>(Object.class, 1561);

  // Limited length bit string
  public static final DataType<Object> VARBIT = new DataType<>(Object.class, 1562);
  public static final DataType<Object> VARBIT_ARRAY = new DataType<>(Object.class, 1563);

  // Single length character, is always 25 TEXT so not implemented any more
  public static final DataType<Character> CHAR = new DataType<Character>(Character.class, 18) {
    @Override
    public Character decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public Character decodeBinary(int len, ByteBuf buff) {
      return (char)buff.readByte();
    }

    @Override
    public void encodeText(Character value, ByteBuf buff) {
      encodeBinaryInternal(value, buff);
    }

    @Override
    public void encodeBinaryInternal(Character value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.writeInt(0);
      buff.writeChar(value);
      buff.setInt(index, buff.writerIndex() - 4 - index);
    }
  };
  // Single length character
  public static final DataType<char[]> CHAR_ARRAY = new DataType<char[]>(char[].class, 1002) {

    @Override
    public char[] decodeBinary(int len, ByteBuf buff) {
      if (len == 12) {
        return new char[]{};
      }
      buff.readerIndex(buff.readerIndex() + 4);
      int offset = buff.readInt();
      buff.readerIndex(buff.readerIndex() + 4);
      int length = buff.readInt();
      char[] array = new char[length];
      buff.readerIndex(buff.readerIndex() + offset + 4);
      for (int i = 0; i < array.length; i++) {
        array[i] = CHAR.decodeBinary(buff.readInt(), buff);
      }
      return array;
    }

    @Override
    public void encodeBinaryInternal(char[] values, ByteBuf buff) {
      int startIndex = buff.writerIndex();
      buff.writeInt(0);
      buff.writeInt(1);
      buff.writeInt(0);
      buff.writeInt(CHAR.id);
      buff.writeInt(values.length);
      buff.writeInt(1);
      for (Character value : values) {
        CHAR.encodeBinaryInternal(value, buff);
      }
      buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
    }
  };

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
    public void encodeBinaryInternal(String value, ByteBuf buff) {
      super.encodeText(value, buff);
    }
  };

  public static final DataType<String[]> VARCHAR_ARRAY = new DataType<String[]>(String[].class, 1015) {

    @Override
    public String[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(STRING_ARRAY_FACTORY, DataType.VARCHAR, len, buff);
    }

    @Override
    public void encodeBinaryInternal(String[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.VARCHAR, buff);
    }
  };

  // Limited blank padded length string
  public static final DataType<String> BPCHAR = new DataType<String>(String.class, 1042) {

    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }

    @Override
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }

    @Override
    public void encodeText(String value, ByteBuf buff) {
      encodeBinaryInternal(value, buff);
    }

    @Override
    public void encodeBinaryInternal(String value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.writeInt(0);
      buff.writeCharSequence(value, StandardCharsets.UTF_8);
      buff.setInt(index, buff.writerIndex() - 4 - index);
    }
  };
  public static final DataType<String[]> BPCHAR_ARRAY = new DataType<String[]>(String[].class, 1014) {

    @Override
    public String[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(STRING_ARRAY_FACTORY, DataType.BPCHAR, len, buff);
    }

    @Override
    public void encodeBinaryInternal(String[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.BPCHAR, buff);
    }
  };

  // Unlimited length string
  public static final DataType<String> TEXT = new DataType<String>(String.class, 25) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
    @Override
    public void encodeBinaryInternal(String value, ByteBuf buff) {
      super.encodeText(value, buff);
    }
  };
  public static final DataType<String[]> TEXT_ARRAY = new DataType<String[]>(String[].class, 1009) {

    @Override
    public String[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(STRING_ARRAY_FACTORY, DataType.TEXT, len, buff);
    }

    @Override
    public void encodeBinaryInternal(String[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.TEXT, buff);
    }
  };

  // 63 bytes length string (internal type for object names)
  public static final DataType<String> NAME = new DataType<String>(String.class, 19) {
    @Override
    public String decodeText(int len, ByteBuf buff) {
      return decodeBinary(len, buff);
    }
    @Override
    public String decodeBinary(int len, ByteBuf buff) {
      return buff.readCharSequence(len, StandardCharsets.UTF_8).toString();
    }
    @Override
    public void encodeBinaryInternal(String value, ByteBuf buff) {
      super.encodeText(value, buff);
    }
  };

  public static final DataType<String[]> NAME_ARRAY = new DataType<String[]>(String[].class, 1003) {

    @Override
    public String[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(STRING_ARRAY_FACTORY, DataType.NAME, len, buff);
    }

    @Override
    public void encodeBinaryInternal(String[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.NAME, buff);
    }
  };

  // 4 bytes date (no time of day)
  public static final DataType<LocalDate> DATE = new DataType<LocalDate>(LocalDate.class, 1082) {
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
    public void encodeBinaryInternal(LocalDate value, ByteBuf buff) {
      buff.writeInt(4);
      buff.writeInt((int) -value.until(PG_EPOCH, ChronoUnit.DAYS));
    }
  };
  public static final DataType<LocalDate[]> DATE_ARRAY = new DataType<LocalDate[]>(LocalDate[].class, 1182) {

    @Override
    public LocalDate[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(LOCALDATE_ARRAY_FACTORY, DataType.DATE, len, buff);
    }

    @Override
    public void encodeBinaryInternal(LocalDate[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.DATE, buff);
    }
  };

  // 8 bytes time of day (no date) without time zone
  public static final DataType<LocalTime> TIME = new DataType<LocalTime>(LocalTime.class, 1083) {
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
    public void encodeBinaryInternal(LocalTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(value.getLong(ChronoField.MICRO_OF_DAY));
    }
  };
  public static final DataType<LocalTime[]> TIME_ARRAY = new DataType<LocalTime[]>(LocalTime[].class, 1183) {

    @Override
    public LocalTime[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(LOCALTIME_ARRAY_FACTORY, DataType.TIME, len, buff);
    }

    @Override
    public void encodeBinaryInternal(LocalTime[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.TIME, buff);
    }
  };

  // 12 bytes time of day (no date) with time zone
  public static final DataType<OffsetTime> TIMETZ = new DataType<OffsetTime>(OffsetTime.class,1266) {
    @Override
    public OffsetTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetTime.parse(cs, TimeFormatter.TIMETZ_FORMAT);
    }
    @Override
    public OffsetTime decodeBinary(int len, ByteBuf buff) {
      // micros to nanos
      return OffsetTime.of(LocalTime.ofNanoOfDay(buff.readLong() * 1000),
        // zone offset in seconds (should we change it to UTC ?)
        ZoneOffset.ofTotalSeconds(-buff.readInt()));
    }
    @Override
    public void encodeBinaryInternal(OffsetTime value, ByteBuf buff) {
      buff.writeInt(12);
      buff.writeLong(value.toLocalTime().getLong(ChronoField.MICRO_OF_DAY));
      // zone offset in seconds (should we change it to UTC ?)
      buff.writeInt(-value.getOffset().getTotalSeconds());
    }
  };
  public static final DataType<OffsetTime[]> TIMETZ_ARRAY = new DataType<OffsetTime[]>(OffsetTime[].class, 1270) {

    @Override
    public OffsetTime[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(OFFSETTIME_ARRAY_FACTORY, DataType.TIMETZ, len, buff);
    }

    @Override
    public void encodeBinaryInternal(OffsetTime[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.TIMETZ, buff);
    }
  };

  // 8 bytes date and time without time zone
  public static final DataType<LocalDateTime> TIMESTAMP = new DataType<LocalDateTime>(LocalDateTime.class,1114) {
    final LocalDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0);
    @Override
    public LocalDateTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return LocalDateTime.parse(cs, DateTimeFormatter.TIMESTAMP_FORMAT);
    }
    @Override
    public LocalDateTime decodeBinary(int len, ByteBuf buff) {
      return PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
    }
    @Override
    public void encodeBinaryInternal(LocalDateTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(-value.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static final DataType<LocalDateTime[]> TIMESTAMP_ARRAY = new DataType<LocalDateTime[]>(LocalDateTime[].class, 1115) {

    @Override
    public LocalDateTime[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(LOCALDATETIME_ARRAY_FACTORY, DataType.TIMESTAMP, len, buff);
    }

    @Override
    public void encodeBinaryInternal(LocalDateTime[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.TIMESTAMP, buff);
    }
  };

  // 8 bytes date and time with time zone
  public static final DataType<OffsetDateTime> TIMESTAMPTZ = new DataType<OffsetDateTime>(OffsetDateTime.class,1184) {
    final OffsetDateTime PG_EPOCH = LocalDateTime.of(2000, 1, 1, 0, 0, 0).atOffset(ZoneOffset.UTC);
    @Override
    public OffsetDateTime decodeText(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return OffsetDateTime.parse(cs, DateTimeFormatter.TIMESTAMPTZ_FORMAT);
    }
    @Override
    public OffsetDateTime decodeBinary(int len, ByteBuf buff) {
      return PG_EPOCH.plus(buff.readLong(), ChronoUnit.MICROS);
    }
    @Override
    public void encodeBinaryInternal(OffsetDateTime value, ByteBuf buff) {
      buff.writeInt(8);
      buff.writeLong(-value.until(PG_EPOCH, ChronoUnit.MICROS));
    }
  };
  public static final DataType<OffsetDateTime[]> TIMESTAMPTZ_ARRAY = new DataType<OffsetDateTime[]>(OffsetDateTime[].class, 1185) {

    @Override
    public OffsetDateTime[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(OFFSETDATETIME_ARRAY_FACTORY, DataType.TIMESTAMPTZ, len, buff);
    }

    @Override
    public void encodeBinaryInternal(OffsetDateTime[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.TIMESTAMPTZ, buff);
    }
  };
  // 16 bytes time interval
  public static final DataType<Object> INTERVAL = new DataType<>(Object.class, 1186);
  public static final DataType<Object> INTERVAL_ARRAY = new DataType<>(Object.class, 1187);

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
    public void encodeBinaryInternal(Buffer value, ByteBuf buff) {
      int index = buff.writerIndex();
      buff.writeInt(0);
      ByteBuf byteBuf = value.getByteBuf();
      int len = byteBuf.readableBytes();
      buff.writeBytes(byteBuf);
      buff.setInt(index, len);
    }
  };
  public static final DataType<Buffer[]> BYTEA_ARRAY = new DataType<Buffer[]>(Buffer[].class, 1001) {

    @Override
    public Buffer[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(BUFFER_ARRAY_FACTORY, DataType.BYTEA, len, buff);
    }

    @Override
    public void encodeBinaryInternal(Buffer[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.BYTEA, buff);
    }
  };

  // 6 bytes MAC address (XX:XX:XX:XX:XX:XX)
  public static final DataType<Object> MACADDR = new DataType<>(Object.class, 829);

  // 7 or 19 bytes (IPv4 and IPv6 hosts and networks)
  public static final DataType<Object> INET = new DataType<>(Object.class, 869);

  // 7 or 19 bytes (IPv4 and IPv6 networks)
  public static final DataType<Object> CIDR = new DataType<>(Object.class, 650);

  // 8 bytes MAC address (XX:XX:XX:XX:XX:XX:XX:XX)
  public static final DataType<Object> MACADDR8 = new DataType<>(Object.class, 774);

  // UUID
  public static final DataType<java.util.UUID> UUID = new DataType<java.util.UUID>(java.util.UUID.class, 2950) {
    @Override
    public java.util.UUID decodeText(int len, ByteBuf buff) {
      return java.util.UUID.fromString(buff.readCharSequence(len, StandardCharsets.UTF_8).toString());
    }
    @Override
    public java.util.UUID decodeBinary(int len, ByteBuf buff) {
      return new java.util.UUID(buff.readLong(), buff.readLong());
    }
    @Override
    public void encodeBinaryInternal(java.util.UUID uuid, ByteBuf buff) {
      buff.writeInt(16);
      buff.writeLong(uuid.getMostSignificantBits());
      buff.writeLong(uuid.getLeastSignificantBits());
    }
  };
  public static final DataType<java.util.UUID[]> UUID_ARRAY = new DataType<java.util.UUID[]>(java.util.UUID[].class, 2951) {

    @Override
    public java.util.UUID[] decodeBinary(int len, ByteBuf buff) {
      return decodeBinary(UUID_ARRAY_FACTORY, DataType.UUID, len, buff);
    }

    @Override
    public void encodeBinaryInternal(java.util.UUID[] values, ByteBuf buff) {
      encodeBinaryArray(values, DataType.UUID, buff);
    }
  };

  // Text JSON
  public static final DataType<Json> JSON = new DataType<Json>(Json.class,114) {
    @Override
    public Json decodeText(int len, ByteBuf buff) {
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Json.create(decodeJson(cs.toString()));
    }
    @Override
    public Json decodeBinary(int len, ByteBuf buff) {
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Json.create(decodeJson(cs.toString()));
    }
    @Override
    public boolean accept(Object value) {
      return value instanceof Json
        || value instanceof String
        || value instanceof Boolean
        || value instanceof Number;
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      if (value instanceof Json) {
        value = ((Json) value).value();
      }
      DataType.TEXT.encodeText(io.vertx.core.json.Json.encode(value), buff);
    }
  };
  // Binary JSON
  public static final DataType<Json> JSONB = new DataType<Json>(Json.class,3802) {
    @Override
    public Json decodeText(int len, ByteBuf buff) {
      // Not sure this is correct
      // Try to do without the intermediary String
      CharSequence cs = buff.readCharSequence(len, StandardCharsets.UTF_8);
      return Json.create(decodeJson(cs.toString()));
    }
    @Override
    public boolean accept(Object value) {
      return JSON.accept(value);
    }
    @Override
    public void encodeBinary(Object value, ByteBuf buff) {
      if (value instanceof Json) {
        value = ((Json) value).value();
      }
      int index = buff.writerIndex();
      String s = io.vertx.core.json.Json.encode(value);
      buff.writeInt(0); // Undetermined yet
      buff.writeByte(1); // version
      int len = buff.writeCharSequence(s, StandardCharsets.UTF_8);
      buff.setInt(index, len + 1);
    }
    @Override
    public Json decodeBinary(int len, ByteBuf buff) {
      buff.readerIndex(buff.readerIndex() + 1); // Skip 1 byte for version (which is 1)
      return decodeText(len - 1, buff);
    }
  };
  // XML
  public static final DataType<Object> XML = new DataType<>(Object.class, 142);
  public static final DataType<Object> XML_ARRAY = new DataType<>(Object.class, 143);

  // Geometric point (x, y)
  public static final DataType<Object> POINT = new DataType<>(Object.class, 600);
  // Geometric box (lower left, upper right)
  public static final DataType<Object> BOX = new DataType<>(Object.class, 603);
  public static final DataType<Object> HSTORE = new DataType<>(Object.class, 33670);

  // Object identifier
  public static final DataType<Object> OID = new DataType<>(Object.class, 26);
  public static final DataType<Object> OID_ARRAY = new DataType<>(Object.class, 1028);
  public static final DataType<Object> VOID = new DataType<>(Object.class, 2278);
  public static final DataType<Object> UNKNOWN = new DataType<>(Object.class, 705);

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
        JsonNode jsonNode = io.vertx.core.json.Json.mapper.readTree(value);
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
  public final Decoder<T> textDecoder;
  public final Decoder<T> binaryDecoder;

  private DataType(Class<T> javaType, int id) {
    this.javaType = javaType;
    this.id = id;
    this.textDecoder = this::decodeText;
    this.binaryDecoder = this::decodeBinary;
  }

  public DataType(Class<T> javaType, int id,
                  Decoder<T> textDecoder,
                  Decoder<T> binaryDecoder) {
    this.javaType = javaType;
    this.id = id;
    this.textDecoder = textDecoder;
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

  public boolean accept(Object value) {
    return javaType.isInstance(value);
  }

  public void encodeBinary(Object value, ByteBuf buff) {
    encodeBinaryInternal((T) value, buff);
  }

  protected void encodeBinaryInternal(T value, ByteBuf buff) {
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

  public static <T> T[] decodeBinary(ArrayFactory<T> supplier, DataType<T> type, int len, ByteBuf buff) {
    buff.readerIndex(buff.readerIndex() + 4);
    int offset = buff.readInt();
    buff.readerIndex(buff.readerIndex() + 4);
    int length = buff.readInt();
    T[] array = supplier.create(length);
    buff.readerIndex(buff.readerIndex() + offset + 4);
    for (int i = 0; i < array.length; i++) {
      array[i] = type.decodeBinary(buff.readInt(), buff);
    }
    return array;
  }

  public static <T> void encodeBinaryArray(T[] values, DataType<T> type, ByteBuf buff){
    int startIndex = buff.writerIndex();
    buff.writeInt(0);
    buff.writeInt(1);
    buff.writeInt(0);
    buff.writeInt(type.id);
    buff.writeInt(values.length);
    buff.writeInt(1);
    for (T value : values) {
      type.encodeBinaryInternal(value, buff);
    }
    buff.setInt(startIndex, buff.writerIndex() - 4 - startIndex);
  }
}
