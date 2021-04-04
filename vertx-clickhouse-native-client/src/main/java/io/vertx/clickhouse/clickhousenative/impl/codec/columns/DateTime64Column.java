package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.Instant;
import java.time.ZoneId;
import java.util.List;

public class DateTime64Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 8;

  private final Integer precision;
  private final ZoneId zoneId;
  private boolean saturateExtraNanos;

  public DateTime64Column(ClickhouseNativeColumnDescriptor descriptor, Integer precision, boolean saturateExtraNanos, ZoneId zoneId) {
    super(descriptor);
    this.precision = precision;
    this.zoneId = zoneId;
    this.saturateExtraNanos = saturateExtraNanos;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new DateTime64ColumnReader(nRows, descriptor, precision, zoneId);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new DateTime64ColumnWriter(data, descriptor, precision, zoneId, saturateExtraNanos, columnIndex);
  }

  @Override
  public Object nullValue() {
    return Instant.ofEpochSecond(0, 0).atZone(zoneId).toOffsetDateTime();
  }
}
