package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.sqlclient.Tuple;

import java.util.List;
import java.util.Map;

public class Enum16Column extends UInt16Column {
  private final Map<? extends Number, String> enumVals;
  private final EnumResolutionMethod resolutionMethod;

  public Enum16Column(ClickhouseNativeColumnDescriptor descriptor, Map<? extends Number, String> enumVals, EnumResolutionMethod resolutionMethod) {
    super(descriptor);
    this.enumVals = enumVals;
    this.resolutionMethod = resolutionMethod;
  }

  @Override
  public ClickhouseColumnReader reader(int nRows) {
    return new Enum16ColumnReader(nRows, descriptor, enumVals, resolutionMethod);
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new Enum16ColumnWriter(data, descriptor, columnIndex, enumVals, resolutionMethod);
  }
}
