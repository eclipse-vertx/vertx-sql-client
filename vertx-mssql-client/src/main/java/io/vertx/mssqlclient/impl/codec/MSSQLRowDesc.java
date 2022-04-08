/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.AbstractList;
import java.util.Collections;
import java.util.List;

/**
 * An implementation of {@link RowDesc} for MSSQL.
 * <p>
 * When reading rows with a cursor, an extra column named {@code ROWSTAT} is returned by the server.
 * This column should not be conveyed to the user so this class filters it out.
 */
public class MSSQLRowDesc extends RowDesc {

  private final ColumnData[] columnDatas;
  private final boolean rowStat;

  private MSSQLRowDesc(List<String> columnNames, List<ColumnDescriptor> columnDescriptors, ColumnData[] columnDatas, boolean hasRowStat) {
    super(columnNames, columnDescriptors);
    this.columnDatas = columnDatas;
    this.rowStat = hasRowStat;
  }

  public static MSSQLRowDesc create(ColumnData[] columnDatas, boolean hasRowStat) {
    if (columnDatas.length == 0) {
      return new MSSQLRowDesc(Collections.emptyList(), Collections.emptyList(), columnDatas, false);
    }
    int size = hasRowStat ? columnDatas.length - 1 : columnDatas.length;
    List<String> columnNames = new AbstractList<String>() {
      @Override
      public String get(int index) {
        if (index < 0 || index >= size) {
          throw new IndexOutOfBoundsException();
        }
        return columnDatas[index].name();
      }

      @Override
      public int size() {
        return size;
      }
    };
    List<ColumnDescriptor> columnDescriptors = new AbstractList<ColumnDescriptor>() {
      @Override
      public ColumnDescriptor get(int index) {
        if (index < 0 || index >= size) {
          throw new IndexOutOfBoundsException();
        }
        return columnDatas[index];
      }

      @Override
      public int size() {
        return size;
      }
    };
    return new MSSQLRowDesc(columnNames, columnDescriptors, columnDatas, hasRowStat);
  }

  public int size() {
    return rowStat ? columnDatas.length - 1 : columnDatas.length;
  }

  public ColumnData get(int index) {
    if (index < 0 || index >= size()) {
      throw new IndexOutOfBoundsException();
    }
    return columnDatas[index];
  }

  public boolean hasRowStat() {
    return rowStat;
  }
}
