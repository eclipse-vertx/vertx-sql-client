package io.vertx.mysqlclient.impl.datatype;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.impl.MySQLCollation;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.codec.CommonCodec;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

public class DataTypeCodec {
  // binary codec protocol: https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row_value

  // Sentinel used when an object is refused by the data type
  public static final Object REFUSED_SENTINEL = new Object();

  private static final java.time.format.DateTimeFormatter DATETIME_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .appendValue(HOUR_OF_DAY, 2)
    .appendLiteral(':')
    .appendValue(MINUTE_OF_HOUR, 2)
    .appendLiteral(':')
    .appendValue(SECOND_OF_MINUTE, 2)
    .appendFraction(MICRO_OF_SECOND, 0, 6, true)
    .toFormatter();

  public static Object decodeText(DataType dataType, int collationId, int columnDefinitionFlags, ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    int index = buffer.readerIndex();
    try {
      switch (dataType) {
        case INT1:
          if (isUnsignedNumeric(columnDefinitionFlags)) {
            return textDecodeInt2(buffer, index, length);
          } else {
            return textDecodeInt1(buffer, index, length);
          }
        case YEAR:
          return textDecodeInt2(buffer, index, length);
        case INT2:
          if (isUnsignedNumeric(columnDefinitionFlags)) {
            return textDecodeInt4(buffer, index, length);
          } else {
            return textDecodeInt2(buffer, index, length);
          }
        case INT3:
          return textDecodeInt4(buffer, index, length);
        case INT4:
          if (isUnsignedNumeric(columnDefinitionFlags)) {
            return textDecodeInt8(buffer, index, length);
          } else {
            return textDecodeInt4(buffer, index, length);
          }
        case INT8:
          if (isUnsignedNumeric(columnDefinitionFlags)) {
            return textDecodeNUMERIC(collationId, buffer, index, length);
          } else {
            return textDecodeInt8(buffer, index, length);
          }
        case FLOAT:
          return textDecodeFloat(collationId, buffer, index, length);
        case DOUBLE:
          return textDecodeDouble(collationId, buffer, index, length);
        case BIT:
          return textDecodeBit(buffer, index, length);
        case NUMERIC:
          return textDecodeNUMERIC(collationId, buffer, index, length);
        case DATE:
          return textDecodeDate(collationId, buffer, index, length);
        case TIME:
          return textDecodeTime(collationId, buffer, index, length);
        case DATETIME:
        case TIMESTAMP:
          return textDecodeDateTime(collationId, buffer, index, length);
        case JSON:
          return textDecodeJson(collationId, buffer, index, length);
        case STRING:
        case VARSTRING:
        case BLOB:
        default:
          return textDecodeBlobOrText(collationId, columnDefinitionFlags, buffer, index, length);
      }
    } finally {
      buffer.readerIndex(index + length);
    }
  }

  public static void encodeBinary(DataType dataType, Object value, Charset charset, ByteBuf buffer) {
    switch (dataType) {
      case INT1:
        if (value instanceof Boolean) {
          if ((Boolean) value) {
            value = 1;
          } else {
            value = 0;
          }
        }
        binaryEncodeInt1((Number) value, buffer);
        break;
      case INT2:
        binaryEncodeInt2((Number) value, buffer);
        break;
      case INT3:
        binaryEncodeInt3((Number) value, buffer);
        break;
      case INT4:
        binaryEncodeInt4((Number) value, buffer);
        break;
      case INT8:
        binaryEncodeInt8((Number) value, buffer);
        break;
      case FLOAT:
        binaryEncodeFloat((Number) value, buffer);
        break;
      case DOUBLE:
        binaryEncodeDouble((Number) value, buffer);
        break;
      case NUMERIC:
        binaryEncodeNumeric((Numeric) value, buffer, charset);
        break;
      case BLOB:
        binaryEncodeBlob((Buffer) value, buffer);
        break;
      case DATE:
        binaryEncodeDate((LocalDate) value, buffer);
        break;
      case TIME:
        if (value instanceof LocalTime) {
          binaryEncodeTime((LocalTime) value, buffer);
        } else {
          binaryEncodeTime((Duration) value, buffer);
        }
        break;
      case DATETIME:
        binaryEncodeDatetime((LocalDateTime) value, buffer);
        break;
      case JSON: // this is unused normally
      case STRING:
      case VARSTRING:
      default:
        if (value instanceof JsonObject || value instanceof JsonArray) {
          binaryEncodeJson(value, buffer, charset);
          return;
        } else if (value == Tuple.JSON_NULL) {
          // we have to make JSON literal null send as a STRING data type
          BufferUtils.writeLengthEncodedString(buffer, "null", charset);
        } else {
          binaryEncodeText(String.valueOf(value), buffer, charset);
        }
        break;
    }
  }

