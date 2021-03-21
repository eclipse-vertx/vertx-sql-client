package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.clickhouse.clickhousenative.impl.codec.Utils;

import java.math.BigInteger;

//experimental support at the moment
public class UInt128ColumnReader extends ClickhouseColumnReader {
  public static final int ELEMENT_SIZE = 16;

  protected UInt128ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      BigInteger[] data = new BigInteger[nRows];
      byte[] readBuffer = new byte[ELEMENT_SIZE];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          in.readBytes(readBuffer);
          data[i] = new BigInteger(Utils.reverse(readBuffer));
        } else {
          in.skipBytes(ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return ((BigInteger[]) this.itemsArray)[rowIdx];
  }
}
