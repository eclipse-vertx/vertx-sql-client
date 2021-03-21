package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class Float32ColumnWriter extends ClickhouseColumnWriter {
  public Float32ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    float b = ((Number)val).floatValue();
    sink.writeFloatLE(b);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeFloatLE(0);
  }
}
