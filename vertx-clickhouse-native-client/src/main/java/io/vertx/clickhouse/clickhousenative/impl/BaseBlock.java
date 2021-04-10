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
import io.vertx.sqlclient.desc.ColumnDescriptor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BaseBlock {
  private final Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes;
  protected final ClickhouseNativeRowDesc rowDesc;
  private final List<ClickhouseColumnReader> data;
  private final BlockInfo blockInfo;
  protected final ClickhouseNativeDatabaseMetadata md;

  public BaseBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                   List<ClickhouseColumnReader> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    this.columnsWithTypes = columnsWithTypes;
    this.rowDesc = buildRowDescriptor(columnsWithTypes);
    this.data = data;
    this.blockInfo = blockInfo;
    this.md = md;
  }

  public Map<String, ClickhouseNativeColumnDescriptor> getColumnsWithTypes() {
    return columnsWithTypes;
  }

  public List<ClickhouseColumnReader> getData() {
    return data;
  }

  public BlockInfo getBlockInfo() {
    return blockInfo;
  }

  public ClickhouseNativeDatabaseMetadata getMd() {
    return md;
  }

  public ClickhouseNativeRowDesc rowDesc() {
    return rowDesc;
  }

  private ClickhouseNativeRowDesc buildRowDescriptor(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes) {
    List<String> columnNames = new ArrayList<>(columnsWithTypes.keySet());
    List<ColumnDescriptor> columnTypes = new ArrayList<>(columnsWithTypes.values());
    return new ClickhouseNativeRowDesc(columnNames, columnTypes);
  }
}
