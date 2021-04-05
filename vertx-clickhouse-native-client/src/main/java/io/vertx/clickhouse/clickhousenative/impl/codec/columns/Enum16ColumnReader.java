package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;

import java.util.Map;

public class Enum16ColumnReader extends UInt16ColumnReader implements EnumColumnReader {
  public static final int ELEMENT_SIZE = 2;
  private final EnumColumnDecoder columnRecoder;

  public Enum16ColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, Map<? extends Number, String> enumVals, EnumResolutionMethod resolutionMethod) {
    super(nRows, descr);
    this.columnRecoder = new EnumColumnDecoder(enumVals, resolutionMethod);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    Short key = (Short) super.getElementInternal(rowIdx, desired);
    return columnRecoder.recodeElement(key, desired);
  }

  @Override
  public Object[] recodeValues(Object[] src, Class desired) {
    return columnRecoder.recodeValues(src, desired);
  }
}
