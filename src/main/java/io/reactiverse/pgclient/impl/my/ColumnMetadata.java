package io.reactiverse.pgclient.impl.my;

import io.reactiverse.pgclient.impl.my.codec.datatype.DataFormat;
import io.reactiverse.pgclient.impl.my.protocol.backend.ColumnDefinition;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// similar to RowDescription in Postgres
public class ColumnMetadata {
  private final ColumnDefinition[] columnDefinitions;
  private final List<String> columnNames;
  private final DataFormat dataFormat;

  public ColumnMetadata(ColumnDefinition[] columnDefinitions, DataFormat dataFormat) {
    this.columnDefinitions = columnDefinitions;
    this.columnNames = Arrays.stream(columnDefinitions).map(ColumnDefinition::getName).collect(Collectors.toList());
    this.dataFormat = dataFormat;
  }

  public int columnIndex(String columnName) {
    if (columnName == null) {
      throw new IllegalArgumentException("Column name can not be null");
    }
    return columnNames.indexOf(columnName);
  }

  public ColumnDefinition[] getColumnDefinitions() {
    return columnDefinitions;
  }

  public List<String> getColumnNames() {
    return columnNames;
  }

  public DataFormat getDataFormat() {
    return dataFormat;
  }
}
