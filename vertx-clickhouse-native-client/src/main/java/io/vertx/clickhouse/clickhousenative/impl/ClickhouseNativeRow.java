package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

public class ClickhouseNativeRow implements Row {
  private final int rowNo;
  private final Charset stringCharset;
  private final ClickhouseNativeRowDesc rowDesc;
  private final ColumnOrientedBlock block;

  public ClickhouseNativeRow(int rowNo, ClickhouseNativeRowDesc rowDesc, ColumnOrientedBlock block) {
    this.rowNo = rowNo;
    this.rowDesc = rowDesc;
    this.block = block;
    this.stringCharset = StandardCharsets.UTF_8;
  }

  @Override
  public String getColumnName(int pos) {
    return rowDesc.columnNames().get(pos);
  }

  @Override
  public int getColumnIndex(String column) {
    return rowDesc.columnIndex(column);
  }

  @Override
  public Object getValue(int columnIndex) {
    return block.getData().get(columnIndex).getElement(rowNo);
  }

  @Override
  public String getString(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof String) {
      return (String) val;
    } else if (val instanceof Enum<?>) {
      return ((Enum<?>) val).name();
    } else if (val.getClass() == byte[].class) {
      return new String((byte[])val, stringCharset);
    } else {
      throw new ClassCastException("Invalid String value type " + val.getClass());
    }
  }

  @Override
  public Tuple addValue(Object value) {
    throw new IllegalStateException("not implemented");
  }

  @Override
  public int size() {
    return block.numRows();
  }

  @Override
  public void clear() {
    throw new IllegalStateException("not implemented");
  }
}
