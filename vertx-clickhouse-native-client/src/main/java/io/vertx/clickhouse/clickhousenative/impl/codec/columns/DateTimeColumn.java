package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

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
      ZonedDateTime[] data = new ZonedDateTime[nRows];
      for (int i = 0; i < nRows; ++i) {
        long unixSeconds = Integer.toUnsignedLong(in.readIntLE());
        if (nullsMap == null || !nullsMap.get(i)) {
          ZonedDateTime dt = Instant.ofEpochSecond(unixSeconds).atZone(zoneId);
          data[i] = dt;
        }
      }
      return data;
    }
    return null;
  }
}
