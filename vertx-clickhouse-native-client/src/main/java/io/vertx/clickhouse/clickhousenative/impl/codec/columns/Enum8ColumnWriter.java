package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.Map;

public class Enum8ColumnWriter extends UInt8ColumnWriter {
  private final EnumColumnEncoder columnEncoder;

  public Enum8ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex,
                           Map<? extends Number, String> enumVals, EnumResolutionMethod resolutionMethod) {
    super(data, columnDescriptor, columnIndex);
    this.columnEncoder = new EnumColumnEncoder(enumVals, resolutionMethod);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Number idx = columnEncoder.encode(val);
    super.serializeDataElement(sink, idx);
  }
}
