package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.clickhouse.clickhousenative.ClickhouseConstants;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeDatabaseMetadata;

public class QueryProgressInfoReader {
  private final int serverRevision;

  private Integer rows;
  private Integer bytes;
  private Integer totalRows;
  private Integer writtenRows;
  private Integer writtenBytes;

  public QueryProgressInfoReader(ClickhouseNativeDatabaseMetadata md) {
    this.serverRevision = md.getRevision();
  }

  public QueryProgressInfo readFrom(ByteBuf in) {
    if (rows == null) {
      rows = ByteBufUtils.readULeb128(in);
      if (rows == null) {
        return null;
      }
    }

    if (bytes == null) {
      bytes = ByteBufUtils.readULeb128(in);
      if (bytes == null) {
        return null;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_TOTAL_ROWS_IN_PROGRESS) {
      if (totalRows == null) {
        totalRows = ByteBufUtils.readULeb128(in);
      }
      if (totalRows == null) {
        return null;
      }
    }

    if (serverRevision >= ClickhouseConstants.DBMS_MIN_REVISION_WITH_CLIENT_WRITE_INFO) {
      if (writtenRows == null) {
        writtenRows = ByteBufUtils.readULeb128(in);
      }
      if (writtenRows == null) {
        return null;
      }
      if (writtenBytes == null) {
        writtenBytes = ByteBufUtils.readULeb128(in);
        if (writtenBytes == null) {
          return null;
        }
      }
    }
    return new QueryProgressInfo(rows, bytes, totalRows, writtenRows, writtenBytes);
  }
}
