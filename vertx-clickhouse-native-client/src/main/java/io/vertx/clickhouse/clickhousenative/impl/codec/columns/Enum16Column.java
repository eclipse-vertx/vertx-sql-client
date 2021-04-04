package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.Map;

public class Enum16Column extends ClickhouseColumn {
  private final Map<? extends Number, String> enumVals;
  private final boolean enumsByName;

  public Enum16Column(ClickhouseNativeColumnDescriptor descriptor, Map<? extends Number, String> enumVals, boolean enumsByName) {
    super(descriptor);
    this.enumVals = enumVals;
    this.enumsByName = enumsByName;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new Enum16ColumnReader(nRows, descriptor, enumVals);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    throw new IllegalArgumentException("not implemented");
  }
}
