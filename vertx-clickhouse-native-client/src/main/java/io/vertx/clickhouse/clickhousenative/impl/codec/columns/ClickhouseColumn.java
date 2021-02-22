package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import java.util.BitSet;

public abstract class ClickhouseColumn {
  protected final int nRows;
  protected final ClickhouseNativeColumnDescriptor columnDescriptor;
  protected BitSet nullsMap;
  protected Object itemsArray;

  protected ClickhouseColumn(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    this.columnDescriptor = columnDescriptor;
    this.nRows = nRows;
  }

  public void readColumn(ByteBuf in){
    readStatePrefix(in);
    readData(in);
  }

  public int nRows() {
    return nRows;
  }

  protected void readStatePrefix(ByteBuf in) {
  }

  protected void readData(ByteBuf in) {
    if (columnDescriptor.isNullable() && nullsMap == null) {
      nullsMap = readNullsMap(in);
      if (nullsMap == null) {
        return;
      }
    }
    readDataInternal(in);
  }

  protected void readDataInternal(ByteBuf in) {
    if (itemsArray == null) {
      itemsArray = readItems(in);
      if (itemsArray == null) {
        return;
      }
    }
    afterReadItems(in);
  }

  protected abstract Object readItems(ByteBuf in);
  protected void afterReadItems(ByteBuf in) {
  }

  protected BitSet readNullsMap(ByteBuf in) {
    int nBytes = nRows / 8 + (nRows % 8 == 0 ? 0 : 1);
    if (in.readableBytes() >= nBytes) {
      return BitSet.valueOf(in.readSlice(nBytes).nioBuffer());
    }
    return null;
  }

  public boolean isPartial() {
    return itemsArray == null;
  }

  public Object getItemsArray() {
    return itemsArray;
  }

  public Object getElement(int rowNo) {
    if (nullsMap != null && nullsMap.get(rowNo)) {
      return null;
    }
    return getElementInternal(rowNo);
  }

  protected Object getElementInternal(int rowNo) {
    return java.lang.reflect.Array.get(itemsArray, rowNo);
  }

  protected Object getObjectsArrayElement(int rowNo) {
    Object[] data = (Object[]) itemsArray;
    return data[rowNo];
  }

  public ClickhouseNativeColumnDescriptor getColumnDescriptor() {
    return columnDescriptor;
  }
}
