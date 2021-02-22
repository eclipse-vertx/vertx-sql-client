package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

public class UInt32Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 4;

  public UInt32Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
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
  protected Object getElementInternal(int rowNo) {
    int element = ((int[])this.itemsArray)[rowNo];
    if (columnDescriptor.isUnsigned()) {
      return Integer.toUnsignedLong(element);
    }
    return element;
  }
}
