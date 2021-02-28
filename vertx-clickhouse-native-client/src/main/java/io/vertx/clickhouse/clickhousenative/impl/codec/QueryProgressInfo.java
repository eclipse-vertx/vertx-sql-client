package io.vertx.clickhouse.clickhousenative.impl.codec;

public class QueryProgressInfo {
  private final int rows;
  private final int bytes;
  private final int totalRows;
  private final int writtenRows;
  private final int writtenBytes;

  public QueryProgressInfo(int rows, int bytes, int totalRows, int writtenRows, int writtenBytes) {
    this.rows = rows;
    this.bytes = bytes;
    this.totalRows = totalRows;

    this.writtenRows = writtenRows;
    this.writtenBytes = writtenBytes;
  }

  public int getRows() {
    return rows;
  }

  public int getBytes() {
    return bytes;
  }

  public int getTotalRows() {
    return totalRows;
  }

  public int getWrittenRows() {
    return writtenRows;
  }

  public int getWrittenBytes() {
    return writtenBytes;
  }

  @Override
  public String toString() {
    return "QueryProgressInfo{" +
      "rows=" + rows +
      ", bytes=" + bytes +
      ", totalRows=" + totalRows +
      ", writtenRows=" + writtenRows +
      ", writtenBytes=" + writtenBytes +
      '}';
  }
}
