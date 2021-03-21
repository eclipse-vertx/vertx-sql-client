package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.ZoneId;
import java.util.List;

public class DateTime64Column extends ClickhouseColumn {
  private final Integer precision;
  private final ZoneId zoneId;

  public DateTime64Column(ClickhouseNativeColumnDescriptor descriptor, Integer precision, ZoneId zoneId) {
    super(descriptor);
    this.precision = precision;
    this.zoneId = zoneId;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new DateTime64ColumnReader(nRows, descriptor, precision, zoneId);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalStateException("not implemented");
  }
}