  public static Object decodeBinary(DataType dataType, int collationId, int columnDefinitionFlags, ByteBuf buffer) {
    switch (dataType) {
      case INT1:
        if (isUnsignedNumeric(columnDefinitionFlags)) {
          return binaryDecodeUnsignedInt1(buffer);
        } else {
          return binaryDecodeInt1(buffer);
        }
      case YEAR:
        return binaryDecodeInt2(buffer);
      case INT2:
        if (isUnsignedNumeric(columnDefinitionFlags)) {
          return binaryDecodeUnsignedInt2(buffer);
        } else {
          return binaryDecodeInt2(buffer);
        }
      case INT3:
        if (isUnsignedNumeric(columnDefinitionFlags)) {
          return binaryDecodeUnsignedInt3(buffer);
        } else {
          return binaryDecodeInt3(buffer);
        }
      case INT4:
        if (isUnsignedNumeric(columnDefinitionFlags)) {
          return binaryDecodeUnsignedInt4(buffer);
        } else {
          return binaryDecodeInt4(buffer);
        }
      case INT8:
        if (isUnsignedNumeric(columnDefinitionFlags)) {
          return binaryDecodeUnsignedInt8(buffer);
        } else {
          return binaryDecodeInt8(buffer);
        }
      case FLOAT:
        return binaryDecodeFloat(buffer);
      case DOUBLE:
        return binaryDecodeDouble(buffer);
      case BIT:
        return binaryDecodeBit(buffer);
      case NUMERIC:
        return binaryDecodeNumeric(collationId, buffer);
      case DATE:
        return binaryDecodeDate(buffer);
      case TIME:
        return binaryDecodeTime(buffer);
      case DATETIME:
      case TIMESTAMP:
        return binaryDecodeDatetime(buffer);
      case JSON:
        return binaryDecodeJson(collationId, buffer);
      case STRING:
      case VARSTRING:
      case BLOB:
      default:
        return binaryDecodeBlobOrText(collationId, columnDefinitionFlags, buffer);
    }
  }

  public static DataType inferDataTypeByEncodingValue(Object value) {
    if (value == null) {
      // ProtocolBinary::MYSQL_TYPE_NULL
      return DataType.NULL;
    } else if (value instanceof Byte) {
      // ProtocolBinary::MYSQL_TYPE_TINY
      return DataType.INT1;
    } else if (value instanceof Boolean) {
      // ProtocolBinary::MYSQL_TYPE_TINY
      return DataType.INT1;
    } else if (value instanceof Short) {
      // ProtocolBinary::MYSQL_TYPE_SHORT, ProtocolBinary::MYSQL_TYPE_YEAR
      return DataType.INT2;
    } else if (value instanceof Integer) {
      // ProtocolBinary::MYSQL_TYPE_LONG, ProtocolBinary::MYSQL_TYPE_INT24
      return DataType.INT4;
    } else if (value instanceof Long) {
      // ProtocolBinary::MYSQL_TYPE_LONGLONG
      return DataType.INT8;
    } else if (value instanceof Double) {
      // ProtocolBinary::MYSQL_TYPE_DOUBLE
      return DataType.DOUBLE;
    } else if (value instanceof Float) {
      // ProtocolBinary::MYSQL_TYPE_FLOAT
      return DataType.FLOAT;
    } else if (value instanceof LocalDate) {
      // ProtocolBinary::MYSQL_TYPE_DATE
      return DataType.DATE;
    } else if (value instanceof Duration || value instanceof LocalTime) {
      // ProtocolBinary::MYSQL_TYPE_TIME
      return DataType.TIME;
    } else if (value instanceof Buffer) {
      // ProtocolBinary::MYSQL_TYPE_LONG_BLOB, ProtocolBinary::MYSQL_TYPE_MEDIUM_BLOB, ProtocolBinary::MYSQL_TYPE_BLOB, ProtocolBinary::MYSQL_TYPE_TINY_BLOB
      return DataType.BLOB;
    } else if (value instanceof LocalDateTime) {
      // ProtocolBinary::MYSQL_TYPE_DATETIME, ProtocolBinary::MYSQL_TYPE_TIMESTAMP
      return DataType.DATETIME;
    }
//    else if (value instanceof JsonObject || value instanceof JsonArray) {
////     note we don't need this in MySQL
//      // ProtocolBinary::MYSQL_TYPE_JSON
//      return DataType.JSON;
//    }
    else {
      /*
        ProtocolBinary::MYSQL_TYPE_STRING, ProtocolBinary::MYSQL_TYPE_VARCHAR, ProtocolBinary::MYSQL_TYPE_VAR_STRING,
        ProtocolBinary::MYSQL_TYPE_ENUM, ProtocolBinary::MYSQL_TYPE_SET, ProtocolBinary::MYSQL_TYPE_GEOMETRY,
        ProtocolBinary::MYSQL_TYPE_BIT, ProtocolBinary::MYSQL_TYPE_DECIMAL, ProtocolBinary::MYSQL_TYPE_NEWDECIMAL
       */
      return DataType.STRING;
    }
  }

