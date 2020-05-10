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
package io.vertx.sqlclient;

import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * An object that represents a single row of the {@link RowSet execution result rowset}.
 * Users can retrieve values with the {@code getByIndex} accessor methods or {@code getByColumnName} accessor methods,
 * it's usually more efficient to use the former one because a column index lookup by could be saved.
 */
@VertxGen
public interface Row extends Tuple {

  /**
   * Get a column name at {@code pos}.
   *
   * @param pos the column position
   * @return the column name or {@code null}
   */
  String getColumnName(int pos);

  /**
   * Get a column position for the given column {@code name}.
   *
   * @param name the column name
   * @return the column name or {@code -1} if not found
   */
  int getColumnIndex(String name);

  /**
   * Get an object value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Object getValue(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getValue(pos);
  }

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Boolean getBoolean(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBoolean(pos);
  }

  /**
   * Get a short value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Short getShort(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getShort(pos);
  }

  /**
   * Get an integer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Integer getInteger(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getInteger(pos);
  }

  /**
   * Get a long value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Long getLong(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLong(pos);
  }

  /**
   * Get a float value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Float getFloat(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getFloat(pos);
  }

  /**
   * Get a double value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Double getDouble(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getDouble(pos);
  }

  /**
   * Get a string value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default String getString(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getString(pos);
  }

  /**
   * Get a {@link JsonObject} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default JsonObject getJsonObject(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getJsonObject(pos);
  }

  /**
   * Get a {@link JsonArray} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default JsonArray getJsonArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getJsonArray(pos);
  }

  /**
   * Get a temporal value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal getTemporal(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getTemporal(pos);
  }

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate getLocalDate(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalDate(pos);
  }

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime getLocalTime(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalTime(pos);
  }

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalDateTime(pos);
  }

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime getOffsetTime(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getOffsetTime(pos);
  }

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime getOffsetDateTime(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getOffsetDateTime(pos);
  }

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  default Buffer getBuffer(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBuffer(pos);
  }

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID getUUID(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getUUID(pos);
  }

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal getBigDecimal(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBigDecimal(pos);
  }

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getBooleanArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBooleanArray(pos);
  }

  /**
   * Get an array of {@link Short} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getShortArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getShortArray(pos);
  }

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getIntegerArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getIntegerArray(pos);
  }

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getLongArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLongArray(pos);
  }

  /**
   * Get an array of {@link Float} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getFloatArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getFloatArray(pos);
  }

  /**
   * Get an array of {@link Double} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getDoubleArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getDoubleArray(pos);
  }

  /**
   * Get an array of {@link String} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getStringArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getStringArray(pos);
  }

  /**
   * Get an array of {@link JsonObject} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonObject[] getJsonObjectArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getJsonObjectArray(pos);
  }

  /**
   * Get an array of {@link JsonArray} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonArray[] getJsonArrayArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getJsonArrayArray(pos);
  }

  /**
   * Get an array of {@link Temporal} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getTemporalArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getTemporalArray(pos);
  }

  /**
   * Get an array of {@link LocalDate} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getLocalDateArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalDateArray(pos);
  }

  /**
   * Get an array of {@link LocalTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getLocalTimeArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalTimeArray(pos);
  }

  /**
   * Get an array of {@link LocalDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getLocalDateTimeArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getLocalDateTimeArray(pos);
  }

  /**
   * Get an array of {@link OffsetTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getOffsetTimeArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getOffsetTimeArray(pos);
  }

  /**
   * Get an array of {@link OffsetDateTime} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getOffsetDateTimeArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getOffsetDateTimeArray(pos);
  }

  /**
   * Get an array of {@link Buffer} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore
  default Buffer[] getBufferArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBufferArray(pos);
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getUUIDArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getUUIDArray(pos);
  }

  /**
   * Get an array of {@link BigDecimal} value at {@code pos}.
   *
   * @param name the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getBigDecimalArray(String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : getBigDecimalArray(pos);
  }

  /**
   * Like {@link #get(Class, int)} but specifying the column {@code name} instead of the position.
   */
  default <T> T get(Class<T> type, String name) {
    int pos = getColumnIndex(name);
    return pos == -1 ? null : get(type, pos);
  }
}
