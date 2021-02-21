package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

public enum ColumnSpec {
  UInt32(4),
  String(null);

  private final Integer elementSize;

  ColumnSpec(Integer elementSize) {
    this.elementSize = elementSize;
  }

  public Integer elSize() {
    return elementSize;
  }
}
