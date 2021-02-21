package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ColumnSpec;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class BaseBlock {
  private final Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes;
  private final List<ClickhouseColumn> data;
  private final BlockInfo blockInfo;
  private final ClickhouseNativeDatabaseMetadata md;

  public BaseBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                   List<ClickhouseColumn> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    this.columnsWithTypes = columnsWithTypes;
    this.data = data;
    this.blockInfo = blockInfo;
    this.md = md;
  }

  public Map<String, ClickhouseNativeColumnDescriptor> getColumnsWithTypes() {
    return columnsWithTypes;
  }

  public List<ClickhouseColumn> getData() {
    return data;
  }

  public BlockInfo getBlockInfo() {
    return blockInfo;
  }

  public ClickhouseNativeDatabaseMetadata getMd() {
    return md;
  }
}
