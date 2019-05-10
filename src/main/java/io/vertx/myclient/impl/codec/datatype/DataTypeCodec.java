package io.vertx.myclient.impl.codec.datatype;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.DecoderException;
import io.vertx.myclient.impl.util.BufferUtils;
import io.vertx.pgclient.data.Numeric;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatterBuilder;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_TIME;

//TODO charset injection
public class DataTypeCodec {
  // binary codec protocol: https://dev.mysql.com/doc/dev/mysql-server/latest/page_protocol_binary_resultset.html#sect_protocol_binary_resultset_row_value

  // Sentinel used when an object is refused by the data type
  public static final Object REFUSED_SENTINEL = new Object();

  private static final java.time.format.DateTimeFormatter TIMESTAMP_FORMAT = new DateTimeFormatterBuilder()
    .parseCaseInsensitive()
    .append(ISO_LOCAL_DATE)
    .appendLiteral(' ')
    .append(ISO_LOCAL_TIME)
    .toFormatter();

  public static Object decodeText(DataType dataType, ByteBuf buffer) {
    switch (dataType) {
      //TODO just a basic implementation, can be optimised here
      case INT1:
        return textDecodeInt1(buffer);
      case INT2:
        return textDecodeInt2(buffer);
      case INT3:
        return textDecodeInt3(buffer);
      case INT4:
        return textDecodeInt4(buffer);
      case INT8:
        return textDecodeInt8(buffer);
      case FLOAT:
        return textDecodeFloat(buffer);
      case DOUBLE:
        return textDecodeDouble(buffer);
      case NUMERIC:
        return textDecodeNUMERIC(buffer);
      case CHAR:
        return textDecodeChar(buffer);
      case VARCHAR:
        return textDecodeVarChar(buffer);
      case DATE:
        return textDecodeDate(buffer);
      case TIME:
        return textDecodeTime(buffer);
      case DATETIME:
        return textDecodeDateTime(buffer);
      default:
        return textDecodeVarChar(buffer);
    }
  }

  //TODO take care of unsigned numeric values here?
  public static void encodeBinary(DataType dataType, Object value, ByteBuf buffer) {
    switch (dataType) {
      case INT1:
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
      case CHAR:
        binaryEncodeChar(String.valueOf(value), buffer);
      case VARCHAR:
        binaryEncodeVarChar(String.valueOf(value), buffer);
        break;
      case DATE:
        // TODO confirm DATE,TIM encoded into VAR_STRING form?
        binaryEncodeDate((LocalDate) value, buffer);
        break;
      case TIME:
        binaryEncodeTime((LocalTime) value, buffer);
        break;
      case DATETIME:
        binaryEncodeDatetime((LocalDateTime) value, buffer);
        break;
      default:
        binaryEncodeVarChar(String.valueOf(value), buffer);
        break;
    }
  }

  public static Object decodeBinary(DataType dataType, ByteBuf buffer) {
    switch (dataType) {
      case INT1:
        return binaryDecodeInt1(buffer);
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
      case CHAR:
        return binaryDecodeChar(buffer);
      case VARCHAR:
        return binaryDecodeVarChar(buffer);
      case DATE:
        return binaryDecodeDate(buffer);
      case TIME:
        return binaryDecodeTime(buffer);
      case DATETIME:
        return binaryDecodeDatetime(buffer);
      default:
        return binaryDecodeVarChar(buffer);
    }
  }

  public static Object prepare(DataType type, Object value) {
    switch (type) {
      //TODO handle json + unknown?
      default:
        Class<?> javaType = type.decodingType;
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

  private static void binaryEncodeChar(String value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, value, StandardCharsets.UTF_8);
  }

  private static void binaryEncodeVarChar(String value, ByteBuf buffer) {
    BufferUtils.writeLengthEncodedString(buffer, value, StandardCharsets.UTF_8);
  }

  private static void binaryEncodeDate(LocalDate value, ByteBuf buffer) {
    buffer.writeByte(4);
    buffer.writeShortLE(value.getYear());
    buffer.writeByte(value.getMonthValue());
    buffer.writeByte(value.getDayOfMonth());
  }

  private static void binaryEncodeTime(LocalTime value, ByteBuf buffer) {
    // FIXME time?
    throw new UnsupportedOperationException();
  }

  private static void binaryEncodeDatetime(LocalDateTime value, ByteBuf buffer) {
    int year = value.getYear();
    int month = value.getMonthValue();
    int day = value.getDayOfMonth();
    int hour = value.getHour();
    int minute = value.getMinute();
    int second = value.getSecond();
    int microsecond = value.getNano() / 1000;

    if (microsecond == 0) {
      if (hour == 0 && minute == 0) {
        buffer.writeByte(4);
        buffer.writeShortLE(year);
        buffer.writeByte(month);
        buffer.writeByte(day);
      } else {
        buffer.writeByte(7);
        buffer.writeShortLE(year);
        buffer.writeByte(month);
        buffer.writeByte(day);
        buffer.writeByte(hour);
        buffer.writeByte(minute);
        buffer.writeByte(second);
      }
    } else {
      buffer.writeByte(11);
      buffer.writeShortLE(year);
      buffer.writeByte(month);
      buffer.writeByte(day);
      buffer.writeByte(hour);
      buffer.writeByte(minute);
      buffer.writeByte(second);
      buffer.writeByte(microsecond);
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

  private static String binaryDecodeChar(ByteBuf buffer) {
    return BufferUtils.readLengthEncodedString(buffer, StandardCharsets.UTF_8);
  }

  private static String binaryDecodeVarChar(ByteBuf buffer) {
    return BufferUtils.readLengthEncodedString(buffer, StandardCharsets.UTF_8);
  }

  private static LocalDateTime binaryDecodeDatetime(ByteBuf buffer) {
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

  private static LocalTime binaryDecodeTime(ByteBuf buffer) {
    byte length = buffer.readByte();
    if (length == 0) {
      return LocalTime.of(0, 0, 0, 0);
    } else {
      // TODO not the same as TIME data type in other databases.
      boolean isNegative = buffer.readByte() == 1;
      int days = buffer.readIntLE();
      byte hour = buffer.readByte();
      byte minute = buffer.readByte();
      byte second = buffer.readByte();
      if (length == 12) {
        int microsecond = buffer.readInt();
        return LocalTime.of(days * 24 + hour, minute, second, microsecond * 1000);
      } else if (length == 8) {
        return LocalTime.of(days * 24 + hour, minute, second);
      }
      throw new DecoderException("Invalid time");
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

  private static String textDecodeChar(ByteBuf buffer) {
    return buffer.toString(StandardCharsets.UTF_8);
  }

  private static String textDecodeVarChar(ByteBuf buffer) {
    return buffer.toString(StandardCharsets.UTF_8);
  }

  private static LocalDate textDecodeDate(ByteBuf buffer) {
    CharSequence cs = buffer.toString(StandardCharsets.UTF_8);
    return LocalDate.parse(cs);
  }

  private static LocalTime textDecodeTime(ByteBuf buffer) {
    CharSequence cs = buffer.toString(StandardCharsets.UTF_8);
    return LocalTime.parse(cs);
  }

  private static LocalDateTime textDecodeDateTime(ByteBuf buffer) {
    CharSequence cs = buffer.toString(StandardCharsets.UTF_8);
    return LocalDateTime.parse(cs, TIMESTAMP_FORMAT);
  }
}
