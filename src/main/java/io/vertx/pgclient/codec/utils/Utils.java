package io.vertx.pgclient.codec.utils;

import io.netty.buffer.ByteBuf;

import java.nio.charset.Charset;


public class Utils {

  public static String readCString(ByteBuf buffer, Charset charset) {
    byte[] bytes = new byte[buffer.bytesBefore((byte) 0) + 1];
    buffer.readBytes(bytes);
    return new String(bytes, 0, bytes.length - 1, charset);
  }
}
