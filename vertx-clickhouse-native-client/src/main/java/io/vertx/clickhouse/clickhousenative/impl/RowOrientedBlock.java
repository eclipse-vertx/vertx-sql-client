package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseNativeColumnDescriptor;
import io.vertx.clickhouse.clickhousenative.impl.codec.ClickhouseStreamDataSink;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumnWriter;
import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumns;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.RowDesc;

import java.util.List;

public class RowOrientedBlock {
  private final RowDesc rowDesc;
  private final List<Tuple> data;
  private final BlockInfo blockInfo;
  private final ClickhouseNativeDatabaseMetadata md;
  private final ClickhouseColumnWriter[] writers;

  public RowOrientedBlock(RowDesc rowDesc,
                          List<Tuple> data, ClickhouseNativeDatabaseMetadata md) {
    this.rowDesc = rowDesc;
    this.data = data;
    this.blockInfo = new BlockInfo();
    this.md = md;
    this.writers = buildWriters();
  }

  private ClickhouseColumnWriter[] buildWriters() {
    ClickhouseColumnWriter[] ret = new ClickhouseColumnWriter[nColumns()];
    for (int columnIndex = 0; columnIndex < nColumns(); ++columnIndex) {
      ClickhouseNativeColumnDescriptor descr = (ClickhouseNativeColumnDescriptor) rowDesc.columnDescriptor().get(columnIndex);
      ClickhouseColumnWriter writer = ClickhouseColumns.columnForSpec(descr, md).writer(data, columnIndex);
      ret[columnIndex] = writer;
    }
    return ret;
  }

  public void serializeAsBlock(ClickhouseStreamDataSink sink, int fromRow, int toRow) {
    if (md.getRevision() >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_BLOCK_INFO) {
      blockInfo.serializeTo(sink);
    }
    //n_columns
    sink.writeULeb128(nColumns());
    //n_rows
    int nRows = toRow - fromRow;
    sink.writeULeb128(nRows);
    //TODO smagellan
    for (int columnIndex = 0; columnIndex < nColumns(); ++columnIndex) {
      ClickhouseNativeColumnDescriptor descr = (ClickhouseNativeColumnDescriptor) rowDesc.columnDescriptor().get(columnIndex);
      sink.writePascalString(descr.name());
      sink.writePascalString(descr.getUnparsedNativeType());
      writers[columnIndex].serializeColumn(sink, fromRow, toRow);
    }
  }

  public int nColumns() {
    return rowDesc.columnDescriptor().size();
  }

  public int totalRows() {
    return data.size();
  }
}
