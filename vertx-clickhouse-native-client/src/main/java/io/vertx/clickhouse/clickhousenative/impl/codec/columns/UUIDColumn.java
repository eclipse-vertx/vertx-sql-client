package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.UUID;

public class UUIDColumn extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 16;

  protected UUIDColumn(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      UUID[] data = new UUID[nRows];
      for (int i = 0; i < nRows; ++i) {
        long mostSigBits = in.readLongLE();
        long leastSigBits = in.readLongLE();
        data[i] = new UUID(mostSigBits, leastSigBits);
      }
      return data;
    }
    return null;
  }
}
