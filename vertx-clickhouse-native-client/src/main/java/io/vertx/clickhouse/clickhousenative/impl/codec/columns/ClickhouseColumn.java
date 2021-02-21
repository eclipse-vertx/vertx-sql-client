package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import java.util.BitSet;

public abstract class ClickhouseColumn {
  protected final int nItems;
  protected final ClickhouseNativeColumnDescriptor columnDescriptor;
  protected BitSet nullsMap;
  protected Object items;

  protected ClickhouseColumn(int nItems, ClickhouseNativeColumnDescriptor columnDescriptor) {
    this.columnDescriptor = columnDescriptor;
    this.nItems = nItems;
  }

  public void readColumn(ByteBuf in){
    readStatePrefix(in);
    readData(in);
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
    if (items == null) {
      items = readItems(in);
      if (items == null) {
        return;
      }
    }
    afterReadItems(in);
  }

  protected abstract Object readItems(ByteBuf in);
  protected void afterReadItems(ByteBuf in) {
  }

  protected BitSet readNullsMap(ByteBuf in) {
    int nBytes = nItems / 8 + (nItems % 8 == 0 ? 0 : 1);
    return BitSet.valueOf(in.readSlice(nBytes).nioBuffer());
  }

  public boolean isPartial() {
    return items == null;
  }

  public Object getItems() {
    return items;
  }

  public Object getElement(int rowNo) {
    if (nullsMap != null && nullsMap.get(rowNo)) {
      return null;
    }
    return getElementInternal(rowNo);
  }

  protected Object getElementInternal(int rowNo) {
    return java.lang.reflect.Array.get(items, rowNo);
  }

  public ClickhouseNativeColumnDescriptor getColumnDescriptor() {
    return columnDescriptor;
  }
}
