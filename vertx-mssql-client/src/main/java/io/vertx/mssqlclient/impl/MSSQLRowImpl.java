/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowDesc;

import java.time.*;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;

public class MSSQLRowImpl extends ArrayTuple implements Row {
  private final RowDesc rowDesc;

  public MSSQLRowImpl(RowDesc rowDesc) {
    super(rowDesc.columnNames().size());
    this.rowDesc = rowDesc;
  }

  @Override
  public String getColumnName(int pos) {
    List<String> columnNames = rowDesc.columnNames();
    return pos < 0 || columnNames.size() - 1 < pos ? null : columnNames.get(pos);
  }

  @Override
  public int getColumnIndex(String columnName) {
    if (columnName == null) {
      throw new IllegalArgumentException("Column name can not be null");
    }
    return rowDesc.columnNames().indexOf(columnName);
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
    } else if (type == LocalDate.class) {
      return type.cast(getLocalDate(position));
    } else if (type == LocalTime.class) {
      return type.cast(getLocalTime(position));
    }else if (type == Object.class) {
      return type.cast(getValue(position));
    } else if (type.isEnum()) {
      return type.cast(getEnum(type, position));
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getName());
    }
  }

  @Override
  public Buffer getBuffer(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Temporal getTemporal(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime getLocalDateTime(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime getOffsetTime(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID getUUID(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Integer[] getIntegerArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Boolean[] getBooleanArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Short[] getShortArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Long[] getLongArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Float[] getFloatArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Double[] getDoubleArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public String[] getStringArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDate[] getLocalDateArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime[] getLocalTimeArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getBufferArray(String columnName) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getUUIDArray(String columnName) {
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

  private Numeric getNumeric(int pos) {
    Object val = getValue(pos);
    if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
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
