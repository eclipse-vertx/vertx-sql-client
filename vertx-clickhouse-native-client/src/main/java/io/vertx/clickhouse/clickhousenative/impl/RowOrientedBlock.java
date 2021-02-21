package io.vertx.clickhouse.clickhousenative.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.codec.ByteBufUtils;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;

import java.util.List;
import java.util.Map;

public class RowOrientedBlock extends BaseBlock {

  public RowOrientedBlock(Map<String, ClickhouseNativeColumnDescriptor> columnsWithTypes,
                          List<ClickhouseColumn> data, BlockInfo blockInfo, ClickhouseNativeDatabaseMetadata md) {
    super(columnsWithTypes, data, blockInfo, md);
  }

  public void serializeTo(ByteBuf buf) {
    if (getMd().getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
      getBlockInfo().serializeTo(buf);
    }
    //n_columns
    ByteBufUtils.writeULeb128(0, buf);
    //n_rows
    ByteBufUtils.writeULeb128(0, buf);
    //TODO smagellan
  }
}
