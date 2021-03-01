package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.math.BigInteger;

public class UInt64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;

  public UInt64Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ByteBuf in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      long[] data = new long[nRows];
      for (int i = 0; i < nRows; ++i) {
        data[i] = in.readLongLE();
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    long element = ((long[])this.itemsArray)[rowIdx];
    if (columnDescriptor.isUnsigned()) {
      return unsignedBi(element);
    }
    return element;
  }

  private static BigInteger unsignedBi(long l) {
    return new BigInteger(1, new byte[] {
      (byte) (l >>> 56 & 0xFF),
      (byte) (l >>> 48 & 0xFF),
      (byte) (l >>> 40 & 0xFF),
      (byte) (l >>> 32 & 0xFF),
      (byte) (l >>> 24 & 0xFF),
      (byte) (l >>> 16 & 0xFF),
      (byte) (l >>> 8 & 0xFF),
      (byte) (l & 0xFF)
    });
  }
}
