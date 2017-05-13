package io.vertx.pgclient.codec.util;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;


public class Util {

  public static String readCString(ByteBuf in, Charset charset) {
    byte[] bytes = new byte[in.bytesBefore((byte) 0) + 1];
    in.readBytes(bytes);
    return new String(bytes, 0, bytes.length - 1, charset);
  }

  public static void writeCString(ByteBuf in, String input, Charset charset) {
    in.writeCharSequence(input, charset);
    in.writeByte(0);
  }
}
