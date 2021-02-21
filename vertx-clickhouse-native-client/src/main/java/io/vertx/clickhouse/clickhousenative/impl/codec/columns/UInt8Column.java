package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

public class UInt8Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 1;

  public UInt8Column(int nItems, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nItems, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nItems) {
      byte[] data = new byte[nItems];
      for (int i = 0; i < nItems; ++i) {
        data[i] = in.readByte();
      }
      return data;
    }
    return null;
  }
}
