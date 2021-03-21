package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

public class UInt32Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 4;

  public UInt32Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    super(nRows, columnDescriptor);
  }

  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= ELEMENT_SIZE * nRows) {
      int[] data = new int[nRows];
      for (int i = 0; i < nRows; ++i) {
        if (nullsMap == null || !nullsMap.get(i)) {
          data[i] = in.readIntLE();
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
    int element = ((int[])this.itemsArray)[rowIdx];
    if (columnDescriptor.isUnsigned()) {
      return Integer.toUnsignedLong(element);
    }
    return element;
  }

  @Override
  protected Object[] asObjectsArray(Class<?> desired) {
    return asObjectsArrayWithGetElement(desired);
  }

  @Override
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Number number = (Number)val;
    int b = number == null ? 0 : number.intValue();
    sink.writeIntLE(b);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeIntLE(0);
  }
}
