package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.handler.codec.DecoderException;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.nio.charset.Charset;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

class DataTypeCodec {
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

  static Object decodeText(DataType dataType, Charset charset, int columnDefinitionFlags, ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    int index = buffer.readerIndex();
    try {
      switch (dataType) {
        case INT1:
          return textDecodeInt1(charset, buffer, index, length);
        case INT2:
        case YEAR:
          return textDecodeInt2(charset, buffer, index, length);
        case INT3:
          return textDecodeInt3(charset, buffer, index, length);
        case INT4:
          return textDecodeInt4(charset, buffer, index, length);
        case INT8:
          return textDecodeInt8(charset, buffer, index, length);
        case FLOAT:
          return textDecodeFloat(charset, buffer, index, length);
        case DOUBLE:
          return textDecodeDouble(charset, buffer, index, length);
        case BIT:
          return textDecodeBit(charset, buffer, index, length);
        case NUMERIC:
          return textDecodeNUMERIC(charset, buffer, index, length);
        case DATE:
          return textDecodeDate(charset, buffer, index, length);
        case TIME:
          return textDecodeTime(charset, buffer, index, length);
        case DATETIME:
        case TIMESTAMP:
          return textDecodeDateTime(charset, buffer, index, length);
        case JSON:
          return textDecodeJson(charset, buffer, index, length);
        case STRING:
        case VARSTRING:
        case BLOB:
        default:
          return textDecodeBlobOrText(charset, columnDefinitionFlags, buffer, index, length);
      }
    } finally {
      buffer.readerIndex(index + length);
    }
  }

