package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class UUIDColumn extends ClickhouseColumn {
  public UUIDColumn(ClickhouseNativeColumnDescriptor descriptor) {
    super(descriptor);
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new UUIDColumnReader(nRows, descriptor);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalArgumentException("not implemented");
  }
}
