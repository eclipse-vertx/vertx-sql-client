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
