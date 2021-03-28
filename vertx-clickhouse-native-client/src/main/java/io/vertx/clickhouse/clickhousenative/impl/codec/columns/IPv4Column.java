package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class IPv4Column extends UInt32Column {
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
}
