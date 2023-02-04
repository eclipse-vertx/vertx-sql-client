package io.vertx.clickhouseclient.binary.impl.codec.columns;

import io.vertx.clickhouseclient.binary.impl.codec.ClickhouseBinaryColumnDescriptor;

public class Int8ColumnReader extends UInt8ColumnReader {

  public Int8ColumnReader(int nRows, ClickhouseBinaryColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    
  }
}
