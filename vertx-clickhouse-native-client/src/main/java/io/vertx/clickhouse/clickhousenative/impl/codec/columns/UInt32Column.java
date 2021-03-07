package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

public class UInt32Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 4;

  public UInt32Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      int[] data = new int[nRows];
      for (int i = 0; i < nRows; ++i) {
        data[i] = in.readIntLE();
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    int element = ((int[])this.itemsArray)[rowIdx];
    if (columnDescriptor.isUnsigned()) {
      return Integer.toUnsignedLong(element);
    }
    return element;
  }
}
