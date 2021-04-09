package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.time.LocalDate;

public class DateColumnReader extends UInt16ColumnReader {
  public static final LocalDate[] EMPTY_ARRAY = new LocalDate[0];

  public static final LocalDate MIN_VALUE = LocalDate.of(1970, 1, 1);
  public static final LocalDate MAX_VALUE = MIN_VALUE.plusDays(65535);

  public DateColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Integer offset = (Integer) super.getElementInternal(rowIdx, desired);
    return MIN_VALUE.plusDays(offset);
  }
}
