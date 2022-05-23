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
    } else if (type == Duration.class) {
      return type.cast(getDuration(position));
    } else if (type == JsonObject.class) {
      return type.cast(getJsonObject(position));
    } else if (type == JsonArray.class) {
      return type.cast(getJsonArray(position));
    } else if (type == Geometry.class) {
      return type.cast(getGeometry(position));
    } else if (type == Point.class) {
      return type.cast(getPoint(position));
    } else if (type == LineString.class) {
      return type.cast(getLineString(position));
    } else if (type == Polygon.class) {
      return type.cast(getPolygon(position));
    } else if (type == MultiPoint.class) {
      return type.cast(getMultiPoint(position));
    } else if (type == MultiLineString.class) {
      return type.cast(getMultiLineString(position));
    } else if (type == MultiPolygon.class) {
      return type.cast(getMultiPolygon(position));
    } else if (type == GeometryCollection.class) {
      return type.cast(getGeometryCollection(position));
    } else if (type.isEnum()) {
      return type.cast(getEnum(type, position));
    } else {
      throw new UnsupportedOperationException("Unsupported type " + type.getName());
    }
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

  @Override
  public Temporal getTemporal(int pos) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(pos), getColumnName(pos), Temporal.class));
  }

  @Override
  public OffsetTime getOffsetTime(int pos) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(pos), getColumnName(pos), OffsetTime.class));
  }

  @Override
  public OffsetDateTime getOffsetDateTime(int pos) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(pos), getColumnName(pos), OffsetDateTime.class));
  }

  @Override
  public UUID getUUID(int pos) {
    throw new UnsupportedOperationException(buildIllegalAccessMessage(getValue(pos), getColumnName(pos), UUID.class));
  }

  @Override
  public LocalDateTime[] getArrayOfLocalDateTimes(int pos) {
    throw new UnsupportedOperationException();
  }

  public OffsetDateTime[] getArrayOfOffsetDateTimes(int pos) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Buffer[] getArrayOfBuffers(String column) {
    throw new UnsupportedOperationException();
  }

  @Override
  public UUID[] getArrayOfUUIDs(String column) {
    throw new UnsupportedOperationException();
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
    } else if (val == null) {
      return null;
    }
    throw new ClassCastException();
  }

  private <T> String buildIllegalAccessMessage(Object value, String columnName, Class<T> clazz) {
    return String.format("Can not retrieve row value[%s] as class[%s], columnName=[%s]", value.toString(), clazz.getName(), columnName);
  }
}