  //TODO take care of unsigned numeric values here?
  static void encodeBinary(DataType dataType, Object value, ByteBuf buffer) {
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
        binaryEncodeNumeric((Numeric) value, buffer);
        break;
      case BLOB:
        binaryEncodeBlob((Buffer) value, buffer);
        break;
      case DATE:
        binaryEncodeDate((LocalDate) value, buffer);
        break;
      case TIME:
        binaryEncodeTime((Duration) value, buffer);
        break;
      case DATETIME:
        binaryEncodeDatetime((LocalDateTime) value, buffer);
        break;
      case JSON: // this is unused normally
      case STRING:
      case VARSTRING:
      default:
        if (value instanceof JsonObject || value instanceof JsonArray) {
          binaryEncodeJson(value, buffer);
          return;
        } else if (value == Tuple.JSON_NULL) {
          // we have to make JSON literal null send as a STRING data type
          BufferUtils.writeLengthEncodedString(buffer, "null");
        } else {
          binaryEncodeText(String.valueOf(value), buffer);
        }
        break;
    }
  }

  static Object decodeBinary(DataType dataType, Charset charset, int columnDefinitionFlags, ByteBuf buffer) {
    switch (dataType) {
      case INT1:
        return binaryDecodeInt1(buffer);
      case YEAR:
      case INT2:
        return binaryDecodeInt2(buffer);
      case INT3:
        return binaryDecodeInt3(buffer);
      case INT4:
        return binaryDecodeInt4(buffer);
      case INT8:
        return binaryDecodeInt8(buffer);
      case FLOAT:
        return binaryDecodeFloat(buffer);
      case DOUBLE:
        return binaryDecodeDouble(buffer);
      case BIT:
        return binaryDecodeBit(buffer);
      case NUMERIC:
        return binaryDecodeNumeric(charset, buffer);
      case DATE:
        return binaryDecodeDate(buffer);
      case TIME:
        return binaryDecodeTime(buffer);
      case DATETIME:
      case TIMESTAMP:
        return binaryDecodeDatetime(buffer);
      case JSON:
        return binaryDecodeJson(charset, buffer);
      case STRING:
      case VARSTRING:
      case BLOB:
      default:
        return binaryDecodeBlobOrText(charset, columnDefinitionFlags, buffer);
    }
  }

  public static Object prepare(DataType type, Object value) {
    switch (type) {
      //TODO handle json + unknown?
      default:
        Class<?> javaType = type.binaryType;
        return value == null || javaType.isInstance(value) ? value : REFUSED_SENTINEL;
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

  private static void binaryEncodeNumeric(Numeric value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, value.toString());
  }

  private static void binaryEncodeText(String value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, value);
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

  private static void binaryEncodeJson(Object value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, Json.encode(value));
  }

  private static Byte binaryDecodeInt1(ByteBuf buffer) {
    return buffer.readByte();
  }

  private static Short binaryDecodeInt2(ByteBuf buffer) {
    return buffer.readShortLE();
  }

  private static Integer binaryDecodeInt3(ByteBuf buffer) {
    return buffer.readIntLE();
  }

  private static Integer binaryDecodeInt4(ByteBuf buffer) {
    return buffer.readIntLE();
  }

  private static Long binaryDecodeInt8(ByteBuf buffer) {
    return buffer.readLongLE();
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

  private static Numeric binaryDecodeNumeric(Charset charset, ByteBuf buffer) {
    return Numeric.parse(BufferUtils.readLengthEncodedString(buffer, charset));
  }

  private static Object binaryDecodeBlobOrText(Charset charset, int columnDefinitionFlags, ByteBuf buffer) {
    if (isBinaryField(columnDefinitionFlags)) {
      return binaryDecodeBlob(buffer);
    } else {
      return binaryDecodeText(charset, buffer);
    }
  }

  private static Buffer binaryDecodeBlob(ByteBuf buffer) {
    int len = (int) BufferUtils.readLengthEncodedInteger(buffer);

    Buffer target = Buffer.buffer(len);
    target.appendBuffer(Buffer.buffer(buffer.slice(buffer.readerIndex(), len)));
    buffer.skipBytes(len);

    return target;
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

  private static Object binaryDecodeJson(Charset charset, ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    Object result = textDecodeJson(charset, buffer, buffer.readerIndex(), length);
    buffer.skipBytes(length);
    return result;
  }

  private static Byte textDecodeInt1(Charset charset, ByteBuf buffer, int index, int length) {
    return Byte.parseByte(buffer.toString(index, length, charset));
  }

  private static Short textDecodeInt2(Charset charset, ByteBuf buffer, int index, int length) {
    return Short.parseShort(buffer.toString(index, length, charset));
  }

  private static Integer textDecodeInt3(Charset charset, ByteBuf buffer, int index, int length) {
    return Integer.parseInt(buffer.toString(index, length, charset));
  }

  private static Integer textDecodeInt4(Charset charset, ByteBuf buffer, int index, int length) {
    return Integer.parseInt(buffer.toString(index, length, charset));
  }

  private static Long textDecodeInt8(Charset charset, ByteBuf buffer, int index, int length) {
    return Long.parseLong(buffer.toString(index, length, charset));
  }

  private static Float textDecodeFloat(Charset charset, ByteBuf buffer, int index, int length) {
    return Float.parseFloat(buffer.toString(index, length, charset));
  }

  private static Double textDecodeDouble(Charset charset, ByteBuf buffer, int index, int length) {
    return Double.parseDouble(buffer.toString(index, length, charset));
  }

  private static Long textDecodeBit(Charset charset, ByteBuf buffer, int index, int length) {
    return decodeBit(buffer, index, length);
  }

  private static Number textDecodeNUMERIC(Charset charset, ByteBuf buff, int index, int length) {
    return Numeric.parse(buff.toString(index, length, charset));
  }

  private static Object textDecodeBlobOrText(Charset charset, int columnDefinitionFlags,
    ByteBuf buffer, int index, int length) {
    if (isBinaryField(columnDefinitionFlags)) {
      return textDecodeBlob(buffer, index, length);
    } else {
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

  private static LocalDate textDecodeDate(Charset charset, ByteBuf buffer, int index, int length) {
    CharSequence cs = buffer.toString(index, length, charset);
    return LocalDate.parse(cs);
  }

  private static Duration textDecodeTime(Charset charset, ByteBuf buffer, int index, int length) {
    // HH:mm:ss or HHH:mm:ss
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

  private static LocalDateTime textDecodeDateTime(Charset charset, ByteBuf buffer, int index, int length) {
    CharSequence cs = buffer.toString(index, length, charset);
    if (cs.equals("0000-00-00 00:00:00")) {
      // Invalid datetime will be converted to zero
      return null;
    }
    return LocalDateTime.parse(cs, DATETIME_FORMAT);
  }

  private static Object textDecodeJson(Charset charset, ByteBuf buffer, int index, int length) {
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

  private static boolean isBinaryField(int columnDefinitionFlags) {
    return (columnDefinitionFlags & ColumnDefinition.ColumnDefinitionFlags.BINARY_FLAG) != 0;
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
