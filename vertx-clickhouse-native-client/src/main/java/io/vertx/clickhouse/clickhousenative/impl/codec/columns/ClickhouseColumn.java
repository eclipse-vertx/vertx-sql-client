package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public abstract class ClickhouseColumn {
  protected ClickhouseNativeColumnDescriptor descriptor;

  public ClickhouseColumn(ClickhouseNativeColumnDescriptor descriptor) {
    this.descriptor = descriptor;
  }

  public abstract ClickhouseColumnReader reader(int nRows);

  public abstract ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex);

  public abstract Object nullValue();
}