  private static void binaryEncodeInt1(Number value, ByteBuf buffer) {
    buffer.writeByte(value.byteValue());
  }

  private static void binaryEncodeInt2(Number value, ByteBuf buffer) {
    buffer.writeShortLE(value.intValue());
  }

  private static void binaryEncodeInt3(Number value, ByteBuf buffer) {
    buffer.writeMediumLE(value.intValue());
  }

  private static void binaryEncodeInt4(Number value, ByteBuf buffer) {
    buffer.writeIntLE(value.intValue());
  }

  private static void binaryEncodeInt8(Number value, ByteBuf buffer) {
    buffer.writeLongLE(value.longValue());
  }

  private static void binaryEncodeFloat(Number value, ByteBuf buffer) {
    buffer.writeFloatLE(value.floatValue());
  }

  private static void binaryEncodeDouble(Number value, ByteBuf buffer) {
    buffer.writeDoubleLE(value.doubleValue());
  }

  private static void binaryEncodeNumeric(Numeric value, ByteBuf buffer, Charset charset) {
    BufferUtils.writeLengthEncodedString(buffer, value.toString(), charset);
  }

  private static void binaryEncodeText(String value, ByteBuf buffer, Charset charset) {
    BufferUtils.writeLengthEncodedString(buffer, value, charset);
  }

  private static void binaryEncodeBlob(Buffer value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedInteger(buffer, value.length());
    buffer.writeBytes(value.getByteBuf());
  }

  private static void binaryEncodeDate(LocalDate value, ByteBuf buffer) {
    buffer.writeByte(4);
    buffer.writeShortLE(value.getYear());
    buffer.writeByte(value.getMonthValue());
    buffer.writeByte(value.getDayOfMonth());
  }

  private static void binaryEncodeTime(LocalTime value, ByteBuf buffer) {
    int hour = value.getHour();
    int minute = value.getMinute();
    int second = value.getSecond();
    int nano = value.getNano();
    if (nano == 0) {
      if (hour == 0 && minute == 0 && second == 0) {
        buffer.writeByte(0);
      } else {
        buffer.writeByte(8);
        buffer.writeByte(0);
        buffer.writeIntLE(0);
        buffer.writeByte(hour);
        buffer.writeByte(minute);
        buffer.writeByte(second);
      }
    } else {
      int microSecond = nano / 1000;
      buffer.writeByte(12);
      buffer.writeByte(0);
      buffer.writeIntLE(0);
      buffer.writeByte(hour);
      buffer.writeByte(minute);
      buffer.writeByte(second);
      buffer.writeIntLE(microSecond);
    }
  }

