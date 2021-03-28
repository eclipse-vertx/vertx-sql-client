package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class ArrayColumnWriter extends ClickhouseColumnWriter {
  private final ClickhouseNativeDatabaseMetadata md;

  public ArrayColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md, int columnIndex) {
    super(data, descriptor, columnIndex);
    this.md = md;
  }

  @Override
  protected void serializeDataInternal(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
  }

  @Override
  protected void serializeNullsMap(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
  }
}
