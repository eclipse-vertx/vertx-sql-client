package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;
import io.vertx.sqlclient.data.Numeric;

public class UnsignedInt64ColumnReader extends SignedInt64ColumnReader {
  public UnsignedInt64ColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return Numeric.create(unsignedBi(primitive(rowIdx)));
  }

  @Override
  protected Object[] allocateTwoDimArray(Class<?> desired, int dim1, int dim2) {
    return new Numeric[dim1][dim2];
  }

  @Override
  protected Object allocateOneDimArray(Class<?> desired, int length) {
    return new Numeric[length];
  }
}
