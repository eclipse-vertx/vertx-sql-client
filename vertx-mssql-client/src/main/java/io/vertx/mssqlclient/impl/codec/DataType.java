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
import io.netty.buffer.Unpooled;
import io.netty.util.collection.IntObjectHashMap;
import io.netty.util.collection.IntObjectMap;
import io.vertx.core.buffer.Buffer;

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
import java.util.stream.Stream;

import static io.vertx.mssqlclient.impl.utils.ByteBufUtils.*;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;

public enum DataType {

  // Zero-Length Data Types https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/bc91c82f-8ee0-4256-98d9-c800bf9ae33b
  NULL(0x1F) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TINYINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readUnsignedByte();
    }
  },
  BIT(0x32) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.BOOLEAN;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readBoolean();
    }
  },
  INT2(0x34) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.SMALLINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readShortLE();
    }
  },
  INT4(0x38) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.INTEGER;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readIntLE();
    }
  },
  DATETIM4(0x3A) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeUnsignedShortDateValue(byteBuf);
    }

    @Override
    public String paramDefinition(Object value) {
      return "smalldatetime";
    }
  },
  FLT4(0x3B) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.REAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readFloatLE();
    }
  },
  MONEY(0x3C) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      long highBits = (long) byteBuf.readIntLE() << 32;
      long lowBits = byteBuf.readIntLE() & 0xFFFFFFFFL;
      BigInteger bigInteger = BigInteger.valueOf(highBits | lowBits);
      return new BigDecimal(bigInteger).divide(new BigDecimal("10000"), 4, RoundingMode.UP);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DECIMAL;
    }
  },
  DATETIME(0x3D) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeIntLEDateValue(byteBuf);
    }
  },
  FLT8(0x3E) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DOUBLE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readDoubleLE();
    }
  },
  MONEY4(0x7A){
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return new BigDecimal(byteBuf.readIntLE()).divide(new BigDecimal("10000"), 2, RoundingMode.UP);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DECIMAL;
    }
  },
  INT8(0x7F) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.BIGINT;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return byteBuf.readLongLE();
    }
  },
  DECIMAL(0x37),
  NUMERIC(0x3F),

  // Variable-Length Data Types https://docs.microsoft.com/en-us/openspecs/windows_protocols/ms-tds/ce3183a6-9d89-47e8-a02f-de5a1a1303de
  GUID(0x24) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      if (metadata.length == 16) return JDBCType.OTHER;
      throw new IllegalArgumentException("Invalid length: " + metadata.length);
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 16) {
        long first = byteBuf.readIntLE() & 0xFFFFFFFF;
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      if (metadata.length == 1) return JDBCType.TINYINT;
      if (metadata.length == 2) return JDBCType.SMALLINT;
      if (metadata.length == 4) return JDBCType.INTEGER;
      if (metadata.length == 8) return JDBCType.BIGINT;
      throw new IllegalArgumentException("Invalid length: " + metadata.length);
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.BOOLEAN;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readUnsignedByte();
      metadata.precision = byteBuf.readByte();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DECIMAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      short length = byteBuf.readUnsignedByte();
      if (length == 0) return null;
      byte sign = byteBuf.readByte();
      byte[] bytes = new byte[length - 1];
      for (int i = 0; i < bytes.length; i++) bytes[i] = byteBuf.getByte(byteBuf.readerIndex() + bytes.length - 1 - i);
      byteBuf.skipBytes(bytes.length);
      BigInteger bigInteger = new BigInteger(bytes);
      BigDecimal bigDecimal = new BigDecimal(bigInteger, metadata.scale);
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return DECIMALN.decodeMetadata(byteBuf);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DECIMAL;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return DECIMALN.decodeValue(byteBuf, metadata);
    }
  },
  FLTN(0x6D) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      if (metadata.length == 4) return JDBCType.REAL;
      if (metadata.length == 8) return JDBCType.DOUBLE;
      throw new IllegalArgumentException("Invalid length: " + metadata.length);
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      if (length == 4) return MONEY4.decodeValue(byteBuf, metadata);
      if (length == 8) return MONEY.decodeValue(byteBuf, metadata);
      throw new IllegalArgumentException("Invalid length: " + length);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DECIMAL;
    }
  },
  DATETIMN(0x6F) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return null;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.DATE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIME;
    }

    @Override
    public String paramDefinition(Object value) {
      return "time";
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      int length = byteBuf.readByte();
      if (length == 0) return null;
      return decodeLocalTime(byteBuf, length, metadata.scale);
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIMESTAMP;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      byte length = byteBuf.readByte();
      if (length == 0) return null;
      LocalTime localTime = decodeLocalTime(byteBuf, length - 3, metadata.scale);
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.scale = byteBuf.readByte();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.TIMESTAMP_WITH_TIMEZONE;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      byte length = byteBuf.readByte();
      if (length == 0) return null;
      LocalTime localTime = decodeLocalTime(byteBuf, length - 5, metadata.scale);
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return BIGVARBINARY.decodeMetadata(byteBuf);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.BINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return BIGVARBINARY.decodeValue(byteBuf, metadata);
    }
  },
  VARBINARY(0x25) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return BIGVARBINARY.decodeMetadata(byteBuf);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.VARBINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return BIGVARBINARY.decodeValue(byteBuf, metadata);
    }
  },

  BIGVARBINARY(0xA5) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      Metadata metadata = new Metadata();
      metadata.length = byteBuf.readUnsignedShortLE();
      return metadata;
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.LONGVARBINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      int length = byteBuf.readUnsignedShortLE();
      ByteBuf res = Unpooled.buffer(length);
      byteBuf.readBytes(res, 0, length);
      res.writerIndex(length);
      return Buffer.buffer(res);
    }

    @Override
    public String paramDefinition(Object value) {
      return "binary(" + (value == null ? 1 : ((Buffer) value).length()) + ")";
    }

    @Override
    public void encodeParam(ByteBuf byteBuf, String name, boolean out, Object value) {
      Buffer buffer = (Buffer) value;
      writeParamDescription(byteBuf, name, out, id);
      byteBuf.writeShortLE(buffer.length()); // max length
      byteBuf.writeShortLE(buffer.length()); // length
      byteBuf.writeBytes(buffer.getByteBuf());
    }
  },
  BIGVARCHAR(0xA7) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return decodeCharacterMetadata(byteBuf, null);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.VARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeCharacterValue(byteBuf, metadata);
    }
  },
  BIGBINARY(0xAD) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return BIGVARBINARY.decodeMetadata(byteBuf);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.BINARY;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return BIGVARBINARY.decodeValue(byteBuf, metadata);
    }
  },
  BIGCHAR(0xAF) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return decodeCharacterMetadata(byteBuf, null);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.CHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeCharacterValue(byteBuf, metadata);
    }
  },
  NVARCHAR(0xE7) {
    @Override
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return decodeCharacterMetadata(byteBuf, StandardCharsets.UTF_16LE);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.VARCHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeCharacterValue(byteBuf, metadata);
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
    public Metadata decodeMetadata(ByteBuf byteBuf) {
      return decodeCharacterMetadata(byteBuf, StandardCharsets.UTF_16LE);
    }

    @Override
    public JDBCType jdbcType(Metadata metadata) {
      return JDBCType.CHAR;
    }

    @Override
    public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
      return decodeCharacterValue(byteBuf, metadata);
    }
  },
  XML(0xF1),
  UDT(0xF0),

  TEXT(0x23),
  IMAGE(0x22),
  NTEXT(0x63),
  SSVARIANT(0x62);

  public final int id;

  DataType(int id) {
    this.id = id;
  }

  public static class Metadata {
    private int length;
    private byte precision;
    private byte scale;
    private Charset charset;

    public int length() {
      return length;
    }

    public byte precision() {
      return precision;
    }

    public byte scale() {
      return scale;
    }

    public Charset charset() {
      return charset;
    }

    @Override
    public String toString() {
      return "Metadata{" +
        "length=" + length +
        ", precision=" + precision +
        ", scale=" + scale +
        ", charset=" + charset +
        '}';
    }
  }

  private static Metadata decodeCharacterMetadata(ByteBuf byteBuf, Charset charset) {
    Metadata metadata = new Metadata();
    metadata.length = byteBuf.readUnsignedShortLE();
    if (charset != null) {
      metadata.charset = charset;
      byteBuf.skipBytes(5);
    } else {
      metadata.charset = Encoding.readCharsetFrom(byteBuf);
    }
    return metadata;
  }

  private static Object decodeCharacterValue(ByteBuf byteBuf, Metadata metadata) {
    Object result;
    if (metadata.length == 0xFFFF) { // PLP (partially length-prefixed)
      long payloadLength = byteBuf.readLongLE();
      if (payloadLength == 0xFFFFFFFFFFFFFFFFL) { // PLP null
        result = null;
      } else {
        Stream.Builder<ByteBuf> byteBufs = Stream.builder();
        for (int chunkSize = (int) byteBuf.readUnsignedIntLE(); chunkSize > 0; chunkSize = (int) byteBuf.readUnsignedIntLE()) {
          byteBufs.add(byteBuf.slice(byteBuf.readerIndex(), chunkSize));
          byteBuf.skipBytes(chunkSize);
        }
        ByteBuf wrapped = Unpooled.wrappedBuffer(byteBufs.build().toArray(ByteBuf[]::new));
        result = wrapped.toString(metadata.charset);
      }
    } else { // Length-prefixed
      int length = byteBuf.readUnsignedShortLE();
      result = length == 0xFFFF ? null : byteBuf.readCharSequence(length, metadata.charset);
    }
    return result;
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

  public Metadata decodeMetadata(ByteBuf byteBuf) {
    throw new UnsupportedOperationException("Unable to decode metadata for " + name());
  }

  public JDBCType jdbcType(Metadata metadata) {
    throw new UnsupportedOperationException("Unable to determine jdbc type for " + name());
  }

  public Object decodeValue(ByteBuf byteBuf, Metadata metadata) {
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
