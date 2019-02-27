package io.reactiverse.pgclient.impl.my;

import io.reactiverse.pgclient.impl.my.protocol.backend.ColumnDefinition;
import io.reactiverse.pgclient.data.Box;
import io.reactiverse.pgclient.data.Circle;
import io.reactiverse.pgclient.data.Interval;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Line;
import io.reactiverse.pgclient.data.LineSegment;
import io.reactiverse.pgclient.data.Numeric;
import io.reactiverse.pgclient.data.Path;
import io.reactiverse.pgclient.data.Point;
import io.reactiverse.pgclient.data.Polygon;
import io.reactiverse.pgclient.impl.ArrayTuple;
import io.reactiverse.pgclient.impl.RowInternal;
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
  public String getColumnName(int pos) {
    final ColumnDefinition[] columnDefinitions = columnMetadata.getColumnDefinitions();
    return pos < 0 || columnDefinitions.length - 1 < pos ? null : columnDefinitions[pos].getName();
  }

  @Override
  public Boolean getBoolean(String name) {
    int pos = columnMetadata.columnIndex(name);
    return pos == -1 ? null : getBoolean(pos);
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
  public Json getJson(String name) {
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
  public Numeric getNumeric(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point getPoint(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Line getLine(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LineSegment getLineSegment(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Box getBox(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path getPath(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Polygon getPolygon(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Circle getCircle(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval getInterval(String name) {
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
  public Json[] getJsonArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Numeric[] getNumericArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Point[] getPointArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Line[] getLineArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public LineSegment[] getLineSegmentArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Box[] getBoxArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Path[] getPathArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Polygon[] getPolygonArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Circle[] getCircleArray(String name) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Interval[] getIntervalArray(String name) {
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
