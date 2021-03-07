package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

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

  public void readColumn(ClickhouseStreamDataSource in){
    readStatePrefix(in);
    readData(in);
  }

  public int nRows() {
    return nRows;
  }

  protected void readStatePrefix(ClickhouseStreamDataSource in) {
  }

  protected void readData(ClickhouseStreamDataSource in) {
    if (columnDescriptor.isNullable() && nullsMap == null) {
      nullsMap = readNullsMap(in);
      if (nullsMap == null) {
        return;
      }
    }
    readDataInternal(in);
  }

  protected void readDataInternal(ClickhouseStreamDataSource in) {
    if (itemsArray == null) {
      itemsArray = readItems(in);
      if (itemsArray == null) {
        return;
      }
    }
    afterReadItems(in);
  }

  protected abstract Object readItems(ClickhouseStreamDataSource in);
  protected void afterReadItems(ClickhouseStreamDataSource in) {
  }

  protected BitSet readNullsMap(ClickhouseStreamDataSource in) {
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

  public Object getElement(int rowIdx) {
    if (nullsMap != null && nullsMap.get(rowIdx)) {
      return null;
    }
    return getElementInternal(rowIdx);
  }

  protected Object getElementInternal(int rowIdx) {
    return java.lang.reflect.Array.get(itemsArray, rowIdx);
  }

  protected Object getObjectsArrayElement(int rowIdx) {
    Object[] data = (Object[]) itemsArray;
    return data[rowIdx];
  }

  public ClickhouseNativeColumnDescriptor getColumnDescriptor() {
    return columnDescriptor;
  }
}
