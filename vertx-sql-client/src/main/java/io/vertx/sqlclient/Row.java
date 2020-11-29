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
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.Utils;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * A single row of the {@link RowSet execution result rowset}.
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
   * Get a column position for the given @code column}.
   *
   * @param column the column name
   * @return the column name or {@code -1} if not found
   */
  int getColumnIndex(String column);

  /**
   * Get a value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Object getValue(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getValue(pos);
  }

  /**
   * Get a boolean value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Boolean getBoolean(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBoolean(pos);
  }

  /**
   * Get a short value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Short getShort(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getShort(pos);
  }

  /**
   * Get an integer value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Integer getInteger(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getInteger(pos);
  }

  /**
   * Get a long value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Long getLong(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLong(pos);
  }

  /**
   * Get a float value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Float getFloat(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getFloat(pos);
  }

  /**
   * Get a double value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Double getDouble(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getDouble(pos);
  }

  /**
   * Get {@link Numeric} value for the given {@code column}.
   *
   * @param column the column
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric getNumeric(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getNumeric(pos);
  }

  /**
   * Get a string value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default String getString(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getString(pos);
  }

  /**
   * Get a JSON value for the given {@code column}, the JSON value might be one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   *   <li>Null</li>
   * </ul>
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Object getJsonElement(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getJsonElement(pos);
  }

  /**
   * Get a {@link JsonObject} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default JsonObject getJsonObject(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getJsonObject(pos);
  }

  /**
   * Get a {@link JsonArray} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default JsonArray getJsonArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getJsonArray(pos);
  }

  /**
   * Get a temporal value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal getTemporal(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getTemporal(pos);
  }

  /**
   * Get {@link java.time.LocalDate} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate getLocalDate(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalDate(pos);
  }

  /**
   * Get {@link java.time.LocalTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime getLocalTime(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalTime(pos);
  }

  /**
   * Get {@link java.time.LocalDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalDateTime(pos);
  }

  /**
   * Get {@link java.time.OffsetTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime getOffsetTime(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getOffsetTime(pos);
  }

  /**
   * Get {@link java.time.OffsetDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime getOffsetDateTime(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getOffsetDateTime(pos);
  }

  /**
   * Get a buffer value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Buffer getBuffer(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBuffer(pos);
  }

  /**
   * Get {@link java.util.UUID} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID getUUID(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getUUID(pos);
  }

  /**
   * Get {@link BigDecimal} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal getBigDecimal(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBigDecimal(pos);
  }

  /**
   * Get an array of {@link Boolean} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getArrayOfBooleans(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfBooleans(pos);
  }

  /**
   * Get an array of {@link Short} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getArrayOfShorts(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfShorts(pos);
  }

  /**
   * Get an array of {@link Integer} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getArrayOfIntegers(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfIntegers(pos);
  }

  /**
   * Get an array of {@link Long} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getArrayOfLongs(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfLongs(pos);
  }

  /**
   * Get an array of {@link Float} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getArrayOfFloats(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfFloats(pos);
  }

  /**
   * Get an array of {@link Double} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getArrayOfDoubles(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfDoubles(pos);
  }

  /**
   * Get an array of {@link Numeric} value for the given {@code column}.
   *
   * @param column the column
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getArrayOfNumerics(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfNumerics(pos);
  }

  /**
   * Get an array of {@link String} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getArrayOfStrings(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfStrings(pos);
  }

  /**
   * Get an array of {@link JsonObject} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonObject[] getArrayOfJsonObjects(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfJsonObjects(pos);
  }

  /**
   * Get an array of {@link JsonArray} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonArray[] getArrayOfJsonArrays(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfJsonArrays(pos);
  }

  /**
   * Get an array of {@link Temporal} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getArrayOfTemporals(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfTemporals(pos);
  }

  /**
   * Get an array of {@link LocalDate} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getArrayOfLocalDates(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfLocalDates(pos);
  }

  /**
   * Get an array of {@link LocalTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getArrayOfLocalTimes(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfLocalTimes(pos);
  }

  /**
   * Get an array of {@link LocalDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getArrayOfLocalDateTimes(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfLocalDateTimes(pos);
  }

  /**
   * Get an array of {@link OffsetTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getArrayOfOffsetTimes(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfOffsetTimes(pos);
  }

  /**
   * Get an array of {@link OffsetDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getArrayOfOffsetDateTimes(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfOffsetDateTimes(pos);
  }

  /**
   * Get an array of {@link Buffer} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore
  default Buffer[] getArrayOfBuffers(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfBuffers(pos);
  }

  /**
   * Get an array of {@link UUID} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getArrayOfUUIDs(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfUUIDs(pos);
  }

  /**
   * Get an array of {@link BigDecimal} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getArrayOfBigDecimals(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfBigDecimals(pos);
  }

  /**
   * Get an array of JSON value for the given {@code column}, the JSON value might be one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   *   <li>Null</li>
   * </ul>
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Object[] getJsonElementArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getJsonElementArray(pos);
  }

  /**
   * Like {@link #get(Class, int)} but specifying the {@code column} instead of the position.
   */
  default <T> T get(Class<T> type, String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return get(type, pos);
  }

  /**
   * Return a JSON object representation of the row.
   *
   * <p>Column names are mapped to JSON keys.
   *
   * <p>The following rules are applied for the column values:
   *
   * <ul>
   *   <li>number, boolean and string are preserved</li>
   *   <li>the {@code null} value is preserved</li>
   *   <li>JSON elements are preserved</li>
   *   <li>{@code Buffer} are converted to base64 encoded strings</li>
   *   <li>array is mapped {@code JsonArray}</li>
   *   <li>otherwise the type converted to a string</li>
   * </ul>
   *
   * @return the json representation
   */
  default JsonObject toJson() {
    JsonObject json = new JsonObject();
    int size = size();
    for (int pos = 0;pos < size;pos++) {
      String name = getColumnName(pos);
      Object value = getValue(pos);
      json.put(name, Utils.toJson(value));
    }
    return json;
  }

}
