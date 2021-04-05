package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.Duration;
import java.util.List;

public class IntervalColumn extends ClickhouseColumn {
  private final Duration multiplier;

  public IntervalColumn(ClickhouseNativeColumnDescriptor descriptor, Duration multiplier) {
    super(descriptor);
    this.multiplier = multiplier;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new IntervalColumnReader(nRows, descriptor, multiplier);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public Object nullValue() {
    return multiplier.multipliedBy(0);
  }
}
