package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.time.Duration;

public class IntervalColumnReader extends UInt64ColumnReader {
  private final Duration multiplier;

  public IntervalColumnReader(int nRows, ClickhouseNativeColumnDescriptor descriptor, Duration multiplier) {
    super(nRows, descriptor);
    this.multiplier = multiplier;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Long obj = (Long)super.getElementInternal(rowIdx, desired);
    if (desired != Duration.class) {
      return obj;
    }
    return multiplier.multipliedBy(obj);
  }
}
