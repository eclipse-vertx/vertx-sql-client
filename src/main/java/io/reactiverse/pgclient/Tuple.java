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

import io.reactiverse.pgclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
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
   * @param elt2 the second value
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
   * @param elements the elements
   * @return the tuple
   */
  @GenIgnore
  static Tuple of(Object... elements) {
    ArrayTuple tuple = new ArrayTuple(elements.length);
    for (Object elt: elements) {
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
   * Get a {@link Character} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Character getCharacter(int pos);

  /**
   * Get a json value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  Json getJson(int pos);

  /**
   * Get a json object value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  JsonObject getJsonObject(int pos);

  /**
   * Get a json array value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  JsonArray getJsonArray(int pos);

  /**
   * Get a {@link java.time.temporal.Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  Temporal getTemporal(int pos);

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDate getLocalDate(int pos);

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalTime getLocalTime(int pos);

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDateTime getLocalDateTime(int pos);

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetTime getOffsetTime(int pos);

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetDateTime getOffsetDateTime(int pos);

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  UUID getUUID(int pos);

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  BigDecimal getBigDecimal(int pos);

  @GenIgnore
  int[] getIntegerArray(int pos);

  /**
   * Get {@link Boolean} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  boolean[] getBooleanArray(int pos);

  /**
   * Get {@link Short} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  short[] getShortArray(int pos);

  /**
   * Get {@link Long} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  long[] getLongArray(int pos);

  /**
   * Get {@link Float} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  float[] getFloatArray(int pos);

  /**
   * Get {@link Double} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  double[] getDoubleArray(int pos);

  /**
   * Get {@link String} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  String[] getStringArray(int pos);

  /**
   * Get {@link LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDate[] getLocalDateArray(int pos);

  /**
   * Get {@link LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalTime[] getLocalTimeArray(int pos);

  /**
   * Get {@link OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetTime[] getOffsetTimeArray(int pos);

  /**
   * Get {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  LocalDateTime[] getLocalDateTimeArray(int pos);

  /**
   * Get {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  OffsetDateTime[] getOffsetDateTimeArray(int pos);

  /**
   * Get {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  Buffer[] getBufferArray(int pos);

  /**
   * Get {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore
  UUID[] getUUIDArray(int pos);

  @GenIgnore
  char[] getCharacterArray(int pos);

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  Numeric getNumeric(int pos);

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
   * Add a {@link Character} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addCharacter(Character value);

  /**
   * Add a json object value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addJsonObject(JsonObject value);

  /**
   * Add a json array value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addJsonArray(JsonArray value);

  /**
   * Add a json value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addJson(Json value);

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
  @GenIgnore
  Tuple addTemporal(Temporal value);

  /**
   * Add a {@link java.time.LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalDate(LocalDate value);

  /**
   * Add a {@link java.time.LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalTime(LocalTime value);

  /**
   * Add a {@link java.time.LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalDateTime(LocalDateTime value);

  /**
   * Add a {@link java.time.OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addOffsetTime(OffsetTime value);

  /**
   * Add a {@link java.time.OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addOffsetDateTime(OffsetDateTime value);

  /**
   * Add a {@link java.util.UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addUUID(UUID value);

  /**
   * Add a {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addBigDecimal(BigDecimal value);

  /**
   * Add a {@link Integer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addIntArray(int[] value);

  /**
   * Add a {@link Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addBooleanArray(boolean[] value);

  /**
   * Add a {@link Short} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addShortArray(short[] value);

  /**
   * Add a {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLongArray(long[] value);

  /**
   * Add a {@link Float} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addFloatArray(float[] value);

  /**
   * Add a {@link Double} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addDoubleArray(double[] value);

  /**
   * Add a {@link String} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addStringArray(String[] value);

  /**
   * Add a {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalDateArray(LocalDate[] value);

  /**
   * Add a {@link LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalTimeArray(LocalTime[] value);

  /**
   * Add a {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addOffsetTimeArray(OffsetTime[] value);

  /**
   * Add a {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addLocalDateTimeArray(LocalDateTime[] value);

  /**
   * Add a {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addOffsetDateTimeArray(OffsetDateTime[] value);

  /**
   * Add a {@link Buffer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addBufferArray(Buffer[] value);

  /**
   * Add a {@link Character} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addCharacterArray(char[] value);

  /**
   * Add a {@link UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addUUIDArray(UUID[] value);
  /**
   * Add a {@link Numeric} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  Tuple addNumeric(Numeric value);

  /**
   * @return the tuple size
   */
  int size();

}
