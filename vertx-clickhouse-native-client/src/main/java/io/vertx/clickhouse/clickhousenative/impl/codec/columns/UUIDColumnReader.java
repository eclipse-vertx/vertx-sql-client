package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.UUID;

public class UUIDColumnReader extends ClickhouseColumnReader {

  protected UUIDColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= UUIDColumn.ELEMENT_SIZE * nRows) {
      UUID[] data = new UUID[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          long mostSigBits = in.readLongLE();
          long leastSigBits = in.readLongLE();
          data[i] = new UUID(mostSigBits, leastSigBits);
        } else {
          in.skipBytes(UUIDColumn.ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }
}
