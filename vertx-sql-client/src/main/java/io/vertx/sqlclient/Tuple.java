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

import io.netty.buffer.ByteBuf;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.NullValue;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.sqlclient.impl.ListTuple;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * A general purpose tuple.
 */
@VertxGen
public interface Tuple {

  /**
   * The JSON null literal value.
   * <br/>
   * It is used to distinguish a JSON null literal value from the Java {@code null} value. This is only
   * used when the database supports JSON types.
   */
  Object JSON_NULL = new Object() {
    @Override
    public String toString() {
      return "null";
    }
  };

  /**
   * @return a new empty tuple
   */
  static Tuple tuple() {
    return new ArrayTuple(10);
  }

  /**
   * Wrap the provided {@code list} with a tuple.
   * <br/>
   * The list is not copied and is used as store for tuple elements.
   *
   * @return the list wrapped as a tuple
   */
  static <T> Tuple from(List<T> list) {
    return wrap(new ArrayList<>(list));
  }

  /**
   * Wrap the provided {@code array} with a tuple.
   * <br/>
   * The array is not copied and is used as store for tuple elements.
   *
   * @return the list wrapped as a tuple
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static Tuple from(Object[] array) {
    List<Object> list = new ArrayList<>(array.length);
    Collections.addAll(list, array);
    return wrap(list);
  }

  /**
   * Wrap the provided {@code list} with a tuple.
   * <br/>
   * The list is not copied and is used as store for tuple elements.
   * <br/>
   * Note: The list might be modified and users should use {@link #tuple(List)} if the list is unmodifiable
   *
   * @return the list wrapped as a tuple
   */
  @SuppressWarnings("unchecked")
  static <T> Tuple wrap(List<T> list) {
    return new ListTuple((List<Object>) list);
  }

  /**
   * Wrap the provided {@code array} with a tuple.
   * <br/>
   * The array is not copied and is used as store for tuple elements.
   *
   * @return the list wrapped as a tuple
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static Tuple wrap(Object[] array) {
    return new ListTuple(Arrays.asList(array));
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
    ArrayTuple tuple = new ArrayTuple(6);
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
    for (Object elt : elts) {
      tuple.addValue(elt);
    }
    return tuple;
  }

  /**
   * Create a tuple with the provided {@code elements} list.
   * <p/>
   * The {@code elements} list is not modified.
   *
   * @param elements the list of elements
   * @return the tuple
   */
  static Tuple tuple(List<Object> elements) {
    return new ArrayTuple(elements);
  }

  /**
   * Get an object value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  Object getValue(int pos);

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Boolean getBoolean(int pos) {
    return (Boolean) getValue(pos);
  }

  /**
   * Get a short value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Short getShort(int pos) {
    return ChainConverter.allowCast(Short.class)
      .orNext(o -> ((Number) o).shortValue())
      .orNext(o -> (short) ((Enum<?>) o).ordinal())
      .apply(getValue(pos));
  }

  /**
   * Get an integer value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Integer getInteger(int pos) {
    return ChainConverter.allowCast(Integer.class)
      .orNext(o -> ((Number) o).intValue())
      .orNext(o -> ((Enum<?>) o).ordinal())
      .apply(getValue(pos));
  }

  /**
   * Get a long value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Long getLong(int pos) {
    return ChainConverter.allowCast(Long.class)
      .orNext(o -> ((Number) o).longValue())
      .orNext(o -> (long) ((Enum<?>) o).ordinal())
      .apply(getValue(pos));
  }

  /**
   * Get a float value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Float getFloat(int pos) {
    return ChainConverter.allowCast(Float.class)
      .orNext(o -> ((Number) o).floatValue())
      .orNext(o -> (float) ((Enum<?>) o).ordinal())
      .apply(getValue(pos));
  }

  /**
   * Get a double value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Double getDouble(int pos) {
    return ChainConverter.allowCast(Double.class)
      .orNext(o -> ((Number) o).doubleValue())
      .orNext(o -> (double) ((Enum<?>) o).ordinal())
      .apply(getValue(pos));
  }

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric getNumeric(int pos) {
    return ChainConverter.allowCast(Numeric.class).orNext(o -> Numeric.create((Number) o)).apply(getValue(pos));
  }

  /**
   * Get a string value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default String getString(int pos) {
    return ChainConverter.allowCast(String.class).orNext(o -> ((Enum<?>) o).name()).apply(getValue(pos));
  }

  /**
   * Get a {@link JsonObject} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default JsonObject getJsonObject(int pos) {
    return ChainConverter.allowJsonObject().apply(getValue(pos));
  }

  /**
   * Get a {@link JsonArray} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default JsonArray getJsonArray(int pos) {
    return ChainConverter.allowJsonArray().apply(getValue(pos));
  }

  /**
   * Get a JSON element at {@code pos}, the element might be {@link io.vertx.sqlclient.Tuple#JSON_NULL null} or one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   * </ul>
   *
   * @param pos the position
   * @return the value
   */
  default Object getJson(int pos) {
    return ChainConverter.allowJson().apply(getValue(pos));
  }

