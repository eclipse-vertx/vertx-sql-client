package io.vertx.clickhouse.clickhousenative.impl.codec;

public class Utils {
  public static String[] hex(long[] src) {
    String[] result = new String[src.length];
    for (int i = 0; i < src.length; ++i) {
      result[i] = "0x" + Long.toHexString(src[i]);
    }
    return result;
  }

  public static byte[] reverse(byte[] src) {
    for (int i = 0, j = src.length - 1; i < j; ++i, --j) {
      byte tmp = src[i];
      src[i] = src[j];
      src[j] = tmp;
    }
    return src;
  }
}
