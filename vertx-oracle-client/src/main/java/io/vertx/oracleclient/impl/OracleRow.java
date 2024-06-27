/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.oracleclient.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.RowBase;
import io.vertx.sqlclient.internal.RowDesc;

import java.time.*;
import java.util.List;
import java.util.UUID;

public class OracleRow extends RowBase {

  private final RowDesc desc;

  public OracleRow(RowDesc desc) {
    super(desc.columnNames().size());
    this.desc = desc;
  }

  public OracleRow(OracleRow row) {
    super(row);
    this.desc = row.desc;
  }

  @Override
  public String getColumnName(int pos) {
    List<String> columnNames = desc.columnNames();
    return pos < 0 || columnNames.size() - 1 < pos ? null : columnNames.get(pos);
  }

  @Override
  public int getColumnIndex(String name) {
    if (name == null) {
      throw new NullPointerException();
    }
    return desc.columnNames().indexOf(name.toUpperCase());
  }

  @Override
  public <T> T get(Class<T> type, int pos) {
    if (type == Boolean.class) {
      return type.cast(getBoolean(pos));
    } else if (type == Short.class) {
      return type.cast(getShort(pos));
    } else if (type == Integer.class) {
      return type.cast(getInteger(pos));
    } else if (type == Long.class) {
      return type.cast(getLong(pos));
    } else if (type == Float.class) {
      return type.cast(getFloat(pos));
    } else if (type == Double.class) {
      return type.cast(getDouble(pos));
    } else if (type == Character.class) {
      return type.cast(getChar(pos));
    } else if (type == Numeric.class) {
      return type.cast(getNumeric(pos));
    } else if (type == String.class) {
      return type.cast(getString(pos));
    } else if (type == Buffer.class) {
      return type.cast(getBuffer(pos));
    } else if (type == UUID.class) {
      return type.cast(getUUID(pos));
    } else if (type == LocalDate.class) {
      return type.cast(getLocalDate(pos));
    } else if (type == LocalTime.class) {
      return type.cast(getLocalTime(pos));
    } else if (type == OffsetTime.class) {
      return type.cast(getOffsetTime(pos));
    } else if (type == LocalDateTime.class) {
      return type.cast(getLocalDateTime(pos));
    } else if (type == OffsetDateTime.class) {
      return type.cast(getOffsetDateTime(pos));
    } else if (type == JsonObject.class) {
      return type.cast(getJson(pos));
    } else if (type == JsonArray.class) {
      return type.cast(getJson(pos));
    } else if (type == Object.class) {
      return type.cast(getValue(pos));
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
  }

  public Numeric getNumeric(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
  }

  public Object[] getArrayOfJsons(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getArrayOfJsons(pos);
  }

  public Numeric[] getArrayOfNumerics(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getArrayOfNumerics(pos);
  }

  public Character[] getArrayOfChars(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getArrayOfChars(pos);
  }

  public Long getLong(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof String) {
      return Long.valueOf((String) val);
    } else if (val instanceof Long) {
      return (Long) val;
    } else if (val instanceof Number) {
      return ((Number) val).longValue();
    } else if (val instanceof Enum<?>) {
      return (long) ((Enum<?>) val).ordinal();
    } else {
      return (Long) val; // Throw CCE
    }
  }

  public Character getChar(int pos) {
    Object val = getValue(pos);
    if (val instanceof Character) {
      return (Character) val;
    } else {
      return null;
    }
  }

  public Numeric getNumeric(int pos) {
    Object val = getValue(pos);
    if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
    }
    return null;
  }

  /**
   * Get a {@link JsonObject} or {@link JsonArray} value.
   */
  public Object getJson(int pos) {
    Object val = getValue(pos);
    if (val instanceof JsonObject) {
      return val;
    } else if (val instanceof JsonArray) {
      return val;
    } else {
      return null;
    }
  }

  public Character[] getArrayOfChars(int pos) {
    Object val = getValue(pos);
    if (val instanceof Character[]) {
      return (Character[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get a {@code Json} array value, the {@code Json} value may be a string, number, JSON object, array, boolean or null.
   */
  public Object[] getArrayOfJsons(int pos) {
    Object val = getValue(pos);
    if (val instanceof Object[]) {
      return (Object[]) val;
    } else {
      return null;
    }
  }

  public Numeric[] getArrayOfNumerics(int pos) {
    Object val = getValue(pos);
    if (val instanceof Numeric[]) {
      return (Numeric[]) val;
    } else {
      return null;
    }
  }
}
