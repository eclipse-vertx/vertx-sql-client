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

package io.vertx.mysqlclient.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.mysqlclient.data.spatial.*;
import io.vertx.mysqlclient.impl.datatype.DataType;
import io.vertx.mysqlclient.impl.protocol.ColumnDefinition;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.core.buffer.Buffer;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;

public class MySQLRowImpl extends ArrayTuple implements Row {

  private final MySQLRowDesc rowDesc;

  public MySQLRowImpl(MySQLRowDesc rowDesc) {
    super(rowDesc.columnNames().size());
    this.rowDesc = rowDesc;
  }

  @Override
  public <T> T get(Class<T> type, int pos) {
    if (type == Boolean.class) {
      return type.cast(getBoolean(pos));
    } else if (type == Byte.class) {
      return type.cast(getByte(pos));
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
    } else if (type == Numeric.class) {
      return type.cast(getNumeric(pos));
    } else if (type == String.class) {
      return type.cast(getString(pos));
    } else if (type == Buffer.class) {
      return type.cast(getBuffer(pos));
    } else if (type == LocalDate.class) {
      return type.cast(getLocalDate(pos));
    } else if (type == LocalDateTime.class) {
      return type.cast(getLocalDateTime(pos));
    } else if (type == Duration.class) {
      return type.cast(getDuration(pos));
    } else if (type == JsonObject.class) {
      return type.cast(getJsonObject(pos));
    } else if (type == JsonArray.class) {
      return type.cast(getJsonArray(pos));
    } else if (type == Geometry.class) {
      return type.cast(getGeometry(pos));
    } else if (type == Point.class) {
      return type.cast(getPoint(pos));
    } else if (type == LineString.class) {
      return type.cast(getLineString(pos));
    } else if (type == Polygon.class) {
      return type.cast(getPolygon(pos));
    } else if (type == MultiPoint.class) {
      return type.cast(getMultiPoint(pos));
    } else if (type == MultiLineString.class) {
      return type.cast(getMultiLineString(pos));
    } else if (type == MultiPolygon.class) {
      return type.cast(getMultiPolygon(pos));
    } else if (type == GeometryCollection.class) {
      return type.cast(getGeometryCollection(pos));
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getName());
    }
  }

  @Override
  public <T> T[] getValues(Class<T> type, int idx) {
    throw new UnsupportedOperationException("MySQL Array data type is not supported");
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
    return rowDesc.columnNames().indexOf(name);
  }

  public Numeric getNumeric(String name) {
    int pos = rowDesc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
  }


  @Override
  public Temporal getTemporal(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LocalTime getLocalTime(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalTime(pos);
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, OffsetTime.class));
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, OffsetDateTime.class));
  }

  @Override
  public UUID getUUID(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, UUID.class));
  }

  @Override
  public Integer[] getIntegerArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Integer[].class));
  }

  @Override
  public Boolean[] getBooleanArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Boolean[].class));
  }

  @Override
  public Short[] getShortArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Short[].class));
  }

  @Override
  public Long[] getLongArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Long[].class));
  }

  @Override
  public Float[] getFloatArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Float[].class));
  }

  @Override
  public Double[] getDoubleArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Double[].class));
  }

  @Override
  public String[] getStringArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, String[].class));
  }

  @Override
  public LocalDate[] getLocalDateArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, LocalDate[].class));
  }

  @Override
  public LocalTime[] getLocalTimeArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, LocalTime[].class));
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, OffsetTime[].class));
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, LocalDateTime[].class));
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, OffsetDateTime[].class));
  }

  @Override
  public Buffer[] getBufferArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, Buffer[].class));
  }

  @Override
  public UUID[] getUUIDArray(String name) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(name), name, UUID[].class));
  }

  @Override
  public Boolean getBoolean(int pos) {
    // in MySQL BOOLEAN type is mapped to TINYINT
    Object val = getValue(pos);
    if (val instanceof Boolean) {
      return (Boolean) val;
    } else if (val instanceof Number) {
      return ((Number) val).byteValue() != 0;
    }
    return null;
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

  private Geometry getGeometry(int pos) {
    Object val = getValue(pos);
    if (val instanceof Geometry) {
      return (Geometry) val;
    }
    return null;
  }

  private Point getPoint(int pos) {
    Object val = getValue(pos);
    if (val instanceof Point) {
      return (Point) val;
    }
    return null;
  }

  private LineString getLineString(int pos) {
    Object val = getValue(pos);
    if (val instanceof LineString) {
      return (LineString) val;
    }
    return null;
  }

  private Polygon getPolygon(int pos) {
    Object val = getValue(pos);
    if (val instanceof Polygon) {
      return (Polygon) val;
    }
    return null;
  }

  private MultiPoint getMultiPoint(int pos) {
    Object val = getValue(pos);
    if (val instanceof MultiPoint) {
      return (MultiPoint) val;
    }
    return null;
  }

  private MultiLineString getMultiLineString(int pos) {
    Object val = getValue(pos);
    if (val instanceof MultiLineString) {
      return (MultiLineString) val;
    }
    return null;
  }

  private MultiPolygon getMultiPolygon(int pos) {
    Object val = getValue(pos);
    if (val instanceof MultiPolygon) {
      return (MultiPolygon) val;
    }
    return null;
  }

  private GeometryCollection getGeometryCollection(int pos) {
    Object val = getValue(pos);
    if (val instanceof GeometryCollection) {
      return (GeometryCollection) val;
    }
    return null;
  }

  @Override
  public LocalTime getLocalTime(int pos) {
    ColumnDefinition columnDefinition = rowDesc.columnDefinitions()[pos];
    Object val = getValue(pos);
    if (columnDefinition.type() == DataType.TIME && val instanceof Duration) {
      // map MySQL TIME data type to java.time.LocalTime
      Duration duration = (Duration) val;
      return LocalTime.ofNanoOfDay(duration.toNanos());
    } else {
      return super.getLocalTime(pos);
    }
  }

  private <T> String buildIllegalAccessMessage(Object value, String columnName, Class<T> clazz) {
    return String.format("Can not retrieve row value[%s] as class[%s], columnName=[%s]", value.toString(), clazz.getName(), columnName);
  }
}
