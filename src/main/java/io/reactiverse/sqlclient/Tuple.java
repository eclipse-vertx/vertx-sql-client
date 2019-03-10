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

package io.reactiverse.sqlclient;

import io.reactiverse.sqlclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.UUID;

/**
 * A general purpose tuple.
 */
@VertxGen
public interface Tuple {

  /**
   * @return a new empty tuple
   */
  static Tuple tuple() {
    return new ArrayTuple(10);
  }

  /**
   * Create a tuple of one element.
   *
   * @param elt1 the first value
   * @return the tuple
   */
  static Tuple of(Object elt1) {
    ArrayTuple tuple = new ArrayTuple(1);
    tuple.addValue(elt1);
    return tuple;
  }

  /**
   * Create a tuple of two elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @return the tuple
   */
  static Tuple of(Object elt1, Object elt2) {
    ArrayTuple tuple = new ArrayTuple(2);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    return tuple;
  }

  /**
   * Create a tuple of three elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @return the tuple
   */
  static Tuple of(Object elt1, Object elt2, Object elt3) {
    ArrayTuple tuple = new ArrayTuple(3);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    return tuple;
  }

  /**
   * Create a tuple of four elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @return the tuple
   */
  static Tuple of(Object elt1, Object elt2, Object elt3, Object elt4) {
    ArrayTuple tuple = new ArrayTuple(4);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    return tuple;
  }

  /**
   * Create a tuple of five elements.
   *
   * @param elt1 the first value
   * @param elt2 the second value
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @param elt5 the fifth value
   * @return the tuple
   */
  static Tuple of(Object elt1, Object elt2, Object elt3, Object elt4, Object elt5) {
    ArrayTuple tuple = new ArrayTuple(5);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    tuple.addValue(elt5);
    return tuple;
  }

  /**
   * Create a tuple of six elements.
   *
   * @param elt1 the first value
   * @param elt2 the second valueg
   * @param elt3 the third value
   * @param elt4 the fourth value
   * @param elt5 the fifth value
   * @param elt6 the sixth value
   * @return the tuple
   */
  static Tuple of(Object elt1, Object elt2, Object elt3, Object elt4, Object elt5, Object elt6) {
    ArrayTuple tuple = new ArrayTuple(5);
    tuple.addValue(elt1);
    tuple.addValue(elt2);
    tuple.addValue(elt3);
    tuple.addValue(elt4);
    tuple.addValue(elt5);
    tuple.addValue(elt6);
    return tuple;
  }

  /**
   * Create a tuple of an arbitrary number of elements.
   *
   * @param elt1 the first element
   * @param elts the remaining elements
   * @return the tuple
   */
  @GenIgnore
  static Tuple of(Object elt1, Object... elts) {
    ArrayTuple tuple = new ArrayTuple(1 + elts.length);
    tuple.addValue(elt1);
    for (Object elt: elts) {
      tuple.addValue(elt);
    }
    return tuple;
  }

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Boolean getBoolean(int pos);

  /**
   * Get an object value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Object getValue(int pos);

  /**
   * Get a short value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Short getShort(int pos);

  /**
   * Get an integer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Integer getInteger(int pos);

  /**
   * Get a long value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Long getLong(int pos);

  /**
   * Get a float value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Float getFloat(int pos);

  /**
   * Get a double value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Double getDouble(int pos);

  /**
   * Get a string value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  String getString(int pos);

  /**
   * Get a {@link java.time.temporal.Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Temporal getTemporal(int pos);

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDate getLocalDate(int pos);

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalTime getLocalTime(int pos);

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDateTime getLocalDateTime(int pos);

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetTime getOffsetTime(int pos);

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetDateTime getOffsetDateTime(int pos);

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  UUID getUUID(int pos);

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  BigDecimal getBigDecimal(int pos);

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Integer[] getIntegerArray(int pos);

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Boolean[] getBooleanArray(int pos);

  /**
   * Get an array of  {@link Short} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Short[] getShortArray(int pos);

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Long[] getLongArray(int pos);

  /**
   * Get an array of  {@link Float} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Float[] getFloatArray(int pos);

  /**
   * Get an array of  {@link Double} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Double[] getDoubleArray(int pos);

  /**
   * Get an array of  {@link String} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  String[] getStringArray(int pos);

  /**
   * Get an array of  {@link LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDate[] getLocalDateArray(int pos);

  /**
   * Get an array of  {@link LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalTime[] getLocalTimeArray(int pos);

  /**
   * Get an array of  {@link OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetTime[] getOffsetTimeArray(int pos);

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  LocalDateTime[] getLocalDateTimeArray(int pos);

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  OffsetDateTime[] getOffsetDateTimeArray(int pos);

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  Buffer[] getBufferArray(int pos);

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  UUID[] getUUIDArray(int pos);

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Buffer getBuffer(int pos);

  /**
   * Add a boolean value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addBoolean(Boolean value);

  /**
   * Add an object value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addValue(Object value);

  /**
   * Add a short value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addShort(Short value);

  /**
   * Add an integer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addInteger(Integer value);

  /**
   * Add a long value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addLong(Long value);

  /**
   * Add a float value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addFloat(Float value);

  /**
   * Add a double value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addDouble(Double value);

  /**
   * Add a string value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addString(String value);

  /**
   * Add a buffer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addBuffer(Buffer value);

  /**
   * Add a {@link java.time.temporal.Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addTemporal(Temporal value);

  /**
   * Add a {@link java.time.LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalDate(LocalDate value);

  /**
   * Add a {@link java.time.LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalTime(LocalTime value);

  /**
   * Add a {@link java.time.LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalDateTime(LocalDateTime value);

  /**
   * Add a {@link java.time.OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addOffsetTime(OffsetTime value);

  /**
   * Add a {@link java.time.OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addOffsetDateTime(OffsetDateTime value);

  /**
   * Add a {@link java.util.UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addUUID(UUID value);

  /**
   * Add a {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addBigDecimal(BigDecimal value);

  /**
   * Add an array of {@code Integer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addIntegerArray(Integer[] value);

  /**
   * Add an array of {@code Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addBooleanArray(Boolean[] value);

  /**
   * Add an array of {@link Short} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addShortArray(Short[] value);

  /**
   * Add an array of {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLongArray(Long[] value);

  /**
   * Add an array of {@link Float} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addFloatArray(Float[] value);

  /**
   * Add an array of {@link Double} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addDoubleArray(Double[] value);

  /**
   * Add an array of {@link String} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addStringArray(String[] value);

  /**
   * Add an array of {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalDateArray(LocalDate[] value);

  /**
   * Add an array of {@link LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalTimeArray(LocalTime[] value);

  /**
   * Add an array of {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addOffsetTimeArray(OffsetTime[] value);

  /**
   * Add an array of {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addLocalDateTimeArray(LocalDateTime[] value);

  /**
   * Add an array of {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addOffsetDateTimeArray(OffsetDateTime[] value);

  /**
   * Add an array of {@link Buffer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addBufferArray(Buffer[] value);

  /**
   * Add an array of {@link UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  Tuple addUUIDArray(UUID[] value);

  <T> T get(Class<T> type, int pos);

  @GenIgnore
  <T> T[] getValues(Class<T> type, int pos);

  @GenIgnore
  <T> Tuple addValues(T[] value);

  /**
   * @return the tuple size
   */
  int size();

  void clear();

}
