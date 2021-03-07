package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;

import java.util.List;
import java.util.Map;

public class RowOrientedBlock extends BaseBlock {

  public RowOrientedBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                          List<ClickhouseColumn> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    super(columnsWithTypes, data, blockInfo, md);
  }

  public void serializeTo(ClickhouseStreamDataSink sink) {
    if (getMd().getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
      getBlockInfo().serializeTo(sink);
    }
    //n_columns
    sink.writeULeb128(0);
    //n_rows
    sink.writeULeb128(0);
    //TODO smagellan
  }
}
