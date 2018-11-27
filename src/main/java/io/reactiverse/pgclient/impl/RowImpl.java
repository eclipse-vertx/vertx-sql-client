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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.data.Box;
import io.reactiverse.pgclient.data.Circle;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Line;
import io.reactiverse.pgclient.data.LineSegment;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.data.Path;
import io.reactiverse.pgclient.data.Polygon;
import io.reactiverse.pgclient.impl.codec.ColumnDesc;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.impl.codec.decoder.RowDescription;
import io.reactiverse.pgclient.data.Point;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

public class RowImpl extends ArrayTuple implements Row {

  // Linked list
  RowImpl next;
  private final RowDescription desc;

  public RowImpl(RowDescription desc) {
    super(desc.columns().length);
    this.desc = desc;
  }

  public RowImpl(RowImpl row) {
    super(row);
    this.desc = row.desc;
  }

  @Override
  public String getColumnName(int pos) {
    final ColumnDesc[] columnDescs = desc.columns();
    return pos < 0 || columnDescs.length - 1 < pos ? null : columnDescs[pos].getName();
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

  @Override
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

  @Override
  public Numeric getNumeric(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumeric(pos);
  }

  @Override
  public Point getPoint(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPoint(pos);
  }

  @Override
  public Line getLine(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLine(pos);
  }

  @Override
  public LineSegment getLineSegment(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineSegment(pos);
  }

  @Override
  public Box getBox(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBox(pos);
  }

  @Override
  public Path getPath(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPath(pos);
  }

  @Override
  public Polygon getPolygon(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPolygon(pos);
  }

  @Override
  public Circle getCircle(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCircle(pos);
  }

  @Override
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

  @Override
  public Json[] getJsonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getJsonArray(pos);
  }

  @Override
  public Numeric[] getNumericArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getNumericArray(pos);
  }

  @Override
  public Point[] getPointArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPointArray(pos);
  }

  @Override
  public Line[] getLineArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineArray(pos);
  }

  @Override
  public LineSegment[] getLineSegmentArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getLineSegmentArray(pos);
  }

  @Override
  public Box[] getBoxArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getBoxArray(pos);
  }

  @Override
  public Path[] getPathArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPathArray(pos);
  }

  @Override
  public Polygon[] getPolygonArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getPolygonArray(pos);
  }

  @Override
  public Circle[] getCircleArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getCircleArray(pos);
  }

  @Override
  public Interval[] getIntervalArray(String name) {
    int pos = desc.columnIndex(name);
    return pos == -1 ? null : getIntervalArray(pos);
  }
}
