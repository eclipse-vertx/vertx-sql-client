package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

public class UInt32Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 4;

  public UInt32Column(int nItems, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nItems, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nItems) {
      int[] data = new int[nItems];
      for (int i = 0; i < nItems; ++i) {
        data[i] = in.readIntLE();
      }
      return data;
    }
    return null;
  }
}
