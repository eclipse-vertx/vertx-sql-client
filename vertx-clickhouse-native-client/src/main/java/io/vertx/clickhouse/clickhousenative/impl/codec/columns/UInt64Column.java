package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.math.BigInteger;

public class UInt64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;

  public UInt64Column(int nItems, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nItems, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nItems) {
      long[] data = new long[nItems];
      for (int i = 0; i < nItems; ++i) {
        data[i] = in.readLongLE();
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowNo) {
    long element = ((long[])this.items)[rowNo];
    if (columnDescriptor.isUnsigned()) {
      BigInteger ret = BigInteger.valueOf(element);
      if (element < 0) {
        ret = ret.negate();
      }
      return ret;
    }
    return element;
  }
}
