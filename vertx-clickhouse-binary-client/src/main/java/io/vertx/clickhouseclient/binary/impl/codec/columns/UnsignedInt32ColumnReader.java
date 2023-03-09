package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;

public class UnsignedInt32ColumnReader extends SignedInt32ColumnReader {
  public UnsignedInt32ColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return Integer.toUnsignedLong(primitive(rowIdx));
  }

  @Override
  protected Object[] allocateTwoDimArray(Class<?> desired, int dim1, int dim2) {
    if (desired == long.class) {
      return new long[dim1][dim2];
    }
    return new Long[dim1][dim2];
  }

  @Override
  protected Object allocateOneDimArray(Class<?> desired, int length) {
    if (desired == long.class) {
      return new long[length];
    }
    return new Long[length];
  }
}
