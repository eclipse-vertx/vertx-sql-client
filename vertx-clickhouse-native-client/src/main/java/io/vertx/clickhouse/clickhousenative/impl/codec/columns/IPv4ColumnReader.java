package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPv4ColumnReader extends UInt32ColumnReader {

  public IPv4ColumnReader(int nRows, ClickhouseNativeColumnDescriptor descriptor) {
    super(nRows, descriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    if (desired == InetAddress.class || desired == Inet4Address.class || desired == String.class || desired == null) {
      Long addr = (Long) super.getElementInternal(rowIdx, byte[].class);
      try {
        return Inet4Address.getByAddress(intBytes(addr));
      } catch (UnknownHostException ex) {
        throw new RuntimeException(ex);
      }
    }
    return super.getElementInternal(rowIdx, desired);
  }

  private static byte[] intBytes(Long l) {
    return new byte[] {
      (byte) (l >>> 24 & 0xFF),
      (byte) (l >>> 16 & 0xFF),
      (byte) (l >>> 8 & 0xFF),
      (byte) (l & 0xFF)
    };
  }
}
