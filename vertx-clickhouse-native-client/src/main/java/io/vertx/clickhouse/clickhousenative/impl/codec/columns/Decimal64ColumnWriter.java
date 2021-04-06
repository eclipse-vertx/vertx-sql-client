package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

public class Decimal64ColumnWriter extends ClickhouseColumnWriter {
  public Decimal64ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    BigDecimal bd = ((Numeric) val).bigDecimalValue();
    if (bd == null) {
      serializeDataNull(sink);
      return;
    }
    if (columnDescriptor.getScale() < bd.scale()) {
      throw new IllegalArgumentException("possible loss of precision: max " + columnDescriptor.getScale() + ", got " + bd.scale());
    }
    BigInteger bi = bd.unscaledValue();
    sink.writeLongLE(bi.longValueExact());
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(Decimal64Column.ELEMENT_SIZE);
  }
}
