package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class LowCardinalityColumn extends ClickhouseColumn {
  private final ClickhouseNativeDatabaseMetadata md;
  public LowCardinalityColumn(ClickhouseNativeColumnDescriptor descriptor, ClickhouseNativeDatabaseMetadata md) {
    super(descriptor);
    this.md = md;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new LowCardinalityColumnReader(nRows, descriptor, md);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalArgumentException("not implemented");
  }
}
