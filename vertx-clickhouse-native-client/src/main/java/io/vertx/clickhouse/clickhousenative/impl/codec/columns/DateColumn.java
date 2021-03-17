package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.time.LocalDate;

public class DateColumn extends UInt16Column {
  public static final LocalDate MIN_DATE = LocalDate.of(1970, 1, 1);

  public DateColumn(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Integer offset = (Integer) super.getElementInternal(rowIdx, desired);
    return MIN_DATE.plusDays(offset);
  }
}
