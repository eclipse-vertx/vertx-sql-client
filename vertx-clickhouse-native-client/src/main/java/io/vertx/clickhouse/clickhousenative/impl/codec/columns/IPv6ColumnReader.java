package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv6ColumnReader extends FixedStringColumnReader {
  public static final int ELEMENT_SIZE = 16;

  protected IPv6ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, columnDescriptor, md);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    if (desired == InetAddress.class || desired == Inet6Address.class || desired == null) {
      byte[] addr = (byte[]) super.getElementInternal(rowIdx, byte[].class);
      try {
        return Inet6Address.getByAddress(addr);
      } catch (UnknownHostException ex) {
        throw new RuntimeException(ex);
      }
    }
    return super.getElementInternal(rowIdx, desired);
  }
}
