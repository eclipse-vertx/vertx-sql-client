package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

public class IPv4Column extends UInt32Column {
  public static final int ELEMENT_SIZE = 4;
  public static final Inet4Address ZERO_VALUE = ipv4(new byte[]{0, 0, 0, 0});
  public static final Inet4Address MAX_VALUE = ipv4(new byte[]{Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE});
  public static final Inet4Address MIN_VALUE = ipv4(new byte[]{Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE});

  private static Inet4Address ipv4(byte[] src) {
    try {
      return (Inet4Address) Inet4Address.getByAddress(src);
    } catch (UnknownHostException ex) {
      throw new RuntimeException(ex);
    }
  }

  public IPv4Column(ClickhouseNativeColumnDescriptor descr) {
    super(descr);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new IPv4ColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new IPv4ColumnWriter(data, descriptor, columnIndex);
  }

  @Override
  public Object nullValue() {
    return ZERO_VALUE;
  }
}
