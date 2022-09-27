/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
import io.netty.buffer.Unpooled;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.buffer.impl.VertxByteBufAllocator;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.math.RoundingMode;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.sql.JDBCType;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public enum DataType {

  // Zero-Length Data Types https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/bc91c82f-8ee0-4256-98d9-c800bf9ae33b
  NULL(0x1F) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public String paramDefinition(Object value) {
      return "nvarchar(4000)";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
    }
  },

  // Fixed-Length Data Types https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/859eb3d2-80d3-40f6-a637-414552c9c552
  INT1(0x30) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TINYINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readUnsignedByte();
    }
  },
  BIT(0x32) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.BOOLEAN;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readBoolean();
    }
  },
  INT2(0x34) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.SMALLINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readShortLE();
    }
  },
  INT4(0x38) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.INTEGER;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readIntLE();
    }
  },
  DATETIM4(0x3A) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return decodeUnsignedShortDateValue(byteBuf);
    }

    @Override
    public String paramDefinition(Object value) {
      return "smalldatetime";
    }
  },
  FLT4(0x3B) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.REAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readFloatLE();
    }
  },
  MONEY(0x3C) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      long highBits = (long) byteBuf.readIntLE() << 32;
      long lowBits = byteBuf.readIntLE() & 0xFFFFFFFFL;
      BigInteger bigInteger = BigInteger.valueOf(highBits | lowBits);
      return new BigDecimal(bigInteger).divide(new BigDecimal("10000"), 4, RoundingMode.UP);
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DECIMAL;
    }
  },
  DATETIME(0x3D) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return decodeIntLEDateValue(byteBuf);
    }
  },
  FLT8(0x3E) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DOUBLE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readDoubleLE();
    }
  },
  MONEY4(0x7A){
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return new BigDecimal(byteBuf.readIntLE()).divide(new BigDecimal("10000"), 2, RoundingMode.UP);
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DECIMAL;
    }
  },
  INT8(0x7F) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.BIGINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return byteBuf.readLongLE();
    }
  },
  DECIMAL(0x37),
  NUMERIC(0x3F),

  // Variable-Length Data Types https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/ce3183a6-9d89-47e8-a02f-de5a1a1303de
  GUID(0x24) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      if (typeInfo.maxLength() == 16) return JDBCType.OTHER;
      throw new IllegalArgumentException("Invalid length: " + typeInfo.maxLength());
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 16) {
        long first = byteBuf.readIntLE();
        long second = byteBuf.readShortLE() & 0xFFFF;
        long third = byteBuf.readShortLE() & 0xFFFF;
        long lsb = byteBuf.readLong();
        return new UUID((first << 32) + (second << 16) + third, lsb);
      }
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public String paramDefinition(Object value) {
      return "uniqueidentifier";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      UUID uValue;
      if (value instanceof UUID) {
        uValue = (UUID) value;
      } else throw new IllegalArgumentException(value.getClass().getName());
      writeParamSize(byteBuf, 16, 16);

      long msb = uValue.getMostSignificantBits();
      byteBuf.writeIntLE((int) (msb >> 32));
      byteBuf.writeShortLE((short) (msb >> 16));
      byteBuf.writeShortLE((short) (msb));
      byteBuf.writeLong(uValue.getLeastSignificantBits());
    }
  },
  INTN(0x26) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      if (typeInfo.maxLength() == 1) return JDBCType.TINYINT;
      if (typeInfo.maxLength() == 2) return JDBCType.SMALLINT;
      if (typeInfo.maxLength() == 4) return JDBCType.INTEGER;
      if (typeInfo.maxLength() == 8) return JDBCType.BIGINT;
      throw new IllegalArgumentException("Invalid length: " + typeInfo.maxLength());
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 1) return byteBuf.readUnsignedByte();
      if (length == 2) return byteBuf.readShortLE();
      if (length == 4) return byteBuf.readIntLE();
      if (length == 8) return byteBuf.readLongLE();
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public String paramDefinition(Object value) {
      return "bigint";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      if (value instanceof Byte) {
        Byte bValue = (Byte) value;
        writeParamSize(byteBuf, 1, 1);
        byteBuf.writeByte(bValue);
      } else if (value instanceof Short) {
        Short sValue = (Short) value;
        writeParamSize(byteBuf, 2, 2);
        byteBuf.writeShortLE(sValue);
      } else if (value instanceof Integer) {
        Integer iValue = (Integer) value;
        writeParamSize(byteBuf, 4, 4);
        byteBuf.writeIntLE(iValue);
      } else if (value instanceof Long) {
        Long lValue = (Long) value;
        writeParamSize(byteBuf, 8, 8);
        byteBuf.writeLongLE(lValue);
      } else throw new IllegalArgumentException(value.getClass().getName());
    }
  },
  BITN(0x68) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.BOOLEAN;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 1) return byteBuf.readBoolean();
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public String paramDefinition(Object value) {
      return "bit";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, BITN.id);
      writeParamSize(byteBuf, 1, 1);
      byteBuf.writeBoolean((Boolean) value);
    }
  },
  DECIMALN(0x6A) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo()
        .maxLength(byteBuf.readUnsignedByte())
        .precision(byteBuf.readByte())
        .scale(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DECIMAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      short length = byteBuf.readUnsignedByte();
      if (length == 0) return null;
      byte sign = byteBuf.readByte();
      byte[] bytes = new byte[length - 1];
      for (int i = 0; i < bytes.length; i++) bytes[i] = byteBuf.getByte(byteBuf.readerIndex() + bytes.length - 1 - i);
      byteBuf.skipBytes(bytes.length);
      BigInteger bigInteger = new BigInteger(bytes);
      BigDecimal bigDecimal = new BigDecimal(bigInteger, typeInfo.scale());
      return sign == 0 ? bigDecimal.negate() : bigDecimal;
    }

    @Override
    public String paramDefinition(Object value) {
      return "numeric(38," + (value == null ? 0 : Math.max(0, ((BigDecimal) value).scale())) + ")";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      BigDecimal bigDecimal = (BigDecimal) value;
      writeParamDescription(byteBuf, name, out, id);
      writeParamSize(byteBuf, 17, 38);
      int sign = bigDecimal.signum() < 0 ? 0 : 1;
      byte[] bytes = (sign == 0 ? bigDecimal.negate() : bigDecimal).unscaledValue().toByteArray();
      byteBuf.writeByte(Math.max(0, bigDecimal.scale()));
      byteBuf.writeByte(1 + bytes.length);
      byteBuf.writeByte(sign);
      for (int i = bytes.length - 1; i >= 0; i--) byteBuf.writeByte(bytes[i]);
    }
  },
  NUMERICN(0x6C) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return DECIMALN.decodeTypeInfo(byteBuf);
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DECIMAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return DECIMALN.decodeValue(byteBuf, typeInfo);
    }
  },
  FLTN(0x6D) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      if (typeInfo.maxLength() == 4) return JDBCType.REAL;
      if (typeInfo.maxLength() == 8) return JDBCType.DOUBLE;
      throw new IllegalArgumentException("Invalid length: " + typeInfo.maxLength());
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 4) return byteBuf.readFloatLE();
      if (length == 8) return byteBuf.readDoubleLE();
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public String paramDefinition(Object value) {
      return "float";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      if (value instanceof Float) {
        Float fValue = (Float) value;
        writeParamSize(byteBuf, 4, 4);
        byteBuf.writeFloatLE(fValue);
      } else if (value instanceof Double) {
        Double dValue = (Double) value;
        writeParamSize(byteBuf, 8, 8);
        byteBuf.writeDoubleLE(dValue);
      } else throw new IllegalArgumentException();
    }
  },
  MONEYN(0x6E) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().scale(byteBuf.readByte());
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 4) return MONEY4.decodeValue(byteBuf, typeInfo);
      if (length == 8) return MONEY.decodeValue(byteBuf, typeInfo);
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DECIMAL;
    }
  },
  DATETIMN(0x6F) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().scale(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      byte length = byteBuf.readByte();
      if (length == 0) return null;

      if (length == 8) {
        return decodeIntLEDateValue(byteBuf);
      } else if (length == 4) {
        return decodeUnsignedShortDateValue(byteBuf);
      } else {
        throw new UnsupportedOperationException("Invalid length for date " + name());
      }
    }

    @Override
    public String paramDefinition(Object value) {
      return "datetime";
    }
  },
  DATEN(0x28) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.DATE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      return decodeLocalDate(byteBuf, length);
    }

    @Override
    public String paramDefinition(Object value) {
      return "date";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      byteBuf.writeByte(3);
      byteBuf.writeMediumLE(daysFromStartDate((LocalDate) value));
    }
  },
  TIMEN(0x29) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().scale(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIME;
    }

    @Override
    public String paramDefinition(Object value) {
      return "time";
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      return decodeLocalTime(byteBuf, length, typeInfo.scale());
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      writeParamSize(byteBuf, 7, 5);
      writeUnsignedInt40LE(byteBuf, hundredsOfNanos((LocalTime) value));
    }
  },
  DATETIME2N(0x2A) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().scale(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      byte length = byteBuf.readByte();
      if (length == 0) return null;
      LocalTime localTime = decodeLocalTime(byteBuf, length - 3, typeInfo.scale());
      LocalDate localDate = decodeLocalDate(byteBuf, 3);
      return LocalDateTime.of(localDate, localTime);
    }

    @Override
    public String paramDefinition(Object value) {
      return "datetime2";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      LocalDateTime localDateTime = (LocalDateTime) value;
      writeParamDescription(byteBuf, name, out, id);
      writeParamSize(byteBuf, 7, 8);
      writeUnsignedInt40LE(byteBuf, hundredsOfNanos(localDateTime.toLocalTime()));
      byteBuf.writeMediumLE(daysFromStartDate(localDateTime.toLocalDate()));
    }
  },
  DATETIMEOFFSETN(0x2B) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().scale(byteBuf.readByte());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.TIMESTAMP_WITH_TIMEZONE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      byte length = byteBuf.readByte();
      if (length == 0) return null;
      LocalTime localTime = decodeLocalTime(byteBuf, length - 5, typeInfo.scale());
      LocalDate localDate = decodeLocalDate(byteBuf, 3);
      short minutes = byteBuf.readShortLE();
      return LocalDateTime.of(localDate, localTime).plusMinutes(minutes).atOffset(ZoneOffset.ofTotalSeconds(60 * minutes));
    }

    @Override
    public String paramDefinition(Object value) {
      return "datetimeoffset";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      OffsetDateTime offsetDateTime = (OffsetDateTime) value;
      writeParamDescription(byteBuf, name, out, id);
      writeParamSize(byteBuf, 7, 10);
      int offsetMinutes = offsetDateTime.getOffset().getTotalSeconds() / 60;
      LocalDateTime localDateTime = offsetDateTime.toLocalDateTime().minusMinutes(offsetMinutes);
      writeUnsignedInt40LE(byteBuf, hundredsOfNanos(localDateTime.toLocalTime()));
      byteBuf.writeMediumLE(daysFromStartDate(localDateTime.toLocalDate()));
      byteBuf.writeShortLE(offsetMinutes);
    }
  },
  CHAR(0x2F),
  VARCHAR(0x27),
  BINARY(0x2D) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.BINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return BIGVARBINARY.decodeValue(byteBuf, typeInfo);
    }
  },
  VARBINARY(0x25) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.VARBINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return BIGVARBINARY.decodeValue(byteBuf, typeInfo);
    }
  },

  BIGVARBINARY(0xA5) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.LONGVARBINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      Object result;
      if (isPLP(typeInfo)) {
        long payloadLength = byteBuf.readLongLE();
        if (isPLPNull(payloadLength)) {
          result = null;
        } else {
          result = Buffer.buffer(readPLP(byteBuf));
        }
      } else {
        int length = byteBuf.readUnsignedShortLE();
        if (length == 0xFFFF) {
          result = null;
        } else {
          result = decodeBinaryValue(byteBuf, length);
        }
      }
      return result;
    }

    @Override
    public String paramDefinition(Object value) {
      String definition;
      if (value == null) {
        definition = "binary(1)";
      } else if (((Buffer) value).length() > 8000) {
        definition = "varbinary(max)";
      } else {
        definition = "varbinary(8000)";
      }
      return definition;
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      Buffer buffer = (Buffer) value;
      writeParamDescription(byteBuf, name, out, id);
      if (buffer.length() > 8000) {
        byteBuf.writeShortLE(0xFFFF);
        byteBuf.writeLongLE(buffer.length());
        byteBuf.writeIntLE(buffer.length());
        byteBuf.writeBytes(buffer.getByteBuf());
        byteBuf.writeIntLE(0);
      } else {
        byteBuf.writeShortLE(buffer.length()); // max length
        byteBuf.writeShortLE(buffer.length()); // length
        byteBuf.writeBytes(buffer.getByteBuf());
      }
    }
  },
  BIGVARCHAR(0xA7) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
      decodeCharacterMetadata(typeInfo, byteBuf, null);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.VARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return NVARCHAR.decodeValue(byteBuf, typeInfo);
    }
  },
  BIGBINARY(0xAD) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      return new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.BINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return BIGVARBINARY.decodeValue(byteBuf, typeInfo);
    }
  },
  BIGCHAR(0xAF) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
      decodeCharacterMetadata(typeInfo, byteBuf, null);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.CHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return BIGVARCHAR.decodeValue(byteBuf, typeInfo);
    }
  },
  NVARCHAR(0xE7) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
      decodeCharacterMetadata(typeInfo, byteBuf, StandardCharsets.UTF_16LE);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.VARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      Object result;
      if (isPLP(typeInfo)) {
        long payloadLength = byteBuf.readLongLE();
        if (isPLPNull(payloadLength)) {
          result = null;
        } else {
          result = readPLP(byteBuf).toString(typeInfo.charset());
        }
      } else {
        int length = byteBuf.readUnsignedShortLE();
        if (length == 0xFFFF) {
          result = null;
        } else {
          result = byteBuf.readCharSequence(length, typeInfo.charset());
        }
      }
      return result;
    }

    @Override
    public String paramDefinition(Object value) {
      String val = stringRepresentation(value);
      return val != null && val.length() > 4000 ? "nvarchar(max)" : "nvarchar(4000)";
    }

    private String stringRepresentation(Object value) {
      return value == null ? null : value.getClass().isEnum() ? ((Enum<?>) value).name() : value.toString();
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      writeParamDescription(byteBuf, name, out, id);
      String val = stringRepresentation(value);
      if (val.length() > 4000) {
        byteBuf.writeShortLE(0xFFFF);
        writeCollation(byteBuf);
        byteBuf.writeLongLE(val.length() * 2L);
        byteBuf.writeIntLE(val.length() * 2);
        byteBuf.writeCharSequence(val, StandardCharsets.UTF_16LE);
        byteBuf.writeIntLE(0);
      } else {
        byteBuf.writeShortLE(8000);
        writeCollation(byteBuf);
        byteBuf.writeShortLE(val.length() * 2);
        byteBuf.writeCharSequence(val, StandardCharsets.UTF_16LE);
      }
    }

    private void writeCollation(ByteBuf byteBuf) {
      byteBuf.writeInt(0x0904d000);
      byteBuf.writeByte(0x34);
    }
  },
  NCHAR(0xEF) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readUnsignedShortLE());
      decodeCharacterMetadata(typeInfo, byteBuf, StandardCharsets.UTF_16LE);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.CHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return NVARCHAR.decodeValue(byteBuf, typeInfo);
    }
  },
  XML(0xF1),
  UDT(0xF0),

  TEXT(0x23) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readIntLE());
      decodeCharacterMetadata(typeInfo, byteBuf, null);
      skipMultipartTableName(byteBuf);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.LONGVARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      return NTEXT.decodeValue(byteBuf, typeInfo);
    }
  },
  IMAGE(0x22) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readIntLE());
      skipMultipartTableName(byteBuf);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.LONGVARBINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      Buffer result;
      if (byteBuf.readUnsignedByte() == 0) {
        result = null;
      } else {
        byteBuf.skipBytes(24);
        int length = byteBuf.readIntLE();
        result = decodeBinaryValue(byteBuf, length);
      }
      return result;
    }
  },
  NTEXT(0x63) {
    @Override
    public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
      TypeInfo typeInfo = new TypeInfo().maxLength(byteBuf.readIntLE());
      decodeCharacterMetadata(typeInfo, byteBuf, StandardCharsets.UTF_16LE);
      skipMultipartTableName(byteBuf);
      return typeInfo;
    }

    @Override
    public JDBCType jdbcType(TypeInfo typeInfo) {
      return JDBCType.LONGVARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
      Object result;
      if (byteBuf.readUnsignedByte() == 0) {
        result = null;
      } else {
        byteBuf.skipBytes(24);
        int length = byteBuf.readIntLE();
        result = byteBuf.readCharSequence(length, typeInfo.charset());
      }
      return result;
    }
  },
  SSVARIANT(0x62);

  public final int id;

  DataType(int id) {
    this.id = id;
  }

  private static void skipMultipartTableName(ByteBuf byteBuf) {
    int numParts = byteBuf.readUnsignedByte();
    for (int i = 0; i < numParts; i++) {
      byteBuf.skipBytes(2 * byteBuf.readUnsignedShortLE());
    }
  }

  private static void decodeCharacterMetadata(TypeInfo typeInfo, ByteBuf byteBuf, Charset charset) {
    if (charset != null) {
      typeInfo.charset(charset);
      byteBuf.skipBytes(5);
    } else {
      typeInfo.charset(Encoding.readCharsetFrom(byteBuf));
    }
  }

  private static boolean isPLP(TypeInfo typeInfo) {
    return typeInfo.maxLength() == 0xFFFF;
  }

  private static boolean isPLPNull(long payloadLength) {
    return payloadLength == 0xFFFFFFFFFFFFFFFFL;
  }

  private static ByteBuf readPLP(ByteBuf byteBuf) {
    final int startIndex = byteBuf.readerIndex();
    int nextIndex = startIndex;
    int totalSize = 0;
    for (int chunkSize = (int) byteBuf.getUnsignedIntLE(nextIndex); chunkSize > 0; chunkSize = (int) byteBuf.getUnsignedIntLE(nextIndex)) {
      totalSize += chunkSize;
      nextIndex += 4 + chunkSize;
    }
    ByteBuf heapBuffer = VertxByteBufAllocator.DEFAULT.heapBuffer(totalSize);
    nextIndex = startIndex;
    for (int chunkSize = (int) byteBuf.getUnsignedIntLE(nextIndex); chunkSize > 0; chunkSize = (int) byteBuf.getUnsignedIntLE(nextIndex)) {
      heapBuffer.writeBytes(byteBuf, nextIndex + 4, chunkSize);
      nextIndex += 4 + chunkSize;
    }
    byteBuf.readerIndex(nextIndex + 4);
    return heapBuffer;
  }

  private static Buffer decodeBinaryValue(ByteBuf byteBuf, int length) {
    ByteBuf unpooled = Unpooled.buffer(length);
    byteBuf.readBytes(unpooled, 0, length);
    unpooled.writerIndex(length);
    return Buffer.buffer(unpooled);
  }

  private static LocalDateTime decodeIntLEDateValue(ByteBuf byteBuf) {
    LocalDate localDate = START_DATE_DATETIME.plus(byteBuf.readIntLE(), ChronoUnit.DAYS);
    long nanoOfDay = NANOSECONDS.convert(Math.round(byteBuf.readIntLE() * (3 + 1D / 3)), MILLISECONDS);
    LocalTime localTime = LocalTime.ofNanoOfDay(nanoOfDay);
    return LocalDateTime.of(localDate, localTime);
  }

  private static LocalDateTime decodeUnsignedShortDateValue(ByteBuf byteBuf) {
    LocalDate localDate = START_DATE_DATETIME.plus(byteBuf.readUnsignedShortLE(), ChronoUnit.DAYS);
    LocalTime localTime = LocalTime.ofSecondOfDay(byteBuf.readUnsignedShortLE() * 60L);
    return LocalDateTime.of(localDate, localTime);
  }

  public TypeInfo decodeTypeInfo(ByteBuf byteBuf) {
    throw new UnsupportedOperationException("Unable to decode typeInfo for " + name());
  }

  public JDBCType jdbcType(TypeInfo typeInfo) {
    throw new UnsupportedOperationException("Unable to determine jdbc type for " + name());
  }

  public Object decodeValue(ByteBuf byteBuf, TypeInfo typeInfo) {
    throw new UnsupportedOperationException("Unable to decode value for " + name());
  }

  public String paramDefinition(Object value) {
    throw new UnsupportedOperationException("Unable to generate param definition for " + name());
  }

  public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
    throw new UnsupportedOperationException("Unable to encode param for " + name());
  }

  private static final LocalDate START_DATE = LocalDate.of(1, 1, 1);
  private static final LocalDate START_DATE_DATETIME = LocalDate.of(1900, 1, 1);
  private static final IntObjectMap<DataType> typesById;
  private static final Map<Class<?>, DataType> typesByValueClass;

  static {
    typesById = new IntObjectHashMap<>(values().length);
    for (DataType dataType : values()) typesById.put(dataType.id, dataType);
    typesByValueClass = new HashMap<>();
    typesByValueClass.put(Byte.class, INTN);
    typesByValueClass.put(Short.class, INTN);
    typesByValueClass.put(Integer.class, INTN);
    typesByValueClass.put(Long.class, INTN);
    typesByValueClass.put(Boolean.class, BITN);
    typesByValueClass.put(Float.class, FLTN);
    typesByValueClass.put(Double.class, FLTN);
    typesByValueClass.put(BigDecimal.class, DECIMALN);
    typesByValueClass.put(String.class, NVARCHAR);
    typesByValueClass.put(LocalDate.class, DATEN);
    typesByValueClass.put(LocalTime.class, TIMEN);
    typesByValueClass.put(LocalDateTime.class, DATETIME2N);
    typesByValueClass.put(OffsetDateTime.class, DATETIMEOFFSETN);
    typesByValueClass.put(UUID.class, GUID);
    typesByValueClass.put(Buffer.class, BIGVARBINARY);
  }

  public static DataType forId(int id) {
    DataType dataType = typesById.get(id);
    if (dataType == null) throw new IllegalArgumentException("Unknown data type: " + id);
    return dataType;
  }

  public static DataType forValueClass(Class<?> valueClass) {
    DataType dataType;
    if (Buffer.class.isAssignableFrom(valueClass)) {
      dataType = typesByValueClass.get(Buffer.class);
    } else if (valueClass.isEnum()) {
      dataType = typesByValueClass.get(String.class);
    } else {
      dataType = typesByValueClass.get(valueClass);
    }
    if (dataType == null) {
      throw new IllegalArgumentException("Unsupported value class: " + valueClass);
    }
    return dataType;
  }

  private static void writeParamDescription(ByteBuf buffer, String name, boolean out, int id) {
    writeByteLengthString(buffer, name);
    buffer.writeShort((out ? 1 : 0) << 8 | id & 0xFF);
  }

  private static void writeParamSize(ByteBuf buffer, int i, int j) {
    buffer.writeShort(i << 8 | j & 0xFF);
  }

  private static LocalDate decodeLocalDate(ByteBuf byteBuf, int length) {
    int days;
    if (length == 3) {
      days = byteBuf.readUnsignedMediumLE();
      return START_DATE.plus(days, ChronoUnit.DAYS);
    }
    throw new IllegalArgumentException("Invalid length: " + length);
  }

  private static LocalTime decodeLocalTime(ByteBuf byteBuf, int length, int scale) {
    long hundredNanos;
    if (length == 3) {
      hundredNanos = byteBuf.readUnsignedMediumLE();
    } else if (length == 4) {
      hundredNanos = byteBuf.readUnsignedIntLE();
    } else if (length == 5) {
      hundredNanos = readUnsignedInt40LE(byteBuf);
    } else {
      throw new IllegalArgumentException("Invalid length: " + length);
    }
    for (int i = scale; i < 7; i++) {
      hundredNanos *= 10;
    }
    return LocalTime.ofNanoOfDay(100 * hundredNanos);
  }

  private static int daysFromStartDate(LocalDate localDate) {
    return (int) ChronoUnit.DAYS.between(START_DATE, localDate);
  }

  private static long hundredsOfNanos(LocalTime localTime) {
    return localTime.toNanoOfDay() / 100;
  }
}
