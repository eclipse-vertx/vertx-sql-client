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

import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Json;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.pgclient.data.Numeric;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Polygon;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Point;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.RowDesc;
import io.vertx.sqlclient.impl.RowInternal;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.List;
import java.util.UUID;

public class RowImpl extends ArrayTuple implements RowInternal {

  // Linked list
  private RowInternal next;
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
    if (type == Json.class) {
      return type.cast(getJson(pos));
    } else if (type == Numeric.class) {
      return type.cast(getNumeric(pos));
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
    } else if (type == Interval.class) {
      return type.cast(getInterval(pos));
    } else if (type == Box.class) {
      return type.cast(getBox(pos));
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
  }

  @Override
  public <T> T[] getValues(Class<T> type, int pos) {
    if (type == Json.class) {
      return (T[]) getJsonArray(pos);
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
    }
    throw new UnsupportedOperationException("Unsupported type " + type.getName());
  }

  @Override
  public Boolean getBoolean(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBoolean(pos);
  }

  @Override
  public Object getValue(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getValue(pos);
  }

  @Override
  public Short getShort(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getShort(pos);
  }

  @Override
  public Integer getInteger(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getInteger(pos);
  }

  @Override
  public Long getLong(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLong(pos);
  }

  @Override
  public Float getFloat(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getFloat(pos);
  }

  @Override
  public Double getDouble(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getDouble(pos);
  }

  @Override
  public String getString(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getString(pos);
  }

  public Json getJson(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJson(pos);
  }

  @Override
  public Buffer getBuffer(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBuffer(pos);
  }

  @Override
  public Temporal getTemporal(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getTemporal(pos);
  }

  @Override
  public LocalDate getLocalDate(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDate(pos);
  }

  @Override
  public LocalTime getLocalTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalTime(pos);
  }

  @Override
  public LocalDateTime getLocalDateTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDateTime(pos);
  }

  @Override
  public OffsetTime getOffsetTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetTime(pos);
  }

  @Override
  public OffsetDateTime getOffsetDateTime(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetDateTime(pos);
  }

  @Override
  public UUID getUUID(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getUUID(pos);
  }

  @Override
  public BigDecimal getBigDecimal(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBigDecimal(pos);
  }

  public Numeric getNumeric(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
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

  @Override
  public Boolean[] getBooleanArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBooleanArray(pos);
  }

  @Override
  public Short[] getShortArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getShortArray(pos);
  }

  @Override
  public Integer[] getIntegerArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getIntegerArray(pos);
  }

  @Override
  public Long[] getLongArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLongArray(pos);
  }

  @Override
  public Float[] getFloatArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getFloatArray(pos);
  }

  @Override
  public Double[] getDoubleArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getDoubleArray(pos);
  }

  @Override
  public String[] getStringArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getStringArray(pos);
  }

  @Override
  public LocalDate[] getLocalDateArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDateArray(pos);
  }

  @Override
  public LocalTime[] getLocalTimeArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalTimeArray(pos);
  }

  @Override
  public OffsetTime[] getOffsetTimeArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetTimeArray(pos);
  }

  @Override
  public LocalDateTime[] getLocalDateTimeArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLocalDateTimeArray(pos);
  }

  @Override
  public OffsetDateTime[] getOffsetDateTimeArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getOffsetDateTimeArray(pos);
  }

  @Override
  public Buffer[] getBufferArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBufferArray(pos);
  }

  @Override
  public UUID[] getUUIDArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getUUIDArray(pos);
  }

  public Json[] getJsonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJsonArray(pos);
  }

  public Numeric[] getNumericArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumericArray(pos);
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

  public Numeric getNumeric(int pos) {
    Object val = get(pos);
    if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
    }
    return null;
  }

  public Point getPoint(int pos) {
    Object val = get(pos);
    if (val instanceof Point) {
      return (Point) val;
    } else {
      return null;
    }
  }

  public Line getLine(int pos) {
    Object val = get(pos);
    if (val instanceof Line) {
      return (Line) val;
    } else {
      return null;
    }
  }

  public LineSegment getLineSegment(int pos) {
    Object val = get(pos);
    if (val instanceof LineSegment) {
      return (LineSegment) val;
    } else {
      return null;
    }
  }

  public Box getBox(int pos) {
    Object val = get(pos);
    if (val instanceof Box) {
      return (Box) val;
    } else {
      return null;
    }
  }

  public Path getPath(int pos) {
    Object val = get(pos);
    if (val instanceof Path) {
      return (Path) val;
    } else {
      return null;
    }
  }

  public Polygon getPolygon(int pos) {
    Object val = get(pos);
    if (val instanceof Polygon) {
      return (Polygon) val;
    } else {
      return null;
    }
  }

  public Circle getCircle(int pos) {
    Object val = get(pos);
    if (val instanceof Circle) {
      return (Circle) val;
    } else {
      return null;
    }
  }

  public Interval getInterval(int pos) {
    Object val = get(pos);
    if (val instanceof Interval) {
      return (Interval) val;
    } else {
      return null;
    }
  }

  public Json[] getJsonArray(int pos) {
    Object val = get(pos);
    if (val instanceof Json[]) {
      return (Json[]) val;
    } else {
      return null;
    }
  }

  public Numeric[] getNumericArray(int pos) {
    Object val = get(pos);
    if (val instanceof Numeric[]) {
      return (Numeric[]) val;
    } else {
      return null;
    }
  }

  public Point[] getPointArray(int pos) {
    Object val = get(pos);
    if (val instanceof Point[]) {
      return (Point[]) val;
    } else {
      return null;
    }
  }

  public Line[] getLineArray(int pos) {
    Object val = get(pos);
    if (val instanceof Line[]) {
      return (Line[]) val;
    } else {
      return null;
    }
  }

  public LineSegment[] getLineSegmentArray(int pos) {
    Object val = get(pos);
    if (val instanceof LineSegment[]) {
      return (LineSegment[]) val;
    } else {
      return null;
    }
  }

  public Box[] getBoxArray(int pos) {
    Object val = get(pos);
    if (val instanceof Box[]) {
      return (Box[]) val;
    } else {
      return null;
    }
  }

  public Path[] getPathArray(int pos) {
    Object val = get(pos);
    if (val instanceof Path[]) {
      return (Path[]) val;
    } else {
      return null;
    }
  }

  public Polygon[] getPolygonArray(int pos) {
    Object val = get(pos);
    if (val instanceof Polygon[]) {
      return (Polygon[]) val;
    } else {
      return null;
    }
  }

  public Circle[] getCircleArray(int pos) {
    Object val = get(pos);
    if (val instanceof Circle[]) {
      return (Circle[]) val;
    } else {
      return null;
    }
  }

  public Interval[] getIntervalArray(int pos) {
    Object val = get(pos);
    if (val instanceof Interval[]) {
      return (Interval[]) val;
    } else {
      return null;
    }
  }

  public Json getJson(int pos) {
    Object val = get(pos);
    if (val instanceof Json) {
      return (Json) val;
    }
    return null;
  }

  @Override
  public void setNext(RowInternal next) {
    this.next = next;
  }

  @Override
  public RowInternal getNext() {
    return next;
  }
}
