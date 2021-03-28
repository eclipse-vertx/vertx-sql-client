package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;
import io.vertx.sqlclient.Tuple;

import java.util.List;

public class UInt8ColumnReader extends ClickhouseColumnReader {
  public static final int ELEMENT_SIZE = 1;

  public UInt8ColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      byte[] data = new byte[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data[i] = in.readByte();
        } else {
          in.skipBytes(ELEMENT_SIZE);
        }
      }
      return data;
    }
    return null;
  }

  @Override
  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    byte element = ((byte[])this.itemsArray)[rowIdx];
    if (columnDescriptor.isUnsigned()) {
      return (short)Byte.toUnsignedInt(element);
    }
    return element;
  }

  @Override
  protected Object[] asObjectsArray(Class<?> desired) {
    return asObjectsArrayWithGetElement(desired);
  }

  @Override
  protected Object[] allocateArray(Class<?> desired, int length) {
    if (columnDescriptor.isUnsigned()) {
      return new Short[length];
    }
    return new Byte[length];
  }

  @Override
  public ClickhouseColumnWriter writer(List<Tuple> data, int columnIndex) {
    return new UInt8ColumnWriter(data, columnDescriptor, columnIndex);
  }
}
