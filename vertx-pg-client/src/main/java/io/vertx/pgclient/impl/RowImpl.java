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
    } else if (type == Interval.class) {
      return type.cast(getInterval(pos));
    } else if (type == Point.class) {
      return type.cast(getPoint(pos));
    } else if (type == Line.class) {
      return type.cast(getLine(pos));
    } else if (type == LineSegment.class) {
      return type.cast(getLineSegment(pos));
    } else if (type == Path.class) {
      return type.cast(getPath(pos));
    } else if (type == Polygon.class) {
      return type.cast(getPolygon(pos));
    } else if (type == Circle.class) {
      return type.cast(getCircle(pos));
    } else if (type == Box.class) {
      return type.cast(getBox(pos));
    } else if (type == JsonObject.class) {
      return type.cast(getJson(pos));
    } else if (type == JsonArray.class) {
      return type.cast(getJson(pos));
    } else if (type == Object.class) {
      return type.cast(getValue(pos));
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
  }

  @Override
  public <T> T[] getValues(Class<T> type, int pos) {
    if (type == Boolean.class) {
      return (T[]) getBooleanArray(pos);
    } else if (type == Short.class) {
      return (T[]) getShortArray(pos);
    } else if (type == Integer.class) {
      return (T[]) getIntegerArray(pos);
    } else if (type == Long.class) {
      return (T[]) getLongArray(pos);
    } else if (type == Float.class) {
      return (T[]) getFloatArray(pos);
    } else if (type == Double.class) {
      return (T[]) getDoubleArray(pos);
    } else if (type == Character.class) {
      return (T[]) getCharArray(pos);
    } else if (type == String.class) {
      return (T[]) getStringArray(pos);
    } else if (type == Buffer.class) {
      return (T[]) getBufferArray(pos);
    } else if (type == UUID.class) {
      return (T[]) getUUIDArray(pos);
    } else if (type == LocalDate.class) {
      return (T[]) getLocalDateArray(pos);
    } else if (type == LocalTime.class) {
      return (T[]) getLocalTimeArray(pos);
    } else if (type == OffsetTime.class) {
      return (T[]) getOffsetTimeArray(pos);
    } else if (type == LocalDateTime.class) {
      return (T[]) getLocalDateTimeArray(pos);
    } else if (type == OffsetDateTime.class) {
      return (T[]) getOffsetDateTimeArray(pos);
    } else if (type == Interval.class) {
      return (T[]) getIntervalArray(pos);
    } else if (type == Numeric.class) {
      return (T[]) getNumericArray(pos);
    } else if (type == Point.class) {
      return (T[]) getPointArray(pos);
    } else if (type == Line.class) {
      return (T[]) getLineArray(pos);
    } else if (type == LineSegment.class) {
      return (T[]) getLineSegmentArray(pos);
    } else if (type == Path.class) {
      return (T[]) getPathArray(pos);
    } else if (type == Polygon.class) {
      return (T[]) getPolygonArray(pos);
    } else if (type == Circle.class) {
      return (T[]) getCircleArray(pos);
    } else if (type == Interval.class) {
      return (T[]) getIntervalArray(pos);
    } else if (type == Box.class) {
      return (T[]) getBoxArray(pos);
    } else if (type == Object.class) {
      return (T[]) getJsonArray(pos);
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
  }

  public Point getPoint(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPoint(pos);
  }

  public Line getLine(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLine(pos);
  }

  public LineSegment getLineSegment(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineSegment(pos);
  }

  public Box getBox(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBox(pos);
  }

  public Path getPath(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPath(pos);
  }

  public Polygon getPolygon(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPolygon(pos);
  }

  public Circle getCircle(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCircle(pos);
  }

  public Interval getInterval(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getInterval(pos);
  }

  public Object[] getJsonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJsonArray(pos);
  }

  public Point[] getPointArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPointArray(pos);
  }

  public Line[] getLineArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineArray(pos);
  }

  public LineSegment[] getLineSegmentArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineSegmentArray(pos);
  }

  public Box[] getBoxArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBoxArray(pos);
  }

  public Path[] getPathArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPathArray(pos);
  }

  public Polygon[] getPolygonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPolygonArray(pos);
  }

  public Circle[] getCircleArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCircleArray(pos);
  }

  public Interval[] getIntervalArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getIntervalArray(pos);
  }

  public Character[] getCharArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCharArray(pos);
  }

  public Character getChar(int pos) {
    Object val = getValue(pos);
    if (val instanceof Character) {
      return (Character) val;
    } else {
      return null;
    }
  }

  private Point getPoint(int pos) {
    return (Point) getValue(pos);
  }

  private Line getLine(int pos) {
    return (Line) getValue(pos);
  }

  private LineSegment getLineSegment(int pos) {
    return (LineSegment) getValue(pos);
  }

  private Box getBox(int pos) {
    return (Box) getValue(pos);
  }

  private Path getPath(int pos) {
    return (Path) getValue(pos);
  }

  private Polygon getPolygon(int pos) {
    return (Polygon) getValue(pos);
  }

  private Circle getCircle(int pos) {
    return (Circle) getValue(pos);
  }

  private Interval getInterval(int pos) {
    return (Interval) getValue(pos);
  }

  public Character[] getCharArray(int pos) {
    return (Character[]) getValue(pos);
  }

  /**
   * Get a {@code Json} array value, the {@code Json} value may be a string, number, JSON object, array, boolean or null.
   */
  private Object[] getJsonArray(int pos) {
    return (Object[]) getValue(pos);
  }

  private Point[] getPointArray(int pos) {
    return (Point[]) getValue(pos);
  }

  private Line[] getLineArray(int pos) {
    return (Line[]) getValue(pos);
  }

  private LineSegment[] getLineSegmentArray(int pos) {
    return (LineSegment[]) getValue(pos);
  }

  private Box[] getBoxArray(int pos) {
    return (Box[]) getValue(pos);
  }

  private Path[] getPathArray(int pos) {
    return (Path[]) getValue(pos);
  }

  private Polygon[] getPolygonArray(int pos) {
    return (Polygon[]) getValue(pos);
  }

  private Circle[] getCircleArray(int pos) {
    return (Circle[]) getValue(pos);
  }

  private Interval[] getIntervalArray(int pos) {
    return (Interval[]) getValue(pos);
  }
}
