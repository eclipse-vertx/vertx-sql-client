package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class BooleanColumnWriter extends ClickhouseColumnWriter {
  public BooleanColumnWriter(List<Tuple> data, ClickhouseBinaryColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    sink.writeBoolean((Boolean) val);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(BooleanColumnReaderAsArray.ELEMENT_SIZE);
  }
}
