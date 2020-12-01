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
import io.vertx.sqlclient.data.Numeric;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.NoSuchElementException;
import java.util.UUID;

/**
 * Represents single row of the result set.
 */
@VertxGen
public interface Row extends Tuple {

  /**
   * Get a column name for the given {@code column}.
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
   * Get an object value for the given {@code column}.
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
   * Get a numeric value for the given {@code column}.
   *
   * @param column the column name
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
   * Get a JSON element for the given {@code column}, the element might be {@link io.vertx.sqlclient.Tuple#JSON_NULL null} or one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   * </ul>
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  default Object getJson(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getJson(pos);
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
    return getBooleanArray(column);
  }

  /**
   * Get an array of {@link Boolean} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfBooleans(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getBooleanArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBooleanArray(pos);
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
    return getShortArray(column);
  }

  /**
   * Get an array of {@link Short} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfShorts(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getShortArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getShortArray(pos);
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
    return getIntegerArray(column);
  }

  /**
   * Get an array of {@link Integer} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfIntegers(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getIntegerArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getIntegerArray(pos);
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
    return getLongArray(column);
  }

  /**
   * Get an array of {@link Long} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfLongs(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getLongArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLongArray(pos);
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
    return getFloatArray(column);
  }

  /**
   * Get an array of {@link Float} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfFloats(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getFloatArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getFloatArray(pos);
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
    return getDoubleArray(column);
  }

  /**
   * Get an array of {@link Double} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfDoubles(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getDoubleArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getDoubleArray(pos);
  }

  /**
   * Get an array of {@link Numeric} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getArrayOfNumerics(String column) {
    return getNumericArray(column);
  }

  /**
   * Get an array of {@link Numeric} value for the given {@code column}.
   *
   * @param column the column
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfNumerics(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getNumericArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getNumericArray(pos);
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
    return getStringArray(column);
  }

  /**
   * Get an array of {@link String} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfStrings(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getStringArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getStringArray(pos);
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
    return getTemporalArray(column);
  }

  /**
   * Get an array of {@link Temporal} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfTemporals(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getTemporalArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getTemporalArray(pos);
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
    return getLocalDateArray(column);
  }

  /**
   * Get an array of {@link LocalDate} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfLocalDates(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getLocalDateArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalDateArray(pos);
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
    return getLocalTimeArray(column);
  }

  /**
   * Get an array of {@link LocalTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfLocalTimes(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getLocalTimeArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalTimeArray(pos);
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
    return getLocalDateTimeArray(column);
  }

  /**
   * Get an array of {@link LocalDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfLocalDateTimes(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getLocalDateTimeArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getLocalDateTimeArray(pos);
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
    return getOffsetTimeArray(column);
  }

  /**
   * Get an array of {@link OffsetTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfOffsetTimes(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getOffsetTimeArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getOffsetTimeArray(pos);
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
    return getOffsetDateTimeArray(column);
  }

  /**
   * Get an array of {@link OffsetDateTime} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfOffsetDateTimes(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getOffsetDateTimeArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getOffsetDateTimeArray(pos);
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
    return getBufferArray(column);
  }

  /**
   * Get an array of {@link Buffer} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfBuffers(String)}
   */
  @Deprecated
  @GenIgnore
  default Buffer[] getBufferArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBufferArray(pos);
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
    return getUUIDArray(column);
  }

  /**
   * Get an array of {@link UUID} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfUUIDs(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getUUIDArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getUUIDArray(pos);
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
    return getBigDecimalArray(column);
  }

  /**
   * Get an array of {@link BigDecimal} value for the given {@code column}.
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   * @deprecated instead use {@link #getArrayOfBigDecimals(String)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getBigDecimalArray(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getBigDecimalArray(pos);
  }

  @GenIgnore
  <T> T[] getValues(Class<T> type, int idx);

  /**
   * Get an array of JSON elements for the given {@code column}, the element might be {@link io.vertx.sqlclient.Tuple#JSON_NULL null} or one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   * </ul>
   *
   * @param column the column name
   * @return the {@code column} value
   * @throws NoSuchElementException when the {@code column} does not exist
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Object[] getArrayOfJsons(String column) {
    int pos = getColumnIndex(column);
    if (pos == -1) {
      throw new NoSuchElementException("Column " + column + " does not exist");
    }
    return getArrayOfJsons(pos);
  }

}
