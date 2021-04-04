package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.Map;

public class Enum16ColumnWriter extends UInt16ColumnWriter {
  private final EnumColumnEncoder columnEncoder;

  public Enum16ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex,
                            Map<? extends Number, String> enumVals, boolean enumsByName) {
    super(data, columnDescriptor, columnIndex);
    this.columnEncoder = new EnumColumnEncoder(enumVals, enumsByName);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Number idx = columnEncoder.encode(val);
    super.serializeDataElement(sink, idx);
  }
}
