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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.impl.ArrayTuple;
import io.netty.buffer.ByteBuf;
import io.vertx.codegen.annotations.Fluent;
import io.vertx.codegen.annotations.GenIgnore;
import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.buffer.Buffer;
import io.vertx.sqlclient.impl.ListTuple;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.ArrayList;
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
   * @return the value or {@code null}
   */
  Object getValue(int pos);

  /**
   * Get a boolean value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Boolean getBoolean(int pos) {
    return (Boolean) getValue(pos);
  }

  /**
   * Get a short value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Short getShort(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Short) {
      return (Short) val;
    } else if (val instanceof Number) {
      return ((Number) val).shortValue();
    } else {
      return (Short) val; // Throw CCE
    }
  }

  /**
   * Get an integer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Integer getInteger(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Integer) {
      return (Integer) val;
    } else if (val instanceof Number) {
      return ((Number) val).intValue();
    } else {
      return (Integer) val; // Throw CCE
    }
  }

  /**
   * Get a long value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Long getLong(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Long) {
      return (Long) val;
    } else if (val instanceof Number) {
      return ((Number) val).longValue();
    } else {
      return (Long) val; // Throw CCE
    }
  }

  /**
   * Get a float value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Float getFloat(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Float) {
      return (Float) val;
    } else if (val instanceof Number) {
      return ((Number) val).floatValue();
    } else {
      return (Float) val; // Throw CCE
    }
  }

  /**
   * Get a double value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Double getDouble(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Double) {
      return (Double) val;
    } else if (val instanceof Number) {
      return ((Number) val).doubleValue();
    } else {
      return (Double) val; // Throw CCE
    }
  }

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric getNumeric(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.parse(val.toString());
    } else {
      return (Numeric) val; // Throw CCE
    }
  }

  /**
   * Get a string value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default String getString(int pos) {
    return (String) getValue(pos);

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
    Object val = getValue(pos);
    if (val == null ||
      val == Tuple.JSON_NULL ||
      val instanceof String ||
      val instanceof Boolean ||
      val instanceof Number ||
      val instanceof JsonObject ||
      val instanceof JsonArray) {
      return val;
    } else {
      throw new ClassCastException("Invalid JSON value type " + val.getClass());
    }
  }

  /**
   * Get a {@link java.time.temporal.Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal getTemporal(int pos) {
    return (Temporal) getValue(pos);
  }

  /**
   * Get {@link java.time.LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate getLocalDate(int pos) {
    return (LocalDate) getValue(pos);
  }

  /**
   * Get {@link java.time.LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime getLocalTime(int pos) {
    return (LocalTime) getValue(pos);
  }

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(int pos) {
    return (LocalDateTime) getValue(pos);
  }

  /**
   * Get {@link java.time.OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime getOffsetTime(int pos) {
    return (OffsetTime) getValue(pos);
  }

  /**
   * Get {@link java.time.OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime getOffsetDateTime(int pos) {
    return (OffsetDateTime) getValue(pos);
  }

  /**
   * Get a buffer value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  default Buffer getBuffer(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Buffer) {
      return (Buffer) val;
    } else if (val instanceof ByteBuf) {
      return Buffer.buffer((ByteBuf) val);
    } else if (val instanceof byte[]) {
      return Buffer.buffer((byte[]) val);
    } else {
      return (Buffer) val; // Throw CCE
    }
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
    if (val == null) {
      return null;
    } else if (val instanceof UUID) {
      return (UUID) val;
    } else if (val instanceof String) {
      return UUID.fromString((String) val);
    } else {
      return (UUID) val; // Throw CCE
    }
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
    if (val == null) {
      return null;
    } else if (val instanceof BigDecimal) {
      return (BigDecimal) val;
    } else if (val instanceof Number) {
      return new BigDecimal(val.toString());
    } else {
      return (BigDecimal) val; // Throw CCE
    }
  }

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getArrayOfBooleans(int pos) {
    return getBooleanArray(pos);
  }

  /**
   * Get an array of {@link Boolean} value at {@code pos}.
   *
   * <p>Target element instance of {@code Object[]} will be
   * coerced to {@code Boolean[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfBooleans(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getBooleanArray(int pos) {
    return (Boolean[]) getValue(pos);
  }

  /**
   * Get an array of  {@link Short} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getArrayOfShorts(int pos) {
    return getShortArray(pos);
  }

  /**
   * Get an array of  {@link Short} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Short[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfShorts(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Short[] getShortArray(int pos) {
    return (Short[]) getValue(pos);
  }

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getArrayOfIntegers(int pos) {
    return getIntegerArray(pos);
  }

  /**
   * Get an array of {@link Integer} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Integer[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfIntegers(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Integer[] getIntegerArray(int pos) {
    return (Integer[]) getValue(pos);
  }

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getArrayOfLongs(int pos) {
    return getLongArray(pos);
  }

  /**
   * Get an array of {@link Long} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Long[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfLongs(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Long[] getLongArray(int pos) {
    return (Long[]) getValue(pos);
  }

  /**
   * Get an array of  {@link Float} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getArrayOfFloats(int pos) {
    return getFloatArray(pos);
  }

  /**
   * Get an array of  {@link Float} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Float[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfFloats(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Float[] getFloatArray(int pos) {
    return (Float[]) getValue(pos);
  }

  /**
   * Get an array of  {@link Double} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getArrayOfDoubles(int pos) {
    return getDoubleArray(pos);
  }

  /**
   * Get an array of  {@link Double} value at {@code pos}.
   *
   * <p>Target element instance of {@code Number[]} or {@code Object[]} will be
   * coerced to {@code Double[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfDoubles(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Double[] getDoubleArray(int pos) {
    return (Double[]) getValue(pos);
  }

  /**
   * Get an array of {@link Numeric} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getArrayOfNumerics(int pos) {
    return getNumericArray(pos);
  }

  /**
   * Get an array of {@link Numeric} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   * @deprecated instead use {@link #getArrayOfNumerics(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getNumericArray(int pos) {
    return (Numeric[]) getValue(pos);
  }

  /**
   * Get an array of  {@link String} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getArrayOfStrings(int pos) {
    return getStringArray(pos);
  }

  /**
   * Get an array of  {@link String} value at {@code pos}.
   *
   * <p>Target element instance of {@code Object[]} will be
   * coerced to {@code String[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfStrings(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default String[] getStringArray(int pos) {
    return (String[]) getValue(pos);
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
  default Object[] getArrayOfJson(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof JsonObject[]
      || val instanceof JsonArray[]
      || val instanceof Number[]
      || val instanceof Boolean[]
      || val instanceof String[]) {
      return (Object[]) val;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      for (int i = 0; i < array.length; i++) {
        Object elt = Array.get(val, i);
        if (elt != null && !(elt == Tuple.JSON_NULL ||
          elt instanceof String ||
          elt instanceof Boolean ||
          elt instanceof Number ||
          elt instanceof JsonObject ||
          elt instanceof JsonArray)) {
          throw new ClassCastException();
        }
      }
      return array;
    } else {
      throw new ClassCastException();
    }
  }

  /**
   * Get an array of  {@link Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getArrayOfTemporals(int pos) {
    return getTemporalArray(pos);
  }

  /**
   * Get an array of  {@link Temporal} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfTemporals(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Temporal[] getTemporalArray(int pos) {
    return (Temporal[]) getValue(pos);
  }

  /**
   * Get an array of  {@link LocalDate} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getArrayOfLocalDates(int pos) {
    return getLocalDateArray(pos);
  }

  /**
   * Get an array of  {@link LocalDate} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime[]} will be
   * coerced to {@code LocalDate[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfLocalDates(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDate[] getLocalDateArray(int pos) {
    return (LocalDate[]) getValue(pos);
  }

  /**
   * Get an array of  {@link LocalTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getArrayOfLocalTimes(int pos) {
    return getLocalTimeArray(pos);
  }

  /**
   * Get an array of  {@link LocalTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code LocalDateTime[]} will be
   * coerced to {@code LocalTime[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfLocalTimes(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalTime[] getLocalTimeArray(int pos) {
    return (LocalTime[]) getValue(pos);
  }

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getArrayOfLocalDateTimes(int pos) {
    return getLocalDateTimeArray(pos);
  }

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfLocalDateTimes(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getLocalDateTimeArray(int pos) {
    return (LocalDateTime[]) getValue(pos);
  }

  /**
   * Get an array of  {@link OffsetTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getArrayOfOffsetTimes(int pos) {
    return getOffsetTimeArray(pos);
  }

  /**
   * Get an array of  {@link OffsetTime} value at {@code pos}.
   *
   * <p>Target element instance of {@code OffsetDateTime[]} will be
   * coerced to {@code OffsetTime[]}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfOffsetTimes(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetTime[] getOffsetTimeArray(int pos) {
    return (OffsetTime[]) getValue(pos);
  }

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getArrayOfOffsetDateTimes(int pos) {
    return getOffsetDateTimeArray(pos);
  }

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfOffsetDateTimes(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getOffsetDateTimeArray(int pos) {
    return (OffsetDateTime[]) getValue(pos);
  }

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value or {@code null}
   */
  @GenIgnore
  default Buffer[] getArrayOfBuffers(int pos) {
    return getBufferArray(pos);
  }

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   * @deprecated instead use {@link #getArrayOfBuffers(int)}
   */
  @Deprecated
  @GenIgnore
  default Buffer[] getBufferArray(int pos) {
    return (Buffer[]) getValue(pos);
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getArrayOfUUIDs(int pos) {
    return getUUIDArray(pos);
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   * @deprecated instead use {@link #getArrayOfUUIDs(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getUUIDArray(int pos) {
    return (UUID[]) getValue(pos);
  }

  /**
   * Get an array of {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the column
   * @return the value or {@code null}
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getArrayOfBigDecimals(int pos) {
    return getBigDecimalArray(pos);
  }

  /**
   * Get an array of {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   * @deprecated instead use {@link #getArrayOfBigDecimals(int)}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getBigDecimalArray(int pos) {
    return (BigDecimal[]) getValue(pos);
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
    return addValue(value);
  }

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
   * Add an array of {@code Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfBoolean(Boolean[] value) {
    return addBooleanArray(value);
  }

  /**
   * Add an array of {@code Boolean} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfBoolean(Boolean[])}
   */
  @Deprecated
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
  default Tuple addArrayOfShort(Short[] value) {
    return addShortArray(value);
  }

  /**
   * Add an array of {@link Short} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfShort(Short[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addShortArray(Short[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@code Integer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfInteger(Integer[] value) {
    return addIntegerArray(value);
  }

  /**
   * Add an array of {@code Integer} value at the end of the tuple.
   *
   * <p>Target element instance of {@code Number[]} will be
   * coerced to {@code Integer[]}.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfInteger(Integer[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addIntegerArray(Integer[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLong(Long[] value) {
    return addLongArray(value);
  }

  /**
   * Add an array of {@link Long} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfLong(Long[])}
   */
  @Deprecated
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
  default Tuple addArrayOfFloat(Float[] value) {
    return addFloatArray(value);
  }

  /**
   * Add an array of {@link Float} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfFloat(Float[])}
   */
  @Deprecated
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
  default Tuple addArrayOfDouble(Double[] value) {
    return addDoubleArray(value);
  }

  /**
   * Add an array of {@link Double} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfDouble(Double[])}
   */
  @Deprecated
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
  default Tuple addArrayOfString(String[] value) {
    return addStringArray(value);
  }

  /**
   * Add an array of {@link String} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfString(String[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addStringArray(String[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfTemporal(Temporal[] value) {
    return addTemporalArray(value);
  }

  /**
   * Add an array of {@link Temporal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfTemporal(Temporal[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addTemporalArray(Temporal[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLocalDate(LocalDate[] value) {
    return addLocalDateArray(value);
  }

  /**
   * Add an array of {@link LocalDate} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfLocalDate(LocalDate[])}
   */
  @Deprecated
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
  default Tuple addArrayOfLocalTime(LocalTime[] value) {
    return addLocalTimeArray(value);
  }

  /**
   * Add an array of {@link LocalTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfLocalTime(LocalTime[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalTimeArray(LocalTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfLocalDateTime(LocalDateTime[] value) {
    return addLocalDateTimeArray(value);
  }

  /**
   * Add an array of {@link LocalDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfLocalDateTime(LocalDateTime[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addLocalDateTimeArray(LocalDateTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfOffsetTime(OffsetTime[] value) {
    return addOffsetTimeArray(value);
  }

  /**
   * Add an array of {@link OffsetTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfOffsetTime(OffsetTime[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addOffsetTimeArray(OffsetTime[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfOffsetDateTime(OffsetDateTime[] value) {
    return addOffsetDateTimeArray(value);
  }

  /**
   * Add an array of {@link OffsetDateTime} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfOffsetDateTime(OffsetDateTime[])}
   */
  @Deprecated
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
  default Tuple addArrayOfBuffer(Buffer[] value) {
    return addBufferArray(value);
  }

  /**
   * Add an array of {@link Buffer} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfBuffer(Buffer[])}
   */
  @Deprecated
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
  default Tuple addArrayOfUUID(UUID[] value) {
    return addUUIDArray(value);
  }

  /**
   * Add an array of {@link UUID} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfUUID(UUID[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addUUIDArray(UUID[] value) {
    return addValue(value);
  }

  /**
   * Add an array of {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addArrayOfBigDecimal(BigDecimal[] value) {
    return addBigDecimalArray(value);
  }

  /**
   * Add an array of {@link BigDecimal} value at the end of the tuple.
   *
   * @param value the value
   * @return a reference to this, so the API can be used fluently
   * @deprecated instead use {@link #addArrayOfBigDecimal(BigDecimal[])}
   */
  @Deprecated
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Tuple addBigDecimalArray(BigDecimal[] value) {
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

  /**
   * @return the list of types built from the tuple
   */
  @GenIgnore
  default List<Class<?>> types() {
    int len = size();
    List<Class<?>> types = new ArrayList<>();
    for (int i = 0;i < len;i++) {
      Object param = getValue(i);
      if (param == null) {
        types.add(Object.class);
      } else {
        types.add(param.getClass());
      }
    }
    return types;
  }

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
