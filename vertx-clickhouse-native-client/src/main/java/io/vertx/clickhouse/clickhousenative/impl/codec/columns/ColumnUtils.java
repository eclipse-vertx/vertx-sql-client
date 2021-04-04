package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

public class ColumnUtils {
  public static byte[] reverse(byte[] src) {
    for (int i = 0, j = src.length - 1; i < j; ++i, --j) {
      byte tmp = src[i];
      src[i] = src[j];
      src[j] = tmp;
    }
    return src;
  }
}
