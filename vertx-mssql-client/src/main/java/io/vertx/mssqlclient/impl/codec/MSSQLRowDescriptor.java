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

import io.vertx.sqlclient.internal.RowDescriptor;

import java.util.Arrays;

/**
 * An implementation of {@link RowDescriptor} for MSSQL.
 * <p>
 * When reading rows with a cursor, an extra column named {@code ROWSTAT} is returned by the server.
 * This column should not be conveyed to the user so this class filters it out.
 */
public class MSSQLRowDescriptor extends RowDescriptor {

  private final ColumnData[] columnDatas;
  private final boolean rowStat;

  private MSSQLRowDescriptor(ColumnData[] columnDatas, boolean hasRowStat) {
    super(columnDatas);
    this.columnDatas = columnDatas;
    this.rowStat = hasRowStat;
  }

  public static MSSQLRowDescriptor create(ColumnData[] columnDatas, boolean hasRowStat) {
    return new MSSQLRowDescriptor(hasRowStat ? Arrays.copyOf(columnDatas, columnDatas.length - 1) : columnDatas, hasRowStat);
  }

  public int size() {
    return columnDatas.length;
  }

  public ColumnData get(int index) {
    return columnDatas[index];
  }

  public boolean hasRowStat() {
    return rowStat;
  }
}
