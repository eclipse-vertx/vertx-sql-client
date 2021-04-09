package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigInteger;

public class UInt64ColumnReader extends ClickhouseColumnReader {
  public static final int ELEMENT_SIZE = 8;

  public UInt64ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    //TODO: maybe read elements as soon as they arrive if we have enough data (> ELEMENT_SIZE)
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      long[] data = new long[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data[i] = in.readLongLE();
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
    long element = ((long[])this.itemsArray)[rowIdx];
    if (columnDescriptor.isUnsigned()) {
      return Numeric.create(unsignedBi(element));
    }
    return element;
  }

  @Override
  protected Object[] asObjectsArray(Class<?> desired) {
    return asObjectsArrayWithGetElement(desired);
  }

  static BigInteger unsignedBi(long l) {
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
