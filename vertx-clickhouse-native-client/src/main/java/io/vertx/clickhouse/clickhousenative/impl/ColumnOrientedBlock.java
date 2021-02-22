package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ColumnOrientedBlock extends BaseBlock {
  public static final ColumnOrientedBlock PARTIAL = new ColumnOrientedBlock(null, null, null, null);

  public ColumnOrientedBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                             List<ClickhouseColumn> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    super(columnsWithTypes, data, blockInfo, md);
  }

  public int numColumns() {
    Collection<ClickhouseColumn> dt = getData();
    return dt == null ? 0 : dt.size();
  }

  public int numRows() {
    if (numColumns() > 0) {
      ClickhouseColumn firstColumn = getData().iterator().next();
      return firstColumn.nRows();
    } else {
      return 0;
    }
  }
}