  private static void binaryEncodeTime(Duration value, ByteBuf buffer) {
    long secondsOfDuration = value.getSeconds();
    int nanosOfDuration = value.getNano();
    if (secondsOfDuration == 0 && nanosOfDuration == 0) {
      buffer.writeByte(0);
      return;
    }
    byte isNegative = 0;
    if (secondsOfDuration < 0) {
      isNegative = 1;
      secondsOfDuration = -secondsOfDuration;
    }

    int days = (int) (secondsOfDuration / 86400);
    int secondsOfADay = (int) (secondsOfDuration % 86400);
    int hour = secondsOfADay / 3600;
    int minute = ((secondsOfADay % 3600) / 60);
    int second = secondsOfADay % 60;

    if (nanosOfDuration == 0) {
      buffer.writeByte(8);
      buffer.writeByte(isNegative);
      buffer.writeIntLE(days);
      buffer.writeByte(hour);
      buffer.writeByte(minute);
      buffer.writeByte(second);
      return;
    }

    int microSecond;
    if (isNegative == 1 && nanosOfDuration > 0) {
      second = second - 1;
      microSecond = (1000_000_000 - nanosOfDuration) / 1000;
    } else {
      microSecond = nanosOfDuration / 1000;
    }

    buffer.writeByte(12);
    buffer.writeByte(isNegative);
    buffer.writeIntLE(days);
    buffer.writeByte(hour);
    buffer.writeByte(minute);
    buffer.writeByte(second);
    buffer.writeIntLE(microSecond);
  }

  private static void binaryEncodeDatetime(LocalDateTime value, ByteBuf buffer) {
    int year = value.getYear();
    int month = value.getMonthValue();
    int day = value.getDayOfMonth();
    int hour = value.getHour();
    int minute = value.getMinute();
    int second = value.getSecond();
    int microsecond = value.getNano() / 1000;

    // LocalDateTime does not have a zero value of month or day
    if (hour == 0 && minute == 0 && second == 0 && microsecond == 0) {
      buffer.writeByte(4);
      buffer.writeShortLE(year);
      buffer.writeByte(month);
      buffer.writeByte(day);
    } else if (microsecond == 0) {
      buffer.writeByte(7);
      buffer.writeShortLE(year);
      buffer.writeByte(month);
      buffer.writeByte(day);
      buffer.writeByte(hour);
      buffer.writeByte(minute);
      buffer.writeByte(second);
    } else {
      buffer.writeByte(11);
      buffer.writeShortLE(year);
      buffer.writeByte(month);
      buffer.writeByte(day);
      buffer.writeByte(hour);
      buffer.writeByte(minute);
      buffer.writeByte(second);
      buffer.writeIntLE(microsecond);
    }
  }

  private static void binaryEncodeJson(Object value, ByteBuf buffer, Charset charset) {
    BufferUtils.writeLengthEncodedString(buffer, Json.encode(value), charset);
  }

  private static Byte binaryDecodeInt1(ByteBuf buffer) {
    return buffer.readByte();
  }

  private static Short binaryDecodeUnsignedInt1(ByteBuf buffer) {
    return buffer.readUnsignedByte();
  }

  private static Short binaryDecodeInt2(ByteBuf buffer) {
    return buffer.readShortLE();
  }

  private static Integer binaryDecodeUnsignedInt2(ByteBuf buffer) {
    return buffer.readUnsignedShortLE();
  }

  private static Integer binaryDecodeInt3(ByteBuf buffer) {
    return buffer.readIntLE();
  }

  private static Integer binaryDecodeUnsignedInt3(ByteBuf buffer) {
    return buffer.readIntLE() & 0xFFFFFF;
  }

  private static Integer binaryDecodeInt4(ByteBuf buffer) {
    return buffer.readIntLE();
  }

  private static Long binaryDecodeUnsignedInt4(ByteBuf buffer) {
    return buffer.readUnsignedIntLE();
  }

  private static Long binaryDecodeInt8(ByteBuf buffer) {
    return buffer.readLongLE();
  }

  private static Numeric binaryDecodeUnsignedInt8(ByteBuf buffer) {
    byte[] bigIntValue = new byte[8];
    buffer.readBytes(bigIntValue); // little endian
    for (int i = 0; i < 4; i++) {
      // swap to big endian order
      byte tmp = bigIntValue[i];
      bigIntValue[i] = bigIntValue[7-i];
      bigIntValue[7-i] = tmp;
    }
    BigInteger value = new BigInteger(1, bigIntValue);
    return Numeric.create(value);
  }

  private static Float binaryDecodeFloat(ByteBuf buffer) {
    return buffer.readFloatLE();
  }

  private static Double binaryDecodeDouble(ByteBuf buffer) {
    return buffer.readDoubleLE();
  }

