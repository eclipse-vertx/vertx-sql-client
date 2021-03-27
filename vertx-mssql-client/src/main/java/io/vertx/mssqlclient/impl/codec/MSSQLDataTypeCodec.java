/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mssqlclient.impl.protocol.datatype.*;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

class MSSQLDataTypeCodec {
  static LocalDate START_DATE = LocalDate.of(1, 1, 1);
  private static Map<Class, String> parameterDefinitionsMapping = new HashMap<>();

  static {
    parameterDefinitionsMapping.put(Byte.class, "tinyint");
    parameterDefinitionsMapping.put(Short.class, "smallint");
    parameterDefinitionsMapping.put(Integer.class, "int");
    parameterDefinitionsMapping.put(Long.class, "bigint");
    parameterDefinitionsMapping.put(Boolean.class, "bit");
    parameterDefinitionsMapping.put(Float.class, "float");
    parameterDefinitionsMapping.put(Double.class, "float");
    parameterDefinitionsMapping.put(String.class, "nvarchar(4000)");
    parameterDefinitionsMapping.put(LocalDate.class, "date");
    parameterDefinitionsMapping.put(LocalTime.class, "time");
    parameterDefinitionsMapping.put(LocalDateTime.class, "datetime2");
  }

  static String inferenceParamDefinitionByValueType(Object value) {
    if (value == null) {
      return "nvarchar(4000)";
    } else if (value instanceof Numeric) {
      //TODO we may need some changes in Numeric to make this work
      throw new UnsupportedOperationException();
    } else if (value.getClass().isEnum()) {
      return parameterDefinitionsMapping.get(String.class);
    } else {
      String paramDefinition = parameterDefinitionsMapping.get(value.getClass());
      if (paramDefinition != null) {
        return paramDefinition;
      } else {
        throw new UnsupportedOperationException("Unsupported type" + value.getClass());
      }
    }
  }

  static Object decode(MSSQLDataType dataType, ByteBuf in) {
    switch (dataType.id()) {
      case MSSQLDataTypeId.INT1TYPE_ID:
        return decodeTinyInt(in);
      case MSSQLDataTypeId.INT2TYPE_ID:
        return decodeSmallInt(in);
      case MSSQLDataTypeId.INT4TYPE_ID:
        return decodeInt(in);
      case MSSQLDataTypeId.INT8TYPE_ID:
        return decodeBigInt(in);
      case MSSQLDataTypeId.NUMERICNTYPE_ID:
      case MSSQLDataTypeId.DECIMALNTYPE_ID:
        return decodeNumeric((NumericDataType) dataType, in);
      case MSSQLDataTypeId.INTNTYPE_ID:
        return decodeIntN(in);
      case MSSQLDataTypeId.FLT4TYPE_ID:
        return decodeFloat4(in);
      case MSSQLDataTypeId.FLT8TYPE_ID:
        return decodeFloat8(in);
      case MSSQLDataTypeId.FLTNTYPE_ID:
        return decodeFltN(in);
      case MSSQLDataTypeId.BITTYPE_ID:
        return decodeBit(in);
      case MSSQLDataTypeId.BITNTYPE_ID:
        return decodeBitN(in);
      case MSSQLDataTypeId.DATENTYPE_ID:
        return decodeDateN(in);
      case MSSQLDataTypeId.TIMENTYPE_ID:
        return decodeTimeN((TimeNDataType) dataType, in);
      case MSSQLDataTypeId.DATETIME2NTYPE_ID:
        return decodeDateTime2N((DateTime2NDataType) dataType, in);
      case MSSQLDataTypeId.BIGVARCHRTYPE_ID:
      case MSSQLDataTypeId.BIGCHARTYPE_ID:
        return decodeVarchar(in);
      case MSSQLDataTypeId.NCHARTYPE_ID:
      case MSSQLDataTypeId.NVARCHARTYPE_ID:
        return decodeNVarchar(in);
      default:
        throw new UnsupportedOperationException("Unsupported datatype: " + dataType);
    }
  }

  private static LocalTime decodeTimeN(TimeNDataType dataType, ByteBuf in) {
    int scale = dataType.scale();
    byte timeLength = in.readByte();
    long timeValue;
    switch (timeLength) {
      case 0:
        return null;
      case 3:
        timeValue = in.readUnsignedMediumLE();
        break;
      case 4:
        timeValue = in.readUnsignedIntLE();
        break;
      case 5:
        timeValue = readUnsignedInt40LE(in);
        break;
      default:
        throw new IllegalStateException("Unexpected timeLength of [" + timeLength + "]");
    }
    return getLocalTime(scale, timeValue);
  }

  private static LocalTime getLocalTime(int scale, long timeValue) {
    long hundredNanos = timeValue;
    for (int i = scale; i < 7; i++) {
      hundredNanos *= 10;
    }
    return LocalTime.ofNanoOfDay(100 * hundredNanos);
  }

  private static LocalDateTime decodeDateTime2N(DateTime2NDataType dataType, ByteBuf in) {
    int scale = dataType.scale();
    byte dataLength = in.readByte();
    if (dataLength == 0) {
      return null;
    }
    long timeValue;
    switch (dataLength - 3) {
      case 3:
        timeValue = in.readUnsignedMediumLE();
        break;
      case 4:
        timeValue = in.readUnsignedIntLE();
        break;
      case 5:
        timeValue = readUnsignedInt40LE(in);
        break;
      default:
        throw new IllegalStateException("Unexpected dataLength of [" + dataLength + "]");
    }
    LocalTime localTime = getLocalTime(scale, timeValue);

    int days = in.readUnsignedMediumLE();
    LocalDate localDate = getLocalDate(days);

    return LocalDateTime.of(localDate, localTime);
  }

