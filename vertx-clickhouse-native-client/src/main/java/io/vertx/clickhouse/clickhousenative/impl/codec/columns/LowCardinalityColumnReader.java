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

import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSource;

import java.util.BitSet;

public class LowCardinalityColumnReader extends ClickhouseColumnReader {
  public static final long SUPPORTED_SERIALIZATION_VERSION = 1;

  private static final ClickhouseColumn[] KEY_COLUMNS = new ClickhouseColumn[] {
    ClickhouseColumns.columnForSpec("UInt8", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt16", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt32", "lcKeyColumn", null),
    ClickhouseColumns.columnForSpec("UInt64", "lcKeyColumn", null)
  };
  public static final Object[] EMPTY_ARRAY = new Object[0];

  private final ClickhouseNativeColumnDescriptor indexDescr;
  private final ClickhouseNativeDatabaseMetadata md;
  private ClickhouseColumnReader indexColumn;
  private Long serType;
  private Long indexSize;
  private Long nKeys;
  Long keysSerializationVersion;

  private ClickhouseColumnReader keysColumn;

  public LowCardinalityColumnReader(int nRows, ClickhouseNativeColumnDescriptor descr, ClickhouseNativeColumnDescriptor indexColumn, ClickhouseNativeDatabaseMetadata md) {
    super(nRows, descr);
    this.indexDescr = indexColumn;
    this.md = md;
  }

  @Override
  protected Object readStatePrefix(ClickhouseStreamDataSource in) {
    if (keysSerializationVersion == null) {
      if (in.readableBytes() >= 4) {
        keysSerializationVersion = in.readLongLE();
        if (keysSerializationVersion != SUPPORTED_SERIALIZATION_VERSION) {
          throw new IllegalStateException("unsupported keysSerializationVersion: " + keysSerializationVersion);
        }
      }
    }
    return keysSerializationVersion;
  }

  @Override
  protected BitSet readNullsMap(ClickhouseStreamDataSource in) {
    return null;
  }

  @Override
  protected void readData(ClickhouseStreamDataSource in) {
    if (keysSerializationVersion == null) {
      return;
    }
    if (indexSize == null) {
      if (in.readableBytes() < 8 + 8) {
        return;
      }
      serType = in.readLongLE();
      indexSize = in.readLongLE();
    }
    if (indexSize > Integer.MAX_VALUE) {
      throw new IllegalArgumentException("low cardinality index is too big (" + indexSize + "), max " + Integer.MAX_VALUE);
    }
    if (indexColumn == null) {
      int sz = indexSize.intValue();
      indexColumn = ClickhouseColumns.columnForSpec(indexDescr, md, true).reader(sz);
    }
    if (indexColumn.isPartial()) {
      indexColumn.readColumn(in);
      if (indexColumn.isPartial()) {
        return;
      }
    }
    if (nKeys == null) {
      if (in.readableBytes() < 8) {
        return;
      }
      nKeys = in.readLongLE();
    }
    int keyType = (int)(serType & 0xf);
    if (keysColumn == null) {
      keysColumn = uintColumn(keyType).reader(nRows);
    }
    keysColumn.readColumn(in);
    itemsArray = EMPTY_ARRAY;
  }

  //called by Array column
  @Override
  protected Object readItems(ClickhouseStreamDataSource in) {
    if (isPartial()) {
      readData(in);
      if (isPartial()) {
        return null;
      }
    }
    return itemsArray;
  }

  @Override
  protected Object[] asObjectsArray(Class<?> desired) {
    return super.asObjectsArrayWithGetElement(desired);
  }

  @Override
  public boolean isPartial() {
    return indexSize == null || indexColumn.isPartial() || nKeys == null || keysColumn.isPartial();
  }

  @Override
  public Object getElement(int rowIdx, Class<?> desired) {
    int key = ((Number)keysColumn.getElement(rowIdx, Number.class)).intValue();
    if (columnDescriptor.isNullable() && key == 0) {
      return null;
    }
    //caveat: caller may change index contents for byte[] elements
    return indexColumn.getElementInternal(key, desired);
  }

  static ClickhouseColumn uintColumn(int code) {
    if (code < 0 || code >= KEY_COLUMNS.length) {
      throw new IllegalArgumentException("unknown low-cardinality key-column code " + code);
    }
    return KEY_COLUMNS[code];
  }
}
