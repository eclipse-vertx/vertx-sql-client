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
package io.reactiverse.pgclient;

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
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

@VertxGen
public interface Row extends Tuple {
  /**
   * Get a column name at {@code pos}.
   *
   * @param pos the position
   * @return the column name or {@code null}
   */
  String getColumnName(int pos);

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Boolean getBoolean(String name);

  /**
   * Get an object value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Object getValue(String name);

  /**
   * Get a short value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Short getShort(String name);

  /**
   * Get an integer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Integer getInteger(String name);

  /**
   * Get a long value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Long getLong(String name);

  /**
   * Get a float value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Float getFloat(String name);

  /**
   * Get a double value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Double getDouble(String name);

  /**
   * Get a string value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  String getString(String name);

  /**
   * Get a json value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Json getJson(String name);

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Buffer getBuffer(String name);

  /**
   * Get a temporal value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Temporal getTemporal(String name);

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDate getLocalDate(String name);

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalTime getLocalTime(String name);

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDateTime getLocalDateTime(String name);

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetTime getOffsetTime(String name);

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetDateTime getOffsetDateTime(String name);

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  UUID getUUID(String name);

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  BigDecimal getBigDecimal(String name);

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Numeric getNumeric(String name);

  /**
   * Get {@link Point} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Point getPoint(String name);

  /**
   * Get {@link Line} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Line getLine(String name);

  /**
   * Get {@link LineSegment} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  LineSegment getLineSegment(String name);

  /**
   * Get {@link Box} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Box getBox(String name);

  /**
   * Get {@link Path} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Path getPath(String name);

  /**
   * Get {@link Polygon} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Polygon getPolygon(String name);

  /**
   * Get {@link Circle} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Circle getCircle(String name);

  /**
   * Get {@link Interval} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  Interval getInterval(String name);

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Integer[] getIntegerArray(String name);

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Boolean[] getBooleanArray(String name);

  /**
   * Get an array of {@link Short} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Short[] getShortArray(String name);

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Long[] getLongArray(String name);

  /**
   * Get an array of {@link Float} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Float[] getFloatArray(String name);

  /**
   * Get an array of {@link Double} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Double[] getDoubleArray(String name);

  /**
   * Get an array of {@link String} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  String[] getStringArray(String name);

  /**
   * Get an array of {@link LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDate[] getLocalDateArray(String name);

  /**
   * Get an array of {@link LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalTime[] getLocalTimeArray(String name);

  /**
   * Get an array of {@link OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetTime[] getOffsetTimeArray(String name);

  /**
   * Get an array of {@link LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDateTime[] getLocalDateTimeArray(String name);

  /**
   * Get an array of {@link OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetDateTime[] getOffsetDateTimeArray(String name);

  /**
   * Get an array of {@link Buffer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Buffer[] getBufferArray(String name);

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  UUID[] getUUIDArray(String name);

  /**
   * Get an array of {@link Json} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  Json[] getJsonArray(String name);

  /**
   * Get an array of {@link Numeric} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Numeric[] getNumericArray(String name);

  /**
   * Get an array of {@link Point} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Point[] getPointArray(String name);

  /**
   * Get an array of {@link Line} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Line[] getLineArray(String name);

  /**
   * Get an array of {@link LineSegment} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LineSegment[] getLineSegmentArray(String name);

  /**
   * Get an array of {@link Box} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Box[] getBoxArray(String name);

  /**
   * Get an array of {@link Path} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Path[] getPathArray(String name);

  /**
   * Get an array of {@link Polygon} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Polygon[] getPolygonArray(String name);

  /**
   * Get an array of {@link Circle} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Circle[] getCircleArray(String name);

  /**
   * Get an array of {@link Interval} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Interval[] getIntervalArray(String name);
}
