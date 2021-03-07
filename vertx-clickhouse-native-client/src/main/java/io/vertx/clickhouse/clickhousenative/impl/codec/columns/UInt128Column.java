package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.math.BigInteger;

public class UInt128Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 16;

  protected UInt128Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      BigInteger[] data = new BigInteger[nRows];
      for (int i = 0; i < nRows; ++i) {
        byte[] tmp = new byte[ELEMENT_SIZE];
        in.readBytes(tmp);
        data[i] = new BigInteger(reverse(tmp));
      }
      return data;
    }
    return null;
  }

  private byte[] reverse(byte[] src) {
    for (int i = 0, j = src.length - 1; i < j; ++i, --j) {
      byte tmp = src[i];
      src[i] = src[j];
      src[j] = tmp;
    }
    return src;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    return ((BigInteger[]) this.itemsArray)[rowIdx];
  }
}
