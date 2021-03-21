package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.ZoneId;
import java.util.List;

public class DateTimeColumn extends ClickhouseColumn {
  private final ZoneId zoneId;

  public DateTimeColumn(ClickhouseNativeColumnDescriptor descriptor, ZoneId zoneId) {
    super(descriptor);
    this.zoneId = zoneId;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new DateTimeColumnReader(nRows, descriptor, zoneId);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalStateException("not implemented");
  }
}
