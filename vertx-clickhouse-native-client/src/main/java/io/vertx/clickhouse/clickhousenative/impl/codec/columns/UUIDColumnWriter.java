package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.UUID;

public class UUIDColumnWriter extends ClickhouseColumnWriter {
  public UUIDColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    UUID uuid = (UUID) val;
    sink.writeLongLE(uuid.getMostSignificantBits());
    sink.writeLongLE(uuid.getLeastSignificantBits());
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(UUIDColumn.ELEMENT_SIZE);
  }
}
