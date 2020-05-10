/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */

package io.vertx.pgclient.impl;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Polygon;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Point;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.core.buffer.Buffer;

import java.lang.reflect.Array;
import java.time.*;
import java.util.List;
import java.util.UUID;

public class RowImpl extends ArrayTuple implements Row {

  private final RowDesc desc;

  public RowImpl(RowDesc desc) {
    super(desc.columnNames().size());
    this.desc = desc;
  }

  public RowImpl(RowImpl row) {
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
    return desc.columnNames().indexOf(name);
  }

  @Override
  public <T> T get(Class<T> type, int position) {
    if (type.isArray()) {
      Class<?> componentType = type.getComponentType();
      if (componentType == Boolean.class) {
        return type.cast(getBooleanArray(position));
      } else if (componentType == Short.class) {
        return type.cast(getShortArray(position));
      } else if (componentType == Integer.class) {
        return type.cast(getIntegerArray(position));
      } else if (componentType == Long.class) {
        return type.cast(getLongArray(position));
      } else if (componentType == Float.class) {
        return type.cast(getFloatArray(position));
      } else if (componentType == Double.class) {
        return type.cast(getDoubleArray(position));
      } else if (componentType == String.class) {
        return type.cast(getStringArray(position));
      } else if (componentType == Buffer.class) {
        return type.cast(getBufferArray(position));
      } else if (componentType == UUID.class) {
        return type.cast(getUUIDArray(position));
      } else if (componentType == LocalDate.class) {
        return type.cast(getLocalDateArray(position));
      } else if (componentType == LocalTime.class) {
        return type.cast(getLocalTimeArray(position));
      } else if (componentType == OffsetTime.class) {
        return type.cast(getOffsetTimeArray(position));
      } else if (componentType == LocalDateTime.class) {
        return type.cast(getLocalDateTimeArray(position));
      } else if (componentType == OffsetDateTime.class) {
        return type.cast(getOffsetDateTimeArray(position));
      } else if (componentType == Interval.class) {
        return type.cast(getIntervalArray(position));
      } else if (componentType == Numeric.class) {
        return type.cast(getNumericArray(position));
      } else if (componentType == Point.class) {
        return type.cast(getPointArray(position));
      } else if (componentType == Line.class) {
        return type.cast(getLineArray(position));
      } else if (componentType == LineSegment.class) {
        return type.cast(getLineSegmentArray(position));
      } else if (componentType == Path.class) {
        return type.cast(getPathArray(position));
      } else if (componentType == Polygon.class) {
        return type.cast(getPolygonArray(position));
      } else if (componentType == Circle.class) {
        return type.cast(getCircleArray(position));
      } else if (componentType == Interval.class) {
        return type.cast(getIntervalArray(position));
      } else if (componentType == Box.class) {
        return type.cast(getBoxArray(position));
      } else if (componentType == Object.class) {
        return type.cast(getJsonArray_(position));
      } else if (componentType.isEnum()) {
        return type.cast(getEnumArray(componentType, position));
      }
    } else {
      if (type == Boolean.class) {
        return type.cast(getBoolean(position));
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
      } else if (type == UUID.class) {
        return type.cast(getUUID(position));
      } else if (type == LocalDate.class) {
        return type.cast(getLocalDate(position));
      } else if (type == LocalTime.class) {
        return type.cast(getLocalTime(position));
      } else if (type == OffsetTime.class) {
        return type.cast(getOffsetTime(position));
      } else if (type == LocalDateTime.class) {
        return type.cast(getLocalDateTime(position));
      } else if (type == OffsetDateTime.class) {
        return type.cast(getOffsetDateTime(position));
      } else if (type == Interval.class) {
        return type.cast(getInterval(position));
      } else if (type == Point.class) {
        return type.cast(getPoint(position));
      } else if (type == Line.class) {
        return type.cast(getLine(position));
      } else if (type == LineSegment.class) {
        return type.cast(getLineSegment(position));
      } else if (type == Path.class) {
        return type.cast(getPath(position));
      } else if (type == Polygon.class) {
        return type.cast(getPolygon(position));
      } else if (type == Circle.class) {
        return type.cast(getCircle(position));
      } else if (type == Box.class) {
        return type.cast(getBox(position));
      } else if (type == JsonObject.class) {
        return type.cast(getJsonElement(position));
      } else if (type == JsonArray.class) {
        return type.cast(getJsonElement(position));
      } else if (type == Object.class) {
        return type.cast(getValue(position));
      } else if (type.isEnum()) {
        return type.cast(getEnum(type, position));
      }
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
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

  /**
   * Get a {@link io.vertx.core.json.JsonObject} or {@link io.vertx.core.json.JsonArray} value.
   */
  private Object getJsonElement(int pos) {
    Object val = getValue(pos);
    if (val instanceof JsonObject) {
      return val;
    } else if (val instanceof JsonArray) {
      return val;
    } else {
      return null;
    }
  }

  private Point getPoint(int pos) {
    Object val = getValue(pos);
    if (val instanceof Point) {
      return (Point) val;
    } else {
      return null;
    }
  }

  private Line getLine(int pos) {
    Object val = getValue(pos);
    if (val instanceof Line) {
      return (Line) val;
    } else {
      return null;
    }
  }

  private LineSegment getLineSegment(int pos) {
    Object val = getValue(pos);
    if (val instanceof LineSegment) {
      return (LineSegment) val;
    } else {
      return null;
    }
  }

  private Box getBox(int pos) {
    Object val = getValue(pos);
    if (val instanceof Box) {
      return (Box) val;
    } else {
      return null;
    }
  }

  private Path getPath(int pos) {
    Object val = getValue(pos);
    if (val instanceof Path) {
      return (Path) val;
    } else {
      return null;
    }
  }

  private Polygon getPolygon(int pos) {
    Object val = getValue(pos);
    if (val instanceof Polygon) {
      return (Polygon) val;
    } else {
      return null;
    }
  }

  private Circle getCircle(int pos) {
    Object val = getValue(pos);
    if (val instanceof Circle) {
      return (Circle) val;
    } else {
      return null;
    }
  }

  private Interval getInterval(int pos) {
    Object val = getValue(pos);
    if (val instanceof Interval) {
      return (Interval) val;
    } else {
      return null;
    }
  }

  private Object getEnum(Class enumType, int pos) {
    Object val = getValue(pos);
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

  /**
   * Get a {@code Json} array value, the {@code Json} value may be a string, number, JSON object, array, boolean or null.
   */
  private Object[] getJsonArray_(int pos) {
    Object val = getValue(pos);
    if (val instanceof Object[]) {
      return (Object[]) val;
    } else {
      return null;
    }
  }

  private Numeric[] getNumericArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Numeric[]) {
      return (Numeric[]) val;
    } else {
      return null;
    }
  }

  private Point[] getPointArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Point[]) {
      return (Point[]) val;
    } else {
      return null;
    }
  }

  private Line[] getLineArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Line[]) {
      return (Line[]) val;
    } else {
      return null;
    }
  }

  private LineSegment[] getLineSegmentArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof LineSegment[]) {
      return (LineSegment[]) val;
    } else {
      return null;
    }
  }

  private Box[] getBoxArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Box[]) {
      return (Box[]) val;
    } else {
      return null;
    }
  }

  private Path[] getPathArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Path[]) {
      return (Path[]) val;
    } else {
      return null;
    }
  }

  private Polygon[] getPolygonArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Polygon[]) {
      return (Polygon[]) val;
    } else {
      return null;
    }
  }

  private Circle[] getCircleArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Circle[]) {
      return (Circle[]) val;
    } else {
      return null;
    }
  }

  private Interval[] getIntervalArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Interval[]) {
      return (Interval[]) val;
    } else {
      return null;
    }
  }

  private Object[] getEnumArray(Class enumType, int pos) {
    Object val = getValue(pos);
    if (val instanceof String[]) {
      String[] array = (String[]) val;
      Object[] ret = (Object[]) Array.newInstance(enumType, array.length);
      for (int i = 0;i < array.length;i++) {
        String string = array[i];
        if (string != null) {
          ret[i] = Enum.valueOf(enumType, string);
        }
      }
      return ret;
    } else if (val instanceof Number[]) {
      Number[] array = (Number[]) val;
      Object[] ret = (Object[]) Array.newInstance(enumType, array.length);
      Object[] constants = enumType.getEnumConstants();
      for (int i = 0;i < array.length;i++) {
        Number number = array[i];
        int ordinal = number.intValue();
        if (ordinal >= 0) {
          if (ordinal < constants.length) {
            ret[i] = constants[ordinal];
          }
        }
      }
      return ret;
    }
    return null;
  }}
