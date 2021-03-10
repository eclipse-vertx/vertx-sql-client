package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum8Column extends UInt8Column {
  public static final int ELEMENT_SIZE = 1;
  private final Map<Byte, String> enumVals;

  public Enum8Column(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals) {
    super(nRows, descr);
    this.enumVals = (Map<Byte, String>) enumVals;
  }

  @Override
  protected Object getElementInternal(int rowIdx) {
    Byte key = (Byte) super.getElementInternal(rowIdx);
    return enumVals.get(key);
  }
}