  private static CharSequence decodeNVarchar(ByteBuf in) {
    int length = in.readUnsignedShortLE();
    if (length == 65535) {
      // CHARBIN_NULL
      return null;
    }
    return in.readCharSequence(length, StandardCharsets.UTF_16LE);
  }

  private static CharSequence decodeVarchar(ByteBuf in) {
    int length = in.readUnsignedShortLE();
    if (length == 65535) {
      // CHARBIN_NULL
      return null;
    }
    return in.readCharSequence(length, StandardCharsets.UTF_8);
  }

  private static LocalDate decodeDateN(ByteBuf in) {
    byte dateLength = in.readByte();
    if (dateLength == 0) {
      return null;
    } else if (dateLength == 3) {
      int days = in.readUnsignedMediumLE();
      return getLocalDate(days);
    } else {
      throw new IllegalStateException("Unexpected dateLength of [" + dateLength + "]");
    }
  }

  private static LocalDate getLocalDate(int days) {
    return START_DATE.plus(days, ChronoUnit.DAYS);
  }

  private static boolean decodeBit(ByteBuf in) {
    return in.readBoolean();
  }

  private static double decodeFloat8(ByteBuf in) {
    return in.readDoubleLE();
  }

  private static float decodeFloat4(ByteBuf in) {
    return in.readFloatLE();
  }

  private static Numeric decodeNumeric(NumericDataType dataType, ByteBuf in) {
    int scale = dataType.scale();
    short length = in.readUnsignedByte();
    if (length == 0) {
      return null;
    } else {
      int sign = in.readByte();
      Number value;
      switch (length - 1) {
        case 4:
          value = in.readIntLE();
          break;
        case 8:
          value = in.readLongLE();
          break;
        case 12:
          return Numeric.create(new BigDecimal(readUnsignedInt96LE(in), scale));
        case 16:
          return Numeric.create(new BigDecimal(readUnsignedInt128LE(in), scale));
        default:
          throw new IllegalStateException("Unexpected numeric length of [" + length + "]");
      }
      return Numeric.create(value.longValue() / Math.pow(10, scale) * sign);
    }
  }

  private static long decodeBigInt(ByteBuf in) {
    return in.readLongLE();
  }

  private static int decodeInt(ByteBuf in) {
    return in.readIntLE();
  }

  private static short decodeSmallInt(ByteBuf in) {
    return in.readShortLE();
  }

  private static short decodeTinyInt(ByteBuf in) {
    return in.readUnsignedByte();
  }

  private static long readUnsignedInt40LE(ByteBuf buffer) {
    long low = buffer.readUnsignedIntLE();
    short high = buffer.readUnsignedByte();
    return (0x100000000L * high) + low;
  }

  private static BigInteger readUnsignedInt96LE(ByteBuf buffer) {
    byte[] result = new byte[12];
    int readerIndex = buffer.readerIndex();
    for (int i = 0; i < 12; i++) {
      result[i] = buffer.getByte(readerIndex + 11 - i);
    }
    buffer.skipBytes(12);
    return new BigInteger(result);
  }

  private static BigInteger readUnsignedInt128LE(ByteBuf buffer) {
    byte[] result = new byte[16];
    int readerIndex = buffer.readerIndex();
    for (int i = 0; i < 16; i++) {
      result[i] = buffer.getByte(readerIndex + 15 - i);
    }
    buffer.skipBytes(16);
    return new BigInteger(result);
  }

  private static Object decodeIntN(ByteBuf buffer) {
    int intNDataTypeLength = buffer.readByte();
    switch (intNDataTypeLength) {
      case 0:
        // this means we read a NULL value(nullable data type).
        return null;
      case 1:
        return buffer.readUnsignedByte();
      case 2:
        return buffer.readShortLE();
      case 4:
        return buffer.readIntLE();
      case 8:
        return buffer.readLongLE();
      default:
        throw new UnsupportedOperationException(String.format("SEVERE: Unsupported length=[%d] for decoding IntNDataType row value.", intNDataTypeLength));
    }
  }

  private static Object decodeFltN(ByteBuf buffer) {
    int fltNDataTypeLength = buffer.readByte();
    switch (fltNDataTypeLength) {
      case 0:
        // this means we read a NULL value(nullable data type).
        return null;
      case 4:
        return buffer.readFloatLE();
      case 8:
        return buffer.readDoubleLE();
      default:
        throw new UnsupportedOperationException(String.format("SEVERE: Unsupported length=[%d] for decoding FLTNTYPE row value.", fltNDataTypeLength));
    }
  }

  private static Object decodeBitN(ByteBuf buffer) {
    int bitNDataTypeLength = buffer.readByte();
    switch (bitNDataTypeLength) {
      case 0:
        // this means we read a NULL value(nullable data type).
        return null;
      case 1:
        return buffer.readBoolean();
      default:
        throw new UnsupportedOperationException(String.format("SEVERE: Unsupported length=[%d] for decoding BITNTYPE row value.", bitNDataTypeLength));
    }
  }
}
