package io.reactiverse.myclient.impl.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

public class BufferUtils {
  private static final byte TERMINAL = 0x00;

  public static String readNullTerminatedString(ByteBuf buffer, Charset charset) {
    int len = buffer.bytesBefore(TERMINAL);
    String s = buffer.readCharSequence(len, charset).toString();
    buffer.readByte();
    return s;
  }

  public static String readFixedLengthString(ByteBuf buffer, int length, Charset charset) {
    return buffer.readCharSequence(length, charset).toString();
  }

  public static void writeNullTerminatedString(ByteBuf buffer, CharSequence charSequence, Charset charset) {
    buffer.writeCharSequence(charSequence, charset);
    buffer.writeByte(0);
  }

  public static void writeLengthEncodedInteger(ByteBuf buffer, long value) {
    if (value < 251) {
      // 1-byte integer
      buffer.writeByte((byte) value);
    } else if (value <= 0xFFFF) {
      // 0xFC + 2-byte integer
      buffer.writeByte(0xFC);
      buffer.writeShortLE((int) value);
    } else if (value < 0xFFFFFF) {
      // 0xFD + 3-byte integer
      buffer.writeByte(0xFD);
      buffer.writeMediumLE((int) value);
    } else {
      // 0xFE + 8-byte integer
      buffer.writeByte(0xFE);
      buffer.writeLongLE(value);
    }
  }

  public static long readLengthEncodedInteger(ByteBuf buffer) {
    short firstByte = buffer.readUnsignedByte();
    if (firstByte < 0xFB) {
      return firstByte;
    } else if (firstByte == 0xFB) {
      // how we handle null here?
      return -1;
    } else if (firstByte == 0xFC) {
      return buffer.readUnsignedShortLE();
    } else if (firstByte == 0xFD) {
      return buffer.readUnsignedMediumLE();
    } else if (firstByte == 0xFE) {
      return buffer.readLongLE();
    } else {
      throw new UnsupportedOperationException("Invalid LengthEncodedInteger format");
    }
  }

  public static void writeLengthEncodedString(ByteBuf buffer, String value, Charset charset) {
    byte[] bytes = value.getBytes(charset);
    writeLengthEncodedInteger(buffer, bytes.length);
    buffer.writeBytes(bytes);
  }

  public static String readLengthEncodedString(ByteBuf buffer, Charset charset) {
    long length = readLengthEncodedInteger(buffer);
    return readFixedLengthString(buffer, (int) length, charset);
  }
}
