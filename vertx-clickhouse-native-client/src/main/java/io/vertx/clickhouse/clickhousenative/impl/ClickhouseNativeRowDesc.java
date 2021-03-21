package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.Collections;
import java.util.List;

public class ClickhouseNativeRowDesc extends RowDesc {
  public static final ClickhouseNativeRowDesc EMPTY = new ClickhouseNativeRowDesc(Collections.emptyList(), Collections.emptyList());

  public ClickhouseNativeRowDesc(List<String> columnNames) {
    super(columnNames);
  }

  public ClickhouseNativeRowDesc(List<String> columnNames, List<ColumnDescriptor> columnDescriptors) {
    super(columnNames, columnDescriptors);
  }
}
