/*
 *
 *  Copyright (c) 2021 Vladimir Vishnevsky
 *
 *  This program and the accompanying materials are made available under the
 *  terms of the Eclipse Public License 2.0 which is available at
 *  http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 *  SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public abstract class ClickhouseColumnReader {
  private static final Object NOP_STATE = new Object();

  protected final int nRows;
  protected final ClickhouseNativeColumnDescriptor columnDescriptor;
  protected BitSet nullsMap;
  protected Object itemsArray;

  protected ClickhouseColumnReader(int nRows, ClickhouseNativeColumnDescriptor columnDescriptor) {
    this.columnDescriptor = columnDescriptor;
    this.nRows = nRows;
  }

  public ClickhouseNativeColumnDescriptor columnDescriptor() {
    return columnDescriptor;
  }

  public void readColumn(ClickhouseStreamDataSource in){
    readStatePrefix(in);
    readData(in);
  }

  public int nRows() {
    return nRows;
  }

  protected Object readStatePrefix(ClickhouseStreamDataSource in) {
    return NOP_STATE;
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

  protected Object[] readItemsAsObjects(ClickhouseStreamDataSource in, Class<?> desired) {
    itemsArray = readItems(in);
    if (itemsArray == null) {
      return null;
    }
    return asObjectsArray(desired);
  }

  protected Object[] asObjectsArray(Class<?> desired) {
    return (Object[]) itemsArray;
  }

  protected Object[] allocateArray(Class<?> desired, int length) {
    if (desired == null) {
      return new Object[length];
    }
    return (Object[]) java.lang.reflect.Array.newInstance(desired, length);
  }

  protected Object[] asObjectsArrayWithGetElement(Class<?> desired) {
    Object[] ret = allocateArray(desired, nRows);
    for (int i = 0; i < nRows; ++i) {
      ret[i] = getElement(i, desired);
    }
    return ret;
  }

  protected abstract Object readItems(ClickhouseStreamDataSource in);
  protected void afterReadItems(ClickhouseStreamDataSource in) {
  }

  protected BitSet readNullsMap(ClickhouseStreamDataSource in) {
    if (in.readableBytes() >= nRows) {
      BitSet bSet = new BitSet(nRows);
      for (int i = 0; i < nRows; ++i) {
        byte b = in.readByte();
        if (b != 0) {
          bSet.set(i);
        }
      }
      return bSet;
    }
    return null;
  }

  public boolean isPartial() {
    return itemsArray == null || (columnDescriptor.isNullable() && nullsMap == null);
  }

  public Object getItemsArray() {
    return itemsArray;
  }

  public Object getElement(int rowIdx, Class<?> desired) {
    if (nullsMap != null && nullsMap.get(rowIdx)) {
      return null;
    }
    return getElementInternal(rowIdx, desired);
  }

  protected Object getElementInternal(int rowIdx, Class<?> desired) {
    return java.lang.reflect.Array.get(itemsArray, rowIdx);
  }

  protected Object getObjectsArrayElement(int rowIdx) {
    Object[] data = (Object[]) itemsArray;
    return data[rowIdx];
  }
}
