/*
 * Copyright (C) 2019,2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.db2client.impl;

import java.math.BigDecimal;
import java.sql.RowId;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;

import io.vertx.core.buffer.Buffer;
import io.vertx.db2client.impl.drda.DB2RowId;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowDesc;

public class DB2RowImpl extends ArrayTuple implements Row {

  private final RowDesc rowDesc;

  public DB2RowImpl(RowDesc rowDesc) {
    super(rowDesc.columnNames().size());
    this.rowDesc = rowDesc;
  }

  @Override
  public <T> T get(Class<T> type, int position) {
    if (type == Boolean.class) {
      return type.cast(getBoolean(position));
    } else if (type == Byte.class) {
      return type.cast(getByte(position));
    } else if (type == Short.class) {
      return type.cast(getShort(position));
    } else if (type == Integer.class) {
      return type.cast(getInteger(position));
    } else if (type == Long.class) {
      return type.cast(getLong(position));
    } else if (type == Float.class) {
      return type.cast(getFloat(position));
    } else if (type == Double.class) {
      return type.cast(getDouble(position));
    } else if (type == Numeric.class) {
      return type.cast(getNumeric(position));
    } else if (type == String.class) {
      return type.cast(getString(position));
    } else if (type == Buffer.class) {
      return type.cast(getBuffer(position));
    } else if (type == LocalDate.class) {
      return type.cast(getLocalDate(position));
    } else if (type == LocalDateTime.class) {
      return type.cast(getLocalDateTime(position));
    } else if (type == LocalTime.class) {
      return type.cast(getLocalTime(position));
    } else if (type == Duration.class) {
      return type.cast(getDuration(position));
    } else if (type == RowId.class || type == DB2RowId.class) {
      return type.cast(getRowId(position));
    } else if (type == UUID.class) {
      return type.cast(getUUID(position));
    } else if (type.isEnum()) {
      return type.cast(getEnum(type, position));
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getName());
    }
  }

  @Override
  public <T> T[] getValues(Class<T> type, int idx) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String getColumnName(int pos) {
    List<String> columnNames = rowDesc.columnNames();
    return pos < 0 || columnNames.size() - 1 < pos ? null : columnNames.get(pos);
  }

  @Override
  public int getColumnIndex(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    int idx = rowDesc.columnIndex(name);
    if (idx >= 0) {
      return idx;
    }
    // Unless the column is renamed in the SQL query, the column name will be uppercase
    return rowDesc.columnIndex(name.toUpperCase());
  }

  @Override
  public Object getValue(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getValue(pos);
  }

  @Override
  public Boolean getBoolean(int pos) {
    // DB2 stores booleans as TINYINT
    Object val = getValue(pos);
    if (val instanceof Boolean) {
      return (Boolean) val;
    } else if (val instanceof Short) {
      return (Short) val != 0;
    }
    return null;
  }

  @Override
  public Boolean getBoolean(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getBoolean(pos);
  }

  @Override
  public Short getShort(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getShort(pos);
  }

  @Override
  public Integer getInteger(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getInteger(pos);
  }

  @Override
  public Long getLong(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getLong(pos);
  }

  @Override
  public Float getFloat(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getFloat(pos);
  }

  @Override
  public Double getDouble(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getDouble(pos);
  }

  public Numeric getNumeric(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getNumeric(pos);
  }

  @Override
  public String getString(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getString(pos);
  }

  @Override
  public Buffer getBuffer(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getBuffer(pos);
  }

  public RowId getRowId(int pos) {
    Object val = getValue(pos);
    if (val instanceof RowId) {
      return (RowId) val;
    } else {
      return null;
    }
  }

  public RowId getRowId(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getRowId(pos);
  }

  @Override
  public Temporal getTemporal(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate getLocalDate(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getLocalDate(pos);
  }

  @Override
  public LocalTime getLocalTime(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getLocalTime(pos);
  }

  @Override
  public LocalDateTime getLocalDateTime(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getLocalDateTime(pos);
  }

  @Override
  public OffsetTime getOffsetTime(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID getUUID(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getUUID(pos);
  }

  @Override
  public BigDecimal getBigDecimal(String column) {
    int pos = getColumnIndex(column);
    return pos == -1 ? null : getBigDecimal(pos);
  }

  @Override
  public Integer[] getIntegerArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean[] getBooleanArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Short[] getShortArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long[] getLongArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float[] getFloatArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getDoubleArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getStringArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate[] getLocalDateArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime[] getLocalTimeArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime[] getArrayOfLocalDateTimes(int pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String column) {
    throw new UnsupportedOperationException();
  }

  public OffsetDateTime[] getArrayOfOffsetDateTimes(int pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getArrayOfBuffers(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getBufferArray(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getArrayOfUUIDs(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getUUIDArray(String column) {
    throw new UnsupportedOperationException();
  }

  private Byte getByte(int pos) {
    Object val = getValue(pos);
    if (val instanceof Byte) {
      return (Byte) val;
    } else if (val instanceof Number) {
      return ((Number) val).byteValue();
    }
    return null;
  }

  private Duration getDuration(int pos) {
    Object val = getValue(pos);
    if (val instanceof Duration) {
      return (Duration) val;
    }
    return null;
  }

  private Object getEnum(Class enumType, int position) {
    Object val = getValue(position);
    if (val instanceof String) {
      return Enum.valueOf(enumType, (String) val);
    } else if (val instanceof Number) {
      int ordinal = ((Number) val).intValue();
      if (ordinal >= 0) {
        Object[] constants = enumType.getEnumConstants();
        if (ordinal < constants.length) {
          return constants[ordinal];
        }
      }
    }
    return null;
  }
}
