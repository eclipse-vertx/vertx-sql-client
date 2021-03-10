package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum16Column extends UInt16Column {
  public static final int ELEMENT_SIZE = 2;
  private final Map<Short, String> enumVals;

  public Enum16Column(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals) {
    super(nRows, descr);
    this.enumVals = (Map<Short, String>) enumVals;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    Short key = (Short) super.getElementInternal(rowIdx);
    return enumVals.get(key);
  }
}
