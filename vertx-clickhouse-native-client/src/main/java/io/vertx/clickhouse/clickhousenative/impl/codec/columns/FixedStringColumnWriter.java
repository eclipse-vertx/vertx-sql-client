package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.Charset;
import java.util.List;

public class FixedStringColumnWriter extends ClickhouseColumnWriter {
  private final Charset charset;

  public FixedStringColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, Charset charset, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
    this.charset = charset;
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    byte[] bytes = val.getClass() == byte[].class ? (byte[])val : ((String)val).getBytes(charset);
    int elSize = columnDescriptor.getElementSize();
    if (bytes.length > elSize) {
      throw new IllegalArgumentException("fixed string bytes are too long: got " + bytes.length + ", max " + elSize);
    }
    sink.writeBytes(bytes);
    sink.writeZero(elSize - bytes.length);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(columnDescriptor.getElementSize());
  }
}
