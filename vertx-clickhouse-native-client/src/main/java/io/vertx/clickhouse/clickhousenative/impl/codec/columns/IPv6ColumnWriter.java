package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.net.Inet6Address;
import java.nio.charset.Charset;
import java.util.List;

public class IPv6ColumnWriter extends FixedStringColumnWriter {
  public IPv6ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, Charset charset, int columnIndex) {
    super(data, columnDescriptor, charset, columnIndex);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Inet6Address address = (Inet6Address) val;
    byte[] bytes = address.getAddress();
    super.serializeDataElement(sink, bytes);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeZero(IPv6Column.ELEMENT_SIZE);
  }
}
