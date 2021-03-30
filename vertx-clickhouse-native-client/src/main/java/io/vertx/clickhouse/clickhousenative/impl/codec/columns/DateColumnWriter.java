package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public class DateColumnWriter extends UInt16ColumnWriter {
  public static final long MAX_VALUE = 65535;

  public DateColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    LocalDate dateVal = (LocalDate) val;
    long daysDelta = ChronoUnit.DAYS.between(DateColumnReader.MIN_VALUE, dateVal);
    if (daysDelta < 0) {
      throw new IllegalArgumentException("date " + dateVal + " is too small; smallest possible date: " + DateColumnReader.MIN_VALUE);
    }
    if (daysDelta > MAX_VALUE) {
      throw new IllegalArgumentException("date " + dateVal + " is too big; largest possible date: " + DateColumnReader.MAX_VALUE);
    }
    super.serializeDataElement(sink, daysDelta);
  }
}
