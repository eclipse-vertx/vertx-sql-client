package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.time.Duration;
import java.util.List;

public class IntervalColumn extends ClickhouseColumn {
  public static final Duration[] EMPTY_ARRAY = new Duration[0];

  public static final Duration ZERO_VALUE = Duration.ZERO;
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
    return ZERO_VALUE;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_ARRAY;
  }
}
