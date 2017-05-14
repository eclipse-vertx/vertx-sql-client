package io.vertx.pgclient.codec.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;

import static io.netty.util.CharsetUtil.UTF_8;


public class Util {

  private static final byte ZERO = 0;

  public static String readCString(ByteBuf src, Charset charset) {
    int len = src.bytesBefore(ZERO);
    String s = src.readCharSequence(len, charset).toString();
    src.readByte();
    return s;
  }

  public static String readCStringUTF8(ByteBuf src) {
    int len = src.bytesBefore(ZERO);
    String s = src.readCharSequence(len, UTF_8).toString();
    src.readByte();
    return s;
  }

  public static void writeCString(ByteBuf dst, String s, Charset charset) {
    dst.writeCharSequence(s, charset);
    dst.writeByte(0);
  }

  public static void writeCString(ByteBuf dst, ByteBuf buf) {
    // Important : won't not change data index
    dst.writeBytes(buf, buf.readerIndex(), buf.readableBytes());
    dst.writeByte(0);
  }

  public static void writeCStringUTF8(ByteBuf dst, String s) {
    dst.writeCharSequence(s, UTF_8);
    dst.writeByte(0);
  }

  public static void writeCString(ByteBuf dst, byte[] bytes) {
    dst.writeBytes(bytes, 0, bytes.length);
    dst.writeByte(0);
  }
}