  private static Long binaryDecodeBit(ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    int idx = buffer.readerIndex();
    Long result = decodeBit(buffer, buffer.readerIndex(), length);
    buffer.readerIndex(idx + length);
    return result;
  }

  private static Numeric binaryDecodeNumeric(int collationId, ByteBuf buffer) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    return Numeric.parse(BufferUtils.readLengthEncodedString(buffer, charset));
  }

  private static Object binaryDecodeBlobOrText(int collationId, int columnDefinitionFlags, ByteBuf buffer) {
    if (collationId == MySQLCollation.binary.collationId()) {
      return binaryDecodeBlob(buffer);
    } else {
      Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
      return binaryDecodeText(charset, buffer);
    }
  }

  private static Buffer binaryDecodeBlob(ByteBuf buffer) {
    int len = (int) BufferUtils.readLengthEncodedInteger(buffer);

    ByteBuf copy = Unpooled.buffer(len);
    copy.writeBytes(buffer, len);

    return Buffer.buffer(copy);
  }

  private static String binaryDecodeText(Charset charset, ByteBuf buffer) {
    return BufferUtils.readLengthEncodedString(buffer, charset);
  }

  private static LocalDateTime binaryDecodeDatetime(ByteBuf buffer) {
    if (buffer.readableBytes() == 0) {
      return null;
    }
    int length = buffer.readByte();
    if (length == 0) {
      // invalid value '0000-00-00' or '0000-00-00 00:00:00'
      return null;
    } else {
      int year = buffer.readShortLE();
      byte month = buffer.readByte();
      byte day = buffer.readByte();
      if (length == 4) {
        return LocalDateTime.of(year, month, day, 0, 0, 0);
      }
      byte hour = buffer.readByte();
      byte minute = buffer.readByte();
      byte second = buffer.readByte();
      if (length == 11) {
        int microsecond = buffer.readIntLE();
        return LocalDateTime.of(year, month, day, hour, minute, second, microsecond * 1000);
      } else if (length == 7) {
        return LocalDateTime.of(year, month, day, hour, minute, second, 0);
      }
      throw new DecoderException("Invalid Datetime");
    }
  }

  private static LocalDate binaryDecodeDate(ByteBuf buffer) {
    return binaryDecodeDatetime(buffer).toLocalDate();
  }

  private static Duration binaryDecodeTime(ByteBuf buffer) {
    byte length = buffer.readByte();
    if (length == 0) {
      return Duration.ZERO;
    } else {
      boolean isNegative = (buffer.readByte() == 1);
      int days = buffer.readIntLE();
      int hour = buffer.readByte();
      int minute = buffer.readByte();
      int second = buffer.readByte();
      if (isNegative) {
        days = -days;
        hour = -hour;
        minute = -minute;
        second = -second;
      }

      if (length == 8) {
        return Duration.ofDays(days).plusHours(hour).plusMinutes(minute).plusSeconds(second);
      }
      if (length == 12) {
        long microsecond = buffer.readUnsignedIntLE();
        if (isNegative) {
          microsecond = -microsecond;
        }
        return Duration.ofDays(days).plusHours(hour).plusMinutes(minute).plusSeconds(second).plusNanos(microsecond * 1000);
      }
      throw new DecoderException("Invalid time format");
    }
  }

  private static Object binaryDecodeJson(int collationId, ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    Object result = textDecodeJson(collationId, buffer, buffer.readerIndex(), length);
    buffer.skipBytes(length);
    return result;
  }

  private static Byte textDecodeInt1(ByteBuf buffer, int index, int length) {
    return (byte) CommonCodec.decodeDecStringToLong(index, length, buffer);
  }

  private static Short textDecodeInt2(ByteBuf buffer, int index, int length) {
    return (short) CommonCodec.decodeDecStringToLong(index, length, buffer);
  }

  private static Integer textDecodeInt3(ByteBuf buffer, int index, int length) {
    return (int) CommonCodec.decodeDecStringToLong(index, length, buffer);
  }

  private static Integer textDecodeInt4(ByteBuf buffer, int index, int length) {
    return (int) CommonCodec.decodeDecStringToLong(index, length, buffer);
  }

  private static Long textDecodeInt8(ByteBuf buffer, int index, int length) {
    return CommonCodec.decodeDecStringToLong(index, length, buffer);
  }

  private static Float textDecodeFloat(int collationId, ByteBuf buffer, int index, int length) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    return Float.parseFloat(buffer.toString(index, length, charset));
  }

  private static Double textDecodeDouble(int collationId, ByteBuf buffer, int index, int length) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    return Double.parseDouble(buffer.toString(index, length, charset));
  }

  private static Long textDecodeBit(ByteBuf buffer, int index, int length) {
    return decodeBit(buffer, index, length);
  }

  private static Number textDecodeNUMERIC(int collationId, ByteBuf buff, int index, int length) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    return Numeric.parse(buff.toString(index, length, charset));
  }

  private static Object textDecodeBlobOrText(int collationId, int columnDefinitionFlags,
    ByteBuf buffer, int index, int length) {
    if (collationId == MySQLCollation.binary.collationId()) {
      return textDecodeBlob(buffer, index, length);
    } else {
      Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
      return textDecodeText(charset, buffer, index, length);
    }
  }

  private static Buffer textDecodeBlob(ByteBuf buffer, int index, int length) {
    ByteBuf copy = Unpooled.buffer(length);
    copy.writeBytes(buffer, index, length);
    return Buffer.buffer(copy);
  }

  private static String textDecodeText(Charset charset, ByteBuf buffer, int index, int length) {
    return buffer.toString(index, length, charset);
  }

  private static LocalDate textDecodeDate(int collationId, ByteBuf buffer, int index, int length) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    CharSequence cs = buffer.toString(index, length, charset);
    return LocalDate.parse(cs);
  }

  private static Duration textDecodeTime(int collationId, ByteBuf buffer, int index, int length) {
    // HH:mm:ss or HHH:mm:ss
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    String timeString = buffer.toString(index, length, charset);
    boolean isNegative = timeString.charAt(0) == '-';
    if (isNegative) {
      timeString = timeString.substring(1);
    }

    String[] timeElements = timeString.split(":");
    if (timeElements.length != 3) {
      throw new DecoderException("Invalid time format");
    }

    int hour = Integer.parseInt(timeElements[0]);
    int minute = Integer.parseInt(timeElements[1]);
    int second = Integer.parseInt(timeElements[2].substring(0, 2));
    long nanos = 0;
    if (timeElements[2].length() > 2) {
      double fractionalSecondsPart = Double.parseDouble("0." + timeElements[2].substring(3));
      nanos = (long) (1000000000 * fractionalSecondsPart);
    }
    if (isNegative) {
      return Duration.ofHours(-hour).minusMinutes(minute).minusSeconds(second).minusNanos(nanos);
    } else {
      return Duration.ofHours(hour).plusMinutes(minute).plusSeconds(second).plusNanos(nanos);
    }
  }

  private static LocalDateTime textDecodeDateTime(int collationId, ByteBuf buffer, int index, int length) {
    Charset charset = MySQLCollation.getJavaCharsetByCollationId(collationId);
    CharSequence cs = buffer.toString(index, length, charset);
    if (cs.equals("0000-00-00 00:00:00")) {
      // Invalid datetime will be converted to zero
      return null;
    }
    return LocalDateTime.parse(cs, DATETIME_FORMAT);
  }

  private static Object textDecodeJson(int collationId, ByteBuf buffer, int index, int length) {
    Charset charset = StandardCharsets.UTF_8; // MySQL JSON data type will only be UTF-8 string
    // Try to do without the intermediary String (?)
    CharSequence cs = buffer.getCharSequence(index, length, charset);
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
      Object o = Json.decodeValue(s);
      if (o == null) {
        return Tuple.JSON_NULL;
      }
      if (o instanceof Number || o instanceof Boolean || o instanceof String) {
        return o;
      }
      return null;
    }
    return value;
  }

  private static boolean isUnsignedNumeric(int columnDefinitionFlags) {
    return (columnDefinitionFlags & ColumnDefinition.ColumnDefinitionFlags.UNSIGNED_FLAG) != 0;
  }

  private static Long decodeBit(ByteBuf buffer, int index, int length) {
    byte[] value = new byte[length];
    buffer.getBytes(index, value, 0, length);
    long result = 0;
    for (byte b : value) {
      result = (b & 0xFF) | (result << 8);
    }
    return result;
  }
}
