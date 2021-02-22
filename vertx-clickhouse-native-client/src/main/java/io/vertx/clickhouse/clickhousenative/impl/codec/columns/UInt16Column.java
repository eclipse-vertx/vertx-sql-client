package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

public class UInt16Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 2;

  public UInt16Column(int nItems, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nItems, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nItems) {
      short[] data = new short[nItems];
      for (int i = 0; i < nItems; ++i) {
        data[i] = in.readShortLE();
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowNo) {
    short element = ((short[])this.items)[rowNo];
    if (columnDescriptor.isUnsigned()) {
      return Short.toUnsignedInt(element);
    }
    return element;
  }
}
