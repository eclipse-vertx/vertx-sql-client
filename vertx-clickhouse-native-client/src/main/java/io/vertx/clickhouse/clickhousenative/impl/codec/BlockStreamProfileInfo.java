package io.vertx.clickhouse.clickhousenative.impl.codec;

public class BlockStreamProfileInfo {
  private final int rows;
  private final int blocks;
  private final int bytes;
  private final boolean appliedLimit;
  private final int rowsBeforeLimit;
  private final boolean calculatedRowsBeforeLimit;

  public BlockStreamProfileInfo(int rows, int blocks, int bytes, boolean appliedLimit, int rowsBeforeLimit,
                                boolean calculatedRowsBeforeLimit) {
    this.rows = rows;
    this.blocks = blocks;
    this.bytes = bytes;
    this.appliedLimit = appliedLimit;
    this.rowsBeforeLimit = rowsBeforeLimit;
    this.calculatedRowsBeforeLimit = calculatedRowsBeforeLimit;
  }

  public int getRows() {
    return rows;
  }

  public int getBlocks() {
    return blocks;
  }

  public int getBytes() {
    return bytes;
  }

  public boolean getAppliedLimit() {
    return appliedLimit;
  }

  public int getRowsBeforeLimit() {
    return rowsBeforeLimit;
  }

  public boolean getCalculatedRowsBeforeLimit() {
    return calculatedRowsBeforeLimit;
  }

  @Override
  public String toString() {
    return "BlockStreamProfileInfo{" +
      "rows=" + rows +
      ", blocks=" + blocks +
      ", bytes=" + bytes +
      ", appliedLimit=" + appliedLimit +
      ", rowsBeforeLimit=" + rowsBeforeLimit +
      ", calculatedRowsBeforeLimit=" + calculatedRowsBeforeLimit +
      '}';
  }
}
