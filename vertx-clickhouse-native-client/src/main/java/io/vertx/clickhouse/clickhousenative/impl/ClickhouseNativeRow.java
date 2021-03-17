package io.vertx.clickhouse.clickhousenative.impl;

import io.vertx.clickhouse.clickhousenative.impl.codec.columns.ClickhouseColumn;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;

import java.nio.charset.Charset;
import java.sql.JDBCType;
import java.util.List;

public class ClickhouseNativeRow implements Row {
  private final int rowNo;
  private final Charset stringCharset;
  private final ClickhouseNativeRowDesc rowDesc;
  private final ColumnOrientedBlock block;

  public ClickhouseNativeRow(int rowNo, ClickhouseNativeRowDesc rowDesc, ColumnOrientedBlock block, ClickhouseNativeDatabaseMetadata md) {
    this.rowNo = rowNo;
    this.rowDesc = rowDesc;
    this.block = block;
    this.stringCharset = md.getStringCharset();
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
    return getValue(columnIndex, String.class);
  }

  private Object getValue(int columnIndex, Class<?> desired) {
    List<ClickhouseColumn> data = block.getData();
    ClickhouseColumn column = data.get(columnIndex);
    Object columnData = column.getElement(rowNo, desired);
    return columnData;
    //if (encodeStrings && columnData != null && column.columnDescriptor().jdbcType() == JDBCType.VARCHAR) {
    //  return new String((byte[]) columnData, stringCharset);
    //} else {
    //  return columnData;
    //}
  }

  @Override
  public  <T> T get(Class<T> type, int position) {
    if (type == null) {
      throw new IllegalArgumentException("Accessor type can not be null");
    }
    Object value = getValue(position, type);
    if (value != null && type.isAssignableFrom(value.getClass())) {
      return type.cast(value);
    }
    return null;
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
    return block.numColumns();
  }

  @Override
  public void clear() {
    throw new IllegalStateException("not implemented");
  }
}
