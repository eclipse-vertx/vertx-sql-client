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

import io.vertx.sqlclient.impl.ArrayTuple;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.impl.ListTuple;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.Arrays;
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
  Object JSON_NULL = new Object();

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
  static Tuple wrap(List<Object> list) {
    return new ListTuple(list);
  }

  /**
   * Wrap the provided {@code array} with a tuple.
   * <br/>
   * The array is not copied and is used as store for tuple elements.
   *
   * @return the list wrapped as a tuple
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  static Tuple wrap(Object... array) {
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
   * Create a tuple of six elements.
   *
   * @param elements the list of elements
   * @return the tuple
   */
  static Tuple tuple(List<Object> elements) {
    ArrayTuple tuple = new ArrayTuple(5);
    return tuple;
  }

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Boolean getBoolean(int pos) {
    Object val = getValue(pos);
    if (val instanceof Boolean) {
      return (Boolean) val;
    }
    return null;
  }

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
  default Short getShort(int pos) {
    Object val = getValue(pos);
    if (val instanceof Short) {
      return (Short) val;
    } else if (val instanceof Number) {
      return ((Number) val).shortValue();
    }
    return null;
  }

  /**
   * Get an integer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Integer getInteger(int pos) {
    Object val = getValue(pos);
    if (val instanceof Integer) {
      return (Integer) val;
    } else if (val instanceof Number) {
      return ((Number) val).intValue();
    }
    return null;
  }

  /**
   * Get a long value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Long getLong(int pos) {
    Object val = getValue(pos);
    if (val instanceof Long) {
      return (Long) val;
    } else if (val instanceof Number) {
      return ((Number) val).longValue();
    }
    return null;
  }

  /**
   * Get a float value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Float getFloat(int pos) {
    Object val = getValue(pos);
    if (val instanceof Float) {
      return (Float) val;
    } else if (val instanceof Number) {
      return ((Number) val).floatValue();
    }
    return null;
  }

  /**
   * Get a double value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Double getDouble(int pos) {
    Object val = getValue(pos);
    if (val instanceof Double) {
      return (Double) val;
    } else if (val instanceof Number) {
      return ((Number) val).doubleValue();
    }
    return null;
  }

  /**
   * Get a string value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default String getString(int pos) {
    Object val = getValue(pos);
    if (val instanceof String) {
      return (String) val;
    }
    return null;
  }

  /**
   * Get a {@link java.time.temporal.Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal getTemporal(int pos) {
    Object val = getValue(pos);
    if (val instanceof Temporal) {
      return (Temporal) val;
    }
    return null;
  }

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate getLocalDate(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalDate) {
      return (LocalDate) val;
    }
    return null;
  }

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime getLocalTime(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalTime) {
      return (LocalTime) val;
    }
    return null;
  }

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalDateTime) {
      return (LocalDateTime) val;
    }
    return null;
  }

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime getOffsetTime(int pos) {
    Object val = getValue(pos);
    if (val instanceof OffsetTime) {
      return (OffsetTime) val;
    }
    return null;
  }

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime getOffsetDateTime(int pos) {
    Object val = getValue(pos);
    if (val instanceof OffsetDateTime) {
      return (OffsetDateTime) val;
    }
    return null;
  }

  /**
   * Get {@link java.util.UUID} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID getUUID(int pos) {
    Object val = getValue(pos);
    if (val instanceof UUID) {
      return (UUID) val;
    }
    return null;
  }

  /**
   * Get {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal getBigDecimal(int pos) {
    Object val = getValue(pos);
    if (val instanceof BigDecimal) {
      return (BigDecimal) val;
    } else if (val instanceof Number) {
      return new BigDecimal(val.toString());
    }
    return null;
  }

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getIntegerArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Integer[]) {
      return (Integer[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getBooleanArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Boolean[]) {
      return (Boolean[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link Short} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getShortArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Short[]) {
      return (Short[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getLongArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Long[]) {
      return (Long[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link Float} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getFloatArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Float[]) {
      return (Float[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link Double} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getDoubleArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Double[]) {
      return (Double[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link String} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getStringArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof String[]) {
      return (String[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getLocalDateArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalDate[]) {
      return (LocalDate[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getLocalTimeArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalTime[]) {
      return (LocalTime[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getOffsetTimeArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof OffsetTime[]) {
      return (OffsetTime[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getLocalDateTimeArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof LocalDateTime[]) {
      return (LocalDateTime[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getOffsetDateTimeArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof OffsetDateTime[]) {
      return (OffsetDateTime[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  default Buffer[] getBufferArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof Buffer[]) {
      return (Buffer[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getUUIDArray(int pos) {
    Object val = getValue(pos);
    if (val instanceof UUID[]) {
      return (UUID[]) val;
    } else {
      return null;
    }
  }

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Buffer getBuffer(int pos) {
    Object val = getValue(pos);
    if (val instanceof Buffer) {
      return (Buffer) val;
    }
    return null;
  }

  /**
   * Add a boolean value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addBoolean(Boolean value) {
    return addValue(value);
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
   * Add a short value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addShort(Short value) {
    return addValue(value);
  }

  /**
   * Add an integer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addInteger(Integer value)  {
    return addValue(value);
  }

  /**
   * Add a long value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addLong(Long value) {
    return addValue(value);
  }

  /**
   * Add a float value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addFloat(Float value) {
    return addValue(value);
  }

  /**
   * Add a double value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addDouble(Double value) {
    return addValue(value);
  }

  /**
   * Add a string value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addString(String value) {
    return addValue(value);
  }

  /**
   * Add a buffer value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @Fluent
  default Tuple addBuffer(Buffer value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.temporal.Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addTemporal(Temporal value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDate(LocalDate value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalTime(LocalTime value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDateTime(LocalDateTime value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetTime(OffsetTime value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.time.OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetDateTime(OffsetDateTime value) {
    return addValue(value);
  }

  /**
   * Add a {@link java.util.UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addUUID(UUID value) {
    return addValue(value);
  }

  /**
   * Add a {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addBigDecimal(BigDecimal value) {
    return addValue(value);
  }

  /**
   * Add an array of {@code Integer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addIntegerArray(Integer[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@code Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addBooleanArray(Boolean[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Short} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addShortArray(Short[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLongArray(Long[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Float} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addFloatArray(Float[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Double} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addDoubleArray(Double[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link String} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addStringArray(String[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDateArray(LocalDate[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalTimeArray(LocalTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetTimeArray(OffsetTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDateTimeArray(LocalDateTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetDateTimeArray(OffsetDateTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Buffer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore
  default Tuple addBufferArray(Buffer[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addUUIDArray(UUID[] value) {
    return addValue(value);
  }

  default <T> T get(Class<T> type, int pos) {
    if (type == null) {
      throw new IllegalArgumentException("Accessor type can not be null");
    }
    Object value = getValue(pos);
    if (value.getClass() == type) {
      return type.cast(value);
    } else {
      try {
        if (value instanceof Buffer) {
          return type.cast(value);
        } else if (value instanceof Temporal) {
          return type.cast(value);
        }
      } catch (ClassCastException e) {
        throw new IllegalArgumentException("mismatched type [" + type.getName() + "] for the value of type [" + value.getClass().getName() + "]");
      }
      throw new IllegalArgumentException("mismatched type [" + type.getName() + "] for the value of type [" + value.getClass().getName() + "]");
    }
  }

  @GenIgnore
  default <T> T[] getValues(Class<T> type, int pos) {
    if (type == null) {
      throw new IllegalArgumentException("Accessor type can not be null");
    }
    Object value = getValue(pos);
    if (value.getClass().isArray() && value.getClass().getComponentType() == type) {
      return (T[]) value;
    } else {
      throw new IllegalArgumentException("mismatched array element type [" + type.getName() + "] for the value of type [" + value.getClass().getName() + "]");
    }
  }

  @GenIgnore
  default <T> Tuple addValues(T[] value) {
    return addValue(value);
  }

  /**
   * @return the tuple size
   */
  int size();

  void clear();

}
