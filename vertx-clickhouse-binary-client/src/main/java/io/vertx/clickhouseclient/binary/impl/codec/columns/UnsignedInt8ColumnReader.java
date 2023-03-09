package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;

public class UnsignedInt8ColumnReader extends SignedInt8ColumnReader {

  public UnsignedInt8ColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return (short)Byte.toUnsignedInt(primitive(rowIdx));
  }

  @Override
  protected Object[] allocateTwoDimArray(Class<?> desired, int dim1, int dim2) {
    if (desired == short.class) {
      return new short[dim1][dim2];
    }
    return new Short[dim1][dim2];
  }

  @Override
  protected Object allocateOneDimArray(Class<?> desired, int length) {
    if (desired == short.class) {
      return new short[length];
    }
    return new Short[length];
  }
}
