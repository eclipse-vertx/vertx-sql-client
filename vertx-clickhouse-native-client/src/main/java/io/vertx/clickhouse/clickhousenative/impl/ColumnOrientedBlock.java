/*
 *
 *  * Copyright (c) 2021 Vladimir Vishnevsky
 *  *
 *  * This program and the accompanying materials are made available under the
 *  * terms of the Eclipse Public License 2.0 which is available at
 *  * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 *  * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *  *
 *  * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 *
 */

package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumnReader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColumnOrientedBlock extends BaseBlock {
  public ColumnOrientedBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                             List<ClickhouseColumnReader> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    super(columnsWithTypes, data, blockInfo, md);
  }

  public int numColumns() {
    Collection<ClickhouseColumnReader> dt = getData();
    return dt == null ? 0 : dt.size();
  }

  public int numRows() {
    if (numColumns() > 0) {
      ClickhouseColumnReader firstColumn = getData().iterator().next();
      return firstColumn.nRows();
    } else {
      return 0;
    }
  }

  public List<ClickhouseNativeRowImpl> rows() {
    int numRows = numRows();
    List<ClickhouseNativeRowImpl> ret = new ArrayList<>(numRows);
    for (int i = 0; i < numRows; ++i) {
      ret.add(row(i));
    }
    return ret;
  }

  public ClickhouseNativeRowImpl row(int rowNo) {
    return new ClickhouseNativeRowImpl(rowNo, rowDesc, this, md);
  }
}
