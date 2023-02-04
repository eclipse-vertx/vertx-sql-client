package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;

public class UnsignedInt16ColumnReader extends SignedInt16ColumnReader {
  public UnsignedInt16ColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return Short.toUnsignedInt(primitive(rowIdx));
  }

  @Override
  protected Object[] allocateTwoDimArray(Class<?> desired, int dim1, int dim2) {
    if (desired == int.class) {
      return new int[dim1][dim2];
    }
    return new Integer[dim1][dim2];
  }

  @Override
  protected Object allocateOneDimArray(Class<?> desired, int length) {
    if (desired == int.class) {
      return new int[length];
    }
    return new Integer[length];
  }
}
