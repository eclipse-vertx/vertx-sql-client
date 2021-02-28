package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRow;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.impl.RowDecoder;

import java.util.stream.Collector;

class RowResultDecoder<C, R> extends RowDecoder<C, R> {
  private static final Logger LOG = LoggerFactory.getLogger(RowResultDecoder.class);

  private final ClickhouseNativeRowDesc rowDesc;
  private ColumnOrientedBlock block;
  private int rowNo;

  protected RowResultDecoder(Collector<Row, C, R> collector, ClickhouseNativeRowDesc rowDesc) {
    super(collector);
    this.rowDesc = rowDesc;
  }

  @Override
  protected Row decodeRow(int len, ByteBuf in) {
    LOG.info("generating row " + (rowNo + 1));
    ClickhouseNativeRow row = new ClickhouseNativeRow(rowNo, rowDesc, block);
    ++rowNo;
    return row;
  }

  public void generateRows(ColumnOrientedBlock block) {
    this.block = block;
    this.rowNo = 0;
    for (int i = 0; i < block.numRows(); ++i) {
      this.handleRow(-1, null);
    }
    this.block = null;
    this.rowNo = 0;
  }

  public ClickhouseNativeRowDesc getRowDesc() {
    return rowDesc;
  }
}
