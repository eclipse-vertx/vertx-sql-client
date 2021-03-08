package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.math.BigInteger;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;

public class DateTime64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;

  private final ZoneId zoneId;
  private final BigInteger invTickSize;

  public DateTime64Column(int nRows, ClickhouseNativeColumnDescriptor descr, Integer precision, ZoneId zoneId) {
    super(nRows, descr);
    this.zoneId = zoneId;
    this.invTickSize = BigInteger.TEN.pow(precision);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      OffsetDateTime[] data = new OffsetDateTime[nRows];
      for (int i = 0; i < nRows; ++i) {
        BigInteger bi = UInt64Column.unsignedBi(in.readLongLE());
        long seconds = bi.divide(invTickSize).longValueExact();
        long nanos = bi.remainder(invTickSize).longValueExact();
        if (nullsMap == null || !nullsMap.get(i)) {
          OffsetDateTime dt = Instant.ofEpochSecond(seconds, nanos).atZone(zoneId).toOffsetDateTime();
          data[i] = dt;
        }
      }
      return data;
    }
    return null;
  }
}
