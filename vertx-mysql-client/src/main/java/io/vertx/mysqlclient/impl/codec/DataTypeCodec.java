package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.vertx.mysqlclient.impl.util.BufferUtils;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.data.Numeric;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.temporal.ChronoField.*;

//TODO charset injection
//TODO 2: In MySQL, there is no way to tell a Result is a BOOLEAN type or a INT1 type so we need to take a look at the type mapping later
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

  static Object decodeText(DataType dataType, int columnDefinitionFlags, ByteBuf buffer) {
    int length = (int) BufferUtils.readLengthEncodedInteger(buffer);
    ByteBuf data = buffer.slice(buffer.readerIndex(), length);
    buffer.skipBytes(length);
    switch (dataType) {
      case INT1:
        return textDecodeInt1(data);
      case INT2:
      case YEAR:
        return textDecodeInt2(data);
      case INT3:
        return textDecodeInt3(data);
      case INT4:
        return textDecodeInt4(data);
      case INT8:
        return textDecodeInt8(data);
      case FLOAT:
        return textDecodeFloat(data);
      case DOUBLE:
        return textDecodeDouble(data);
      case NUMERIC:
        return textDecodeNUMERIC(data);
      case DATE:
        return textDecodeDate(data);
      case TIME:
        return textDecodeTime(data);
      case DATETIME:
      case TIMESTAMP:
        return textDecodeDateTime(data);
      case STRING:
      case VARSTRING:
      case BLOB:
      default:
        return textDecodeBlobOrText(columnDefinitionFlags, data);
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
      case STRING:
      case VARSTRING:
      default:
        binaryEncodeText(String.valueOf(value), buffer);
        break;
    }
  }

  static Object decodeBinary(DataType dataType, int columnDefinitionFlags, ByteBuf buffer) {
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
      case NUMERIC:
        return binaryDecodeNumeric(buffer);
      case DATE:
        return binaryDecodeDate(buffer);
      case TIME:
        return binaryDecodeTime(buffer);
      case DATETIME:
      case TIMESTAMP:
        return binaryDecodeDatetime(buffer);
      case STRING:
      case VARSTRING:
      case BLOB:
      default:
        return binaryDecodeBlobOrText(columnDefinitionFlags, buffer);
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
    BufferUtils.writeLengthEncodedString(buffer, value.toString(), StandardCharsets.UTF_8);
  }

  private static void binaryEncodeText(String value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, value, StandardCharsets.UTF_8);
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

  private static Numeric binaryDecodeNumeric(ByteBuf buffer) {
    return Numeric.parse(BufferUtils.readLengthEncodedString(buffer, StandardCharsets.UTF_8));
  }

  private static Object binaryDecodeBlobOrText(int columnDefinitionFlags, ByteBuf buffer) {
    if (isBinaryField(columnDefinitionFlags)) {
      return binaryDecodeBlob(buffer);
    } else {
      return binaryDecodeText(buffer);
    }
  }

  private static Buffer binaryDecodeBlob(ByteBuf buffer) {
    int len = (int) BufferUtils.readLengthEncodedInteger(buffer);
    ByteBuf copy = buffer.copy(buffer.readerIndex(), len);
    buffer.skipBytes(len);
    return Buffer.buffer(copy);
  }

  private static String binaryDecodeText(ByteBuf buffer) {
    return BufferUtils.readLengthEncodedString(buffer, StandardCharsets.UTF_8);
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

  private static Byte textDecodeInt1(ByteBuf buffer) {
    return Byte.parseByte(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Short textDecodeInt2(ByteBuf buffer) {
    return Short.parseShort(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Integer textDecodeInt3(ByteBuf buffer) {
    return Integer.parseInt(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Integer textDecodeInt4(ByteBuf buffer) {
    return Integer.parseInt(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Long textDecodeInt8(ByteBuf buffer) {
    return Long.parseLong(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Float textDecodeFloat(ByteBuf buffer) {
    return Float.parseFloat(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Double textDecodeDouble(ByteBuf buffer) {
    return Double.parseDouble(buffer.toString(StandardCharsets.UTF_8));
  }

  private static Number textDecodeNUMERIC(ByteBuf buff) {
    return Numeric.parse(buff.toString(StandardCharsets.UTF_8));
  }

  private static Object textDecodeBlobOrText(int columnDefinitionFlags, ByteBuf buffer) {
    if (isBinaryField(columnDefinitionFlags)) {
      return textDecodeBlob(buffer);
    } else {
      return textDecodeText(buffer);
    }
  }

  private static Buffer textDecodeBlob(ByteBuf buffer) {
    return Buffer.buffer(buffer.copy());
  }

  private static String textDecodeText(ByteBuf buffer) {
    return buffer.toString(StandardCharsets.UTF_8);
  }

  private static LocalDate textDecodeDate(ByteBuf buffer) {
    CharSequence cs = buffer.toString(StandardCharsets.UTF_8);
    return LocalDate.parse(cs);
  }

  private static Duration textDecodeTime(ByteBuf buffer) {
    // HH:mm:ss or HHH:mm:ss
    String timeString = buffer.toString(StandardCharsets.UTF_8);
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

  private static LocalDateTime textDecodeDateTime(ByteBuf buffer) {
    CharSequence cs = buffer.toString(StandardCharsets.UTF_8);
    if (cs.equals("0000-00-00 00:00:00")) {
      // Invalid datetime will be converted to zero
      return null;
    }
    return LocalDateTime.parse(cs, DATETIME_FORMAT);
  }

  private static boolean isBinaryField(int columnDefinitionFlags) {
    return (columnDefinitionFlags & ColumnDefinition.ColumnDefinitionFlags.BINARY_FLAG) != 0;
  }
}
