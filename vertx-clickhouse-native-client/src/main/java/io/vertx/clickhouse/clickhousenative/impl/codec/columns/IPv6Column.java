package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class IPv6Column extends FixedStringColumn {
  public static final int ELEMENT_SIZE = 16;

  public IPv6Column(ClickhouseNativeColumnDescriptor descr, ClickhouseNativeDatabaseMetadata md) {
    super(descr, md);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new IPv6ColumnReader(nRows, descriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new IPv6ColumnWriter(data, descriptor, md.getStringCharset(), columnIndex);
  }
}
