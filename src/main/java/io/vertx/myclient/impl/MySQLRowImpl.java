package io.vertx.myclient.impl;

import io.vertx.myclient.impl.protocol.backend.ColumnDefinition;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowInternal;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.UUID;

// TODO how we design Row API ? Some data types in Postgres may not be compatible with some MySQL data types. need to check this

public class MySQLRowImpl extends ArrayTuple implements RowInternal {

  private final ColumnMetadata columnMetadata;
  MySQLRowImpl next;

  public MySQLRowImpl(ColumnMetadata columnMetadata) {
    super(columnMetadata.getColumnDefinitions().length);
    this.columnMetadata = columnMetadata;
  }

  @Override
  public <T> T[] getValues(Class<T> type, int idx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnName(int pos) {
    final ColumnDefinition[] columnDefinitions = columnMetadata.getColumnDefinitions();
    return pos < 0 || columnDefinitions.length - 1 < pos ? null : columnDefinitions[pos].getName();
  }

  @Override
  public int getColumnIndex(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    final ColumnDefinition[] columnDefinitions = columnMetadata.getColumnDefinitions();
    for (int idx = 0;idx < columnDefinitions.length;idx++) {
      if (columnDefinitions[idx].getName().equals(name)) {
        return idx;
      }
    }
    return -1;
  }

  @Override
  public Boolean getBoolean(String name) {
    int pos = columnMetadata.columnIndex(name);
    // in MySQL BOOLEAN type is mapped to TINYINT
    return pos == -1 ? null :( (byte) getValue(pos) == 1);
  }

  @Override
  public Object getValue(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getValue(pos);
  }

  @Override
  public Short getShort(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getShort(pos);
  }

  @Override
  public Integer getInteger(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getInteger(pos);
  }

  @Override
  public Long getLong(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getLong(pos);
  }

  @Override
  public Float getFloat(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getFloat(pos);
  }

  @Override
  public Double getDouble(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getDouble(pos);
  }

  @Override
  public String getString(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getString(pos);
  }

  @Override
  public <T> T get(Class<T> type, int pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer getBuffer(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Temporal getTemporal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate getLocalDate(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime getLocalTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime getLocalDateTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID getUUID(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer[] getIntegerArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean[] getBooleanArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Short[] getShortArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long[] getLongArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float[] getFloatArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getDoubleArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getStringArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate[] getLocalDateArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime[] getLocalTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getBufferArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getUUIDArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void setNext(RowInternal next) {
    this.next = (MySQLRowImpl) next;
  }

  @Override
  public RowInternal getNext() {
    return next;
  }
}
