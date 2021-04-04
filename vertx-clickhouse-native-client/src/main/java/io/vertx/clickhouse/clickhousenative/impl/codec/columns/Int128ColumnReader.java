package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.math.BigInteger;

//experimental support at the moment
public class Int128ColumnReader extends ClickhouseColumnReader {
  protected Int128ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= Int128Column.ELEMENT_SIZE * nRows) {
      BigInteger[] data = new BigInteger[nRows];
      byte[] readBuffer = new byte[Int128Column.ELEMENT_SIZE];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          in.readBytes(readBuffer);
          data[i] = new BigInteger(ColumnUtils.reverse(readBuffer));
        } else {
          in.skipBytes(Int128Column.ELEMENT_SIZE);
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
