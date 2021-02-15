package io.vertx.clickhouse.clikhousenative.impl.codec;

import io.netty.buffer.ByteBuf;

import java.nio.charset.StandardCharsets;

public class ByteBufUtils {
  public static void writeULeb128(int value, ByteBuf buf) {
    assert (value >= 0);
    int remaining = value >>> 7;
    while (remaining != 0) {
      buf.writeByte((byte) ((value & 0x7f) | 0x80));
      value = remaining;
      remaining >>>= 7;
    }
    buf.writeByte((byte) (value & 0x7f));
  }

  public static void writeCString(String str, ByteBuf buf) {
    byte[] b = str.getBytes(StandardCharsets.UTF_8);
    writeULeb128(b.length, buf);
    buf.writeBytes(b);
  }
}
