package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.sqlclient.Tuple;

import java.net.Inet4Address;
import java.util.List;

public class IPv4ColumnWriter extends UInt32ColumnWriter {
  public IPv4ColumnWriter(List<Tuple> data, ClickhouseNativeColumnDescriptor columnDescriptor, int columnIndex) {
    super(data, columnDescriptor, columnIndex);
  }

  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Inet4Address addr = (Inet4Address) val;
    super.serializeDataElement(sink, Integer.toUnsignedLong(intFromBytes(addr.getAddress())));
  }

  private static int intFromBytes(byte[] b) {
    return (0xFF000000 & (b[0] << 24)) | (0xFF0000 & (b[1] << 16)) | (0xFF00 & (b[2] << 8)) | (0xFF & (b[3]));
  }
}
