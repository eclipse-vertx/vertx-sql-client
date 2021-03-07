package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTimeColumn extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 4;

  private final ZoneId zoneId;

  public DateTimeColumn(int nRows, ClickhouseNativeColumnDescriptor descr, ZoneId zoneId) {
    super(nRows, descr);
    this.zoneId = zoneId;
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      OffsetDateTime[] data = new OffsetDateTime[nRows];
      for (int i = 0; i < nRows; ++i) {
        long unixSeconds = Integer.toUnsignedLong(in.readIntLE());
        if (nullsMap == null || !nullsMap.get(i)) {
          OffsetDateTime dt = Instant.ofEpochSecond(unixSeconds).atZone(zoneId).toOffsetDateTime();
          data[i] = dt;
        }
      }
      return data;
    }
    return null;
  }
}