  /**
   * Get a {@link java.time.temporal.Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal getTemporal(int pos) {
    return (Temporal) getValue(pos);
  }

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime} will be
   * coerced to {@code LocalDate}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate getLocalDate(int pos) {
    return ChainConverter.allowCast(LocalDate.class)
      .orNext(o -> ((LocalDateTime) o).toLocalDate())
      .orNext(o -> ((OffsetDateTime) o).toLocalDate())
      .apply(getValue(pos));
  }

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime} will be
   * coerced to {@code LocalTime}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime getLocalTime(int pos) {
    return ChainConverter.allowCast(LocalTime.class)
      .orNext(o -> ((LocalDateTime) o).toLocalTime())
      .orNext(o -> ((OffsetDateTime) o).toLocalTime())
      .apply(getValue(pos));
  }

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(int pos) {
    return ChainConverter.allowCast(LocalDateTime.class)
      .orNext(o -> ((OffsetDateTime) o).toLocalDateTime())
      .apply(getValue(pos));
  }

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code OffsetDateTime} will be
   * coerced to {@code OffsetTime}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime getOffsetTime(int pos) {
    return ChainConverter.allowCast(OffsetTime.class)
      .orNext(o -> ((OffsetDateTime) o).toOffsetTime())
      .apply(getValue(pos));
  }

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime getOffsetDateTime(int pos) {
    return (OffsetDateTime) getValue(pos);
  }

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Buffer getBuffer(int pos) {
    return ChainConverter.allowCast(Buffer.class)
      .orNext(o -> Buffer.buffer((ByteBuf) o))
      .orNext(o -> Buffer.buffer((byte[]) o))
      .apply(getValue(pos));
  }

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID getUUID(int pos) {
    return ChainConverter.allowCast(UUID.class).orNext(o -> UUID.fromString((String) o)).apply(getValue(pos));
  }

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal getBigDecimal(int pos) {
    return ChainConverter.allowCast(BigDecimal.class)
      .orNext(o -> BigDecimal.valueOf(((Number) o).doubleValue()))
      .apply(getValue(pos));
  }

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * <p>Target element instance of {@code Object[]} will be
   * coerced to {@code Boolean[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getArrayOfBooleans(int pos) {
    return ChainConverter.allowCast(Boolean.class).toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link Short} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Short[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getArrayOfShorts(int pos) {
    return ChainConverter.allowCast(Short.class)
      .orNext(o -> ((Number) o).shortValue())
      .orNext(o -> (short) ((Enum<?>) o).ordinal())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Integer[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getArrayOfIntegers(int pos) {
    return ChainConverter.allowCast(Integer.class)
      .orNext(o -> ((Number) o).intValue())
      .orNext(o -> ((Enum<?>) o).ordinal())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Long[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getArrayOfLongs(int pos) {
    return ChainConverter.allowCast(Long.class)
      .orNext(o -> ((Number) o).longValue())
      .orNext(o -> (long) ((Enum<?>) o).ordinal())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link Float} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Float[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getArrayOfFloats(int pos) {
    return ChainConverter.allowCast(Float.class)
      .orNext(o -> ((Number) o).floatValue())
      .orNext(o -> (float) ((Enum<?>) o).ordinal())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link Double} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Double[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getArrayOfDoubles(int pos) {
    return ChainConverter.allowCast(Double.class)
      .orNext(o -> ((Number) o).doubleValue())
      .orNext(o -> (double) ((Enum<?>) o).ordinal())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of {@link Numeric} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getArrayOfNumerics(int pos) {
    return ChainConverter.allowCast(Numeric.class)
      .orNext(o -> Numeric.create((Number) o))
      .orNext(o -> Numeric.create(((Enum<?>) o).ordinal()))
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link String} value at {@code pos}.
   *
   * <p>Target element instance of {@code Object[]} will be
   * coerced to {@code String[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getArrayOfStrings(int pos) {
    return ChainConverter.allowCast(String.class)
      .orNext(o -> ((Enum<?>) o).name())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link JsonObject} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonObject[] getArrayOfJsonObjects(int pos) {
    return ChainConverter.allowJsonObject().toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link JsonArray} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonArray[] getArrayOfJsonArrays(int pos) {
    return ChainConverter.allowJsonArray().toArray(getValue(pos));
  }

  /**
   * Get an array of JSON elements at {@code pos}, the element might be {@link io.vertx.sqlclient.Tuple#JSON_NULL null} or one of the following types:
   * <ul>
   *   <li>String</li>
   *   <li>Number</li>
   *   <li>JsonObject</li>
   *   <li>JsonArray</li>
   *   <li>Boolean</li>
   * </ul>
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Object[] getArrayOfJsons(int pos) {
    return ChainConverter.allowJson().toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getArrayOfTemporals(int pos) {
    return (Temporal[]) getValue(pos);
  }

  /**
   * Get an array of  {@link LocalDate} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime[]} will be
   * coerced to {@code LocalDate[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getArrayOfLocalDates(int pos) {
    return ChainConverter.allowCast(LocalDate.class)
      .orNext(o -> ((LocalDateTime) o).toLocalDate())
      .orNext(o -> ((OffsetDateTime) o).toLocalDate())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link LocalTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime[]} will be
   * coerced to {@code LocalTime[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getArrayOfLocalTimes(int pos) {
    return ChainConverter.allowCast(LocalTime.class)
      .orNext(o -> ((LocalDateTime) o).toLocalTime())
      .orNext(o -> ((OffsetDateTime) o).toLocalTime())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getArrayOfLocalDateTimes(int pos) {
    return ChainConverter.allowCast(LocalDateTime.class)
      .orNext(o -> ((OffsetDateTime) o).toLocalDateTime())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link OffsetTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code OffsetDateTime[]} will be
   * coerced to {@code OffsetTime[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getArrayOfOffsetTimes(int pos) {
    return ChainConverter.allowCast(OffsetTime.class)
      .orNext(o -> ((OffsetDateTime) o).toOffsetTime())
      .toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getArrayOfOffsetDateTimes(int pos) {
    return ChainConverter.allowCast(OffsetDateTime.class).toArray(getValue(pos));
  }

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore
  default Buffer[] getArrayOfBuffers(int pos) {
    return ChainConverter.allowCast(Buffer.class)
      .orNext(o -> Buffer.buffer((ByteBuf) o))
      .orNext(o -> Buffer.buffer((byte[]) o))
      .toArray(getValue(pos));
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getArrayOfUUIDs(int pos) {
    return ChainConverter.allowCast(UUID.class).orNext(o -> UUID.fromString((String) o)).toArray(getValue(pos));
  }

  /**
   * Get an array of {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getArrayOfBigDecimals(int pos) {
    return ChainConverter.allowCast(BigDecimal.class)
      .orNext(o -> BigDecimal.valueOf(((Number) o).doubleValue()))
      .toArray(getValue(pos));
  }

  /**
   * Add an object value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  Tuple addValue(Object value);

  /**
   * Add a boolean value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addBoolean(Boolean value) {
    return addValue(value == null ? NullValue.Boolean : value);
  }

  /**
   * Add a short value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addShort(Short value) {
    return addValue(value == null ? NullValue.Short : value);
  }

  /**
   * Add an integer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addInteger(Integer value) {
    return addValue(value == null ? NullValue.Integer : value);
  }

  /**
   * Add a long value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addLong(Long value) {
    return addValue(value == null ? NullValue.Long : value);
  }

  /**
   * Add a float value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addFloat(Float value) {
    return addValue(value == null ? NullValue.Float : value);
  }

  /**
   * Add a double value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addDouble(Double value) {
    return addValue(value == null ? NullValue.Double : value);
  }

  /**
   * Add a string value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addString(String value) {
    return addValue(value == null ? NullValue.String : value);
  }

  /**
   * Add a {@link JsonObject} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addJsonObject(JsonObject value) {
    return addValue(value == null ? NullValue.JsonObject : value);
  }

  /**
   * Add a {@link JsonArray} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addJsonArray(JsonArray value) {
    return addValue(value == null ? NullValue.JsonArray : value);
  }

  /**
   * Add a {@link java.time.temporal.Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addTemporal(Temporal value) {
    return addValue(value == null ? NullValue.Temporal : value);
  }

  /**
   * Add a {@link java.time.LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDate(LocalDate value) {
    return addValue(value == null ? NullValue.LocalDate : value);
  }

  /**
   * Add a {@link java.time.LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalTime(LocalTime value) {
    return addValue(value == null ? NullValue.LocalTime : value);
  }

  /**
   * Add a {@link java.time.LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDateTime(LocalDateTime value) {
    return addValue(value == null ? NullValue.LocalDateTime : value);
  }

  /**
   * Add a {@link java.time.OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetTime(OffsetTime value) {
    return addValue(value == null ? NullValue.OffsetTime : value);
  }

  /**
   * Add a {@link java.time.OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetDateTime(OffsetDateTime value) {
    return addValue(value == null ? NullValue.OffsetDateTime : value);
  }

  /**
   * Add a buffer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addBuffer(Buffer value) {
    return addValue(value == null ? NullValue.Buffer : value);
  }

  /**
   * Add a {@link java.util.UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addUUID(UUID value) {
    return addValue(value == null ? NullValue.UUID : value);
  }

  /**
   * Add a {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addBigDecimal(BigDecimal value) {
    return addValue(value == null ? NullValue.BigDecimal : value);
  }

  /**
   * Add an array of {@code Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfBoolean(Boolean[] value) {
    return addValue(value == null ? NullValue.ArrayOfBoolean : value);
  }

  /**
   * Add an array of {@link Short} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfShort(Short[] value) {
    return addValue(value == null ? NullValue.ArrayOfShort : value);
  }

  /**
   * Add an array of {@code Integer} value at the end of the tuple.
   *
   * <p>Target element instance of {@code Number[]} will be
   * coerced to {@code Integer[]}.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfInteger(Integer[] value) {
    return addValue(value == null ? NullValue.ArrayOfInteger : value);
  }

  /**
   * Add an array of {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLong(Long[] value) {
    return addValue(value == null ? NullValue.ArrayOfLong : value);
  }

  /**
   * Add an array of {@link Float} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfFloat(Float[] value) {
    return addValue(value == null ? NullValue.ArrayOfFloat : value);
  }

  /**
   * Add an array of {@link Double} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfDouble(Double[] value) {
    return addValue(value == null ? NullValue.ArrayOfDouble : value);
  }

  /**
   * Add an array of {@link String} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfString(String[] value) {
    return addValue(value == null ? NullValue.ArrayOfString : value);
  }

  /**
   * Add an array of {@link JsonObject} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfJsonObject(JsonObject[] value) {
    return addValue(value == null ? NullValue.ArrayOfJsonObject : value);
  }

  /**
   * Add an array of {@link JsonArray} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfJsonArray(JsonArray[] value) {
    return addValue(value == null ? NullValue.ArrayOfJsonArray : value);
  }

  /**
   * Add an array of {@link Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfTemporal(Temporal[] value) {
    return addValue(value == null ? NullValue.ArrayOfTemporal : value);
  }

  /**
   * Add an array of {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLocalDate(LocalDate[] value) {
    return addValue(value == null ? NullValue.ArrayOfLocalDate : value);
  }

  /**
   * Add an array of {@link LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLocalTime(LocalTime[] value) {
    return addValue(value == null ? NullValue.ArrayOfLocalTime : value);
  }

  /**
   * Add an array of {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLocalDateTime(LocalDateTime[] value) {
    return addValue(value == null ? NullValue.ArrayOfLocalDateTime : value);
  }

  /**
   * Add an array of {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfOffsetTime(OffsetTime[] value) {
    return addValue(value == null ? NullValue.ArrayOfOffsetTime : value);
  }

  /**
   * Add an array of {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfOffsetDateTime(OffsetDateTime[] value) {
    return addValue(value == null ? NullValue.ArrayOfOffsetDateTime : value);
  }

  /**
   * Add an array of {@link Buffer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  default Tuple addArrayOfBuffer(Buffer[] value) {
    return addValue(value == null ? NullValue.ArrayOfBuffer : value);
  }

  /**
   * Add an array of {@link UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfUUID(UUID[] value) {
    return addValue(value == null ? NullValue.ArrayOfUUID : value);
  }

  /**
   * Add an array of {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfBigDecimal(BigDecimal[] value) {
    return addValue(value == null ? NullValue.ArrayOfBigDecimal : value);
  }

  /**
   * Get the value at the specified {@code position} and the specified {@code type}.
   *
   * <p>The type can be one of the types returned by the row (e.g {@code String.class}) or an array
   * of the type (e.g {@code String[].class})).
   *
   * @param type     the expected value type
   * @param position the value position
   * @return the value if the value is found or null.
   */
  default <T> T get(Class<T> type, int position) {
    if (type == null) {
      throw new IllegalArgumentException("Accessor type can not be null");
    }
    Object value = getValue(position);
    if (value != null && type.isAssignableFrom(value.getClass())) {
      return type.cast(value);
    }
    return null;
  }

  /**
   * @return the tuple size
   */
  int size();

  void clear();

  /**
   * @return the list of types built from the tuple
   */
  @GenIgnore
  List<Class<?>> types();

    /**
     * @return A String containing the {@link Object#toString} value of each element,
     * separated by a comma (,) character
     */
  default String deepToString() {
    StringBuilder sb = new StringBuilder();
    sb.append("[");
    final int size = size();
    for (int i = 0; i < size; i++) {
      sb.append(getValue(i));
      if (i + 1 < size)
        sb.append(",");
    }
    sb.append("]");
    return sb.toString();
  }

}
