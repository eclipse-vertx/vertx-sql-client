package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;


public class UInt8Column extends ClickhouseColumn {
  public static final int ELEMENT_SIZE = 1;

  public UInt8Column(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
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
  protected void serializeDataElement(ClickhouseStreamDataSink sink, Object val) {
    Number number = (Number)val;
    byte b = number == null ? 0 : number.byteValue();
    sink.writeByte(b);
  }

  @Override
  protected void serializeDataNull(ClickhouseStreamDataSink sink) {
    sink.writeByte(0);
  }
}
