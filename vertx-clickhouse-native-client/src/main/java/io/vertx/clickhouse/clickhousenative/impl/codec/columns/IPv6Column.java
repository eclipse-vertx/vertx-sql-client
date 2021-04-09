package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.net.Inet6Address;
import java.net.UnknownHostException;
import java.util.List;

public class IPv6Column extends FixedStringColumn {
  public static final int ELEMENT_SIZE = 16;
  public static final Inet6Address EMPTY_ARRAY[] = new Inet6Address[0];

  public static final Inet6Address ZERO_VALUE = ipv6(new byte[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0});
  public static final Inet6Address MIN_VALUE = ipv6(new byte[]{
    Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE,
    Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE,
    Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE,
    Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE
  });
  public static final Inet6Address MAX_VALUE = ipv6(new byte[]{
    Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE,
    Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE,
    Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE,
    Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MAX_VALUE
  });

  private static Inet6Address ipv6(byte[] src) {
    try {
      return (Inet6Address) Inet6Address.getByAddress(src);
    } catch (UnknownHostException ex) {
      throw new RuntimeException(ex);
    }
  }

  public IPv6Column(ClickhouseNativeColumnDescriptor descr, ClickhouseNativeDatabaseMetadata md) {
    super(descr, md, false);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new IPv6ColumnReader(nRows, descriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new IPv6ColumnWriter(data, descriptor, md.getStringCharset(), columnIndex);
  }

  @Override
  public Object nullValue() {
    return ZERO_VALUE;
  }

  @Override
  public Object[] emptyArray() {
    return EMPTY_ARRAY;
  }
}
