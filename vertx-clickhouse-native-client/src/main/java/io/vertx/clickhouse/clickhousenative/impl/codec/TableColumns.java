package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.vertx.clickhouse.clickhousenative.impl.ColumnOrientedBlock;

import java.util.List;

public class TableColumns {
  private final List<String> msg;
  private final ColumnOrientedBlock columnDefinition;

  public TableColumns(List<String> msg, ColumnOrientedBlock columnDefinition) {
    this.msg = msg;
    this.columnDefinition = columnDefinition;
  }

  public List<String> msg() {
    return msg;
  }

  public ColumnOrientedBlock columnDefinition() {
    return columnDefinition;
  }
}
