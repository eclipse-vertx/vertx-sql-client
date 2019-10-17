package io.vertx.mssqlclient.impl.codec;

import io.vertx.sqlclient.impl.RowDesc;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class MSSQLRowDesc extends RowDesc {
  final ColumnData[] columnDatas;

  MSSQLRowDesc(ColumnData[] columnDatas) {
    super(Stream.of(columnDatas).map(ColumnData::colName).collect(Collectors.toList()));
    this.columnDatas = columnDatas;
  }
}
