package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.Charset;
import java.util.List;

public class StringColumnWriter extends ClickhouseColumnWriter {
  private final Charset charset;
  public StringColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, Charset charset, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
    this.charset = charset;
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    byte[] bytes = val.getClass() == byte[].class ? (byte[])val : ((String)val).getBytes(charset);
    sink.writeULeb128(bytes.length);
    sink.writeBytes(bytes);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeULeb128(0);
  }
}
