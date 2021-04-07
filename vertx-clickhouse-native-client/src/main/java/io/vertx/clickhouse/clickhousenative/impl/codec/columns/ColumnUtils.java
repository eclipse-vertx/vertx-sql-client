package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.math.BigDecimal;

public class ColumnUtils {
  public static byte[] reverse(byte[] src) {
    for (int i = 0, j = src.length - 1; i < j; ++i, --j) {
      byte tmp = src[i];
      src[i] = src[j];
      src[j] = tmp;
    }
    return src;
  }

  public static int getLastNonZeroPos(byte[] bytes) {
    int lastNonZeroPos = bytes.length - 1;
    for (; lastNonZeroPos >= 0 && bytes[lastNonZeroPos] == 0; --lastNonZeroPos) {
    }
    return lastNonZeroPos;
  }

  //TODO: maybe rework checks
  public static void bigDecimalFitsOrThrow(BigDecimal bd, ClickhouseNativeColumnDescriptor columnDescriptor) {
    if (columnDescriptor.getScale() < bd.scale()) {
      throw new IllegalArgumentException("possible loss of scale: max " + columnDescriptor.getScale() + ", got " + bd.scale());
    }
    if (columnDescriptor.getPrecision() < bd.precision()) {
      throw new IllegalArgumentException("possible loss of precision: max " + columnDescriptor.getPrecision() + ", got " + bd.precision());
    }
  }
}
