package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;

public class DateTimeColumn extends ClickhouseColumn {
  public static final OffsetDateTime[] EMPTY_ARRAY = new OffsetDateTime[0];

  private final ZoneId zoneId;
  private final OffsetDateTime nullValue;

  public DateTimeColumn(ClickhouseNativeColumnDescriptor descriptor, ZoneId zoneId) {
    super(descriptor);
    this.zoneId = zoneId;
    this.nullValue = Instant.EPOCH.atZone(zoneId).toOffsetDateTime();
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new DateTimeColumnReader(nRows, descriptor, zoneId);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new DateTimeColumnWriter(data, descriptor, zoneId, columnIndex);
  }

  @Override
  public Object nullValue() {
    return nullValue;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_ARRAY;
  }
}
