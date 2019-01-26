package io.reactiverse.mysqlclient.impl.codec.datatype;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class DataTypeCodec {
  public static Object decodeText(DataType dataType, ByteBuf buffer) {
    switch (dataType) {
      //TODO just a basic implementation, can be optimised here
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
      case VARCHAR:
        return textDecodeVarChar(buffer);
      default:
        return textDecodeVarChar(buffer);
    }
  }

  public static void encodeBinary(DataType dataType, Object value, ByteBuf buffer) {
    throw new UnsupportedOperationException();
  }

  public static Object decodeBinary(DataType dataType, ByteBuf buffer) {
    throw new UnsupportedOperationException();
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

  private static String textDecodeVarChar(ByteBuf buffer) {
    return buffer.toString(StandardCharsets.UTF_8);
  }
}
