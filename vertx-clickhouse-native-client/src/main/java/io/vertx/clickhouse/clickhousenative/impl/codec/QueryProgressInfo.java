package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;

public class QueryProgressInfo {
  private final int serverRevision;

  private Integer rows;
  private Integer bytes;
  private Integer totalRows;
  private Integer writtenRows;
  private Integer writtenBytes;

  public QueryProgressInfo(ClickhouseNativeDatabaseMetadata md) {
    this.serverRevision = md.getRevision();
  }

  public void readFrom(ByteBuf in) {
    if (rows == null) {
      rows = ByteBufUtils.readULeb128(in);
      if (rows == null) {
        return;
      }
    }

    if (bytes == null) {
      bytes = ByteBufUtils.readULeb128(in);
      if (bytes == null) {
        return;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TOTAL_ROWS_IN_PROGRESS) {
      if (totalRows == null) {
        totalRows = ByteBufUtils.readULeb128(in);
      }
      if (totalRows == null) {
        return;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_WRITE_INFO) {
      if (writtenRows == null) {
        writtenRows = ByteBufUtils.readULeb128(in);
      }
      if (writtenRows == null) {
        return;
      }
      if (writtenBytes == null) {
        writtenBytes = ByteBufUtils.readULeb128(in);
        if (writtenBytes == null) {
          return;
        }
      }
    }
  }

  public boolean isComplete() {
    return !isPartial();
  }

  public boolean isPartial() {
    return writtenBytes == null;
  }

  @Override
  public String toString() {
    return "QueryProgressInfo{" +
      "serverRevision=" + serverRevision +
      ", rows=" + rows +
      ", bytes=" + bytes +
      ", totalRows=" + totalRows +
      ", writtenRows=" + writtenRows +
      ", writtenBytes=" + writtenBytes +
      '}';
  }
}
