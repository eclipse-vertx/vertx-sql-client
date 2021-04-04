package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.math.BigInteger;
import java.util.List;

public class Int128ColumnWriter extends ClickhouseColumnWriter {
  public Int128ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    BigInteger bi = (BigInteger) val;
    byte[] bytes = ColumnUtils.reverse(bi.toByteArray());
    sink.writeBytes(bytes);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(Int128Column.ELEMENT_SIZE);
  }
}
