package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum8ColumnReader extends UInt8ColumnReader implements EnumColumnReader {
  public static final int ELEMENT_SIZE = 1;
  private final EnumColumnDecoder columnRecoder;

  public Enum8ColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals, boolean enumsByName) {
    super(nRows, descr);
    this.columnRecoder = new EnumColumnDecoder(enumVals, enumsByName);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class desired) {
    Byte key = (Byte) super.getElementInternal(rowIdx, desired);
    return columnRecoder.recodeElement(key, desired);
  }

  @Override
  public Object[] recodeValues(Object[] src, Class desired) {
    return columnRecoder.recodeValues(src, desired);
  }
}
