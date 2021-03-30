package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeColumnReader extends ClickhouseColumnReader {
  public static final long MAX_EPOCH_SECOND = 4294967295L;

  public static final int ELEMENT_SIZE = 4;

  private final ZoneId zoneId;

  public DateTimeColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, ZoneId zoneId) {
    super(nRows, descr);
    this.zoneId = zoneId;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      OffsetDateTime[] data = new OffsetDateTime[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          long unixSeconds = Integer.toUnsignedLong(in.readIntLE());
          OffsetDateTime dt = Instant.ofEpochSecond(unixSeconds).atZone(zoneId).toOffsetDateTime();
          data[i] = dt;
        } else {
          in.skipBytes(ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }
}
