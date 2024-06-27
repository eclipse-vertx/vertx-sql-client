/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
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
import io.vertx.sqlclient.internal.ArrayTuple;
import io.vertx.sqlclient.impl.ListTuple;
import io.vertx.core.internal.buffer.BufferInternal;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * A general purpose tuple.
 * <p>
 * <em>CAUTION:</em> indexes start at 0, not at 1.
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
  @SuppressWarnings("unchecked")
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
    ArrayList<Object> list = new ArrayList<>(array.length);
    for (Object o : array) {
      list.add(o);
    }
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
   * @param elt2 the second valueg
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Short) {
      return (Short) val;
    } else if (val instanceof Number) {
      return ((Number) val).shortValue();
    } else if (val instanceof Enum<?>) {
      return (short)((Enum<?>) val).ordinal();
    } else {
      return (Short) val; // Throw CCE
    }
  }

  /**
   * Get an integer value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Integer getInteger(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Integer) {
      return (Integer) val;
    } else if (val instanceof Number) {
      return ((Number) val).intValue();
    } else if (val instanceof Enum<?>) {
      return ((Enum<?>) val).ordinal();
    } else {
      return (Integer) val; // Throw CCE
    }
  }

  /**
   * Get a long value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Long getLong(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Long) {
      return (Long) val;
    } else if (val instanceof Number) {
      return ((Number) val).longValue();
    } else if (val instanceof Enum<?>) {
      return (long)((Enum<?>) val).ordinal();
    } else {
      return (Long) val; // Throw CCE
    }
  }

  /**
   * Get a float value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Float getFloat(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Float) {
      return (Float) val;
    } else if (val instanceof Number) {
      return ((Number) val).floatValue();
    } else if (val instanceof Enum<?>) {
      return (float)((Enum<?>) val).ordinal();
    } else {
      return (Float) val; // Throw CCE
    }
  }

  /**
   * Get a double value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default Double getDouble(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Double) {
      return (Double) val;
    } else if (val instanceof Number) {
      return ((Number) val).doubleValue();
    } else if (val instanceof Enum<?>) {
      return (double)((Enum<?>) val).ordinal();
    } else {
      return (Double) val; // Throw CCE
    }
  }

  /**
   * Get {@link Numeric} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric getNumeric(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Numeric) {
      return (Numeric) val;
    } else if (val instanceof Number) {
      return Numeric.create((Number) val);
    } else {
      return (Numeric) val; // Throw CCE
    }
  }

  /**
   * Get a string value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default String getString(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof String) {
      return (String) val;
    } else if (val instanceof Enum<?>) {
      return ((Enum<?>) val).name();
    } else {
      throw new ClassCastException("Invalid String value type " + val.getClass());
    }
  }

  /**
   * Get a {@link JsonObject} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default JsonObject getJsonObject(int pos) {
    return (JsonObject) getValue(pos);
  }

  /**
   * Get a {@link JsonArray} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  default JsonArray getJsonArray(int pos) {
    return (JsonArray) getValue(pos);
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof LocalDate) {
      return (LocalDate) val;
    } else if (val instanceof LocalDateTime) {
      return ((LocalDateTime) val).toLocalDate();
    } else if (val instanceof OffsetDateTime) {
      return ((OffsetDateTime) val).toLocalDate();
    } else {
      return (LocalDate) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof LocalTime) {
      return (LocalTime) val;
    } else if (val instanceof LocalDateTime) {
      return ((LocalDateTime) val).toLocalTime();
    } else if (val instanceof OffsetDateTime) {
      return ((OffsetDateTime) val).toLocalTime();
    } else {
      return (LocalTime) val; // Throw CCE
    }
  }

  /**
   * Get {@link java.time.LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime getLocalDateTime(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof OffsetDateTime) {
      return ((OffsetDateTime) val).toLocalDateTime();
    } else {
      return (LocalDateTime) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof OffsetTime) {
      return (OffsetTime) val;
    } else if (val instanceof OffsetDateTime) {
      return ((OffsetDateTime)val).toOffsetTime();
    } else {
      return (OffsetTime) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Buffer) {
      return (Buffer) val;
    } else if (val instanceof ByteBuf) {
      return BufferInternal.buffer((ByteBuf) val);
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
   * @return the value
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
   * @return the value
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
   * <p>Target element instance of {@code Object[]} will be
   * coerced to {@code Boolean[]}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Boolean[] getArrayOfBooleans(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Boolean[]) {
      return (Boolean[]) val;
    } else if (val instanceof boolean[]) {
      boolean[] array = (boolean[]) val;
      Boolean[] booleanArray = new Boolean[array.length];
      for (int i = 0;i < array.length;i++) {
        booleanArray[i] = array[i];
      }
      return booleanArray;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Boolean[] booleanArray = new Boolean[array.length];
      for (int i = 0;i < array.length;i++) {
        booleanArray[i] = (Boolean) array[i];
      }
      return booleanArray;
    } else {
      return (Boolean[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Short[]) {
      return (Short[]) val;
    } else if (val instanceof short[]) {
      short[] array = (short[]) val;
      Short[] a = new Short[array.length];
      for (int i = 0;i < array.length;i++) {
        a[i] = array[i];
      }
      return a;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Short[] arr = new Short[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = elt.shortValue();
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Short[] arr = new Short[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = (short)elt.ordinal();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Short[] shortArray = new Short[array.length];
      for (int i = 0;i < array.length;i++) {
        shortArray[i] = ((Number) array[i]).shortValue();
      }
      return shortArray;
    } else {
      return (Short[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Integer[]) {
      return (Integer[]) val;
    } else if (val instanceof int[]) {
      int[] array = (int[]) val;
      Integer[] a = new Integer[array.length];
      for (int i = 0;i < array.length;i++) {
        a[i] = array[i];
      }
      return a;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Integer[] arr = new Integer[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = elt.intValue();
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Integer[] arr = new Integer[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = elt.ordinal();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Integer[] integerArray = new Integer[array.length];
      for (int i = 0;i < array.length;i++) {
        integerArray[i] = ((Number) array[i]).intValue();
      }
      return integerArray;
    } else {
      return (Integer[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Long[]) {
      return (Long[]) val;
    } else if (val instanceof long[]) {
      long[] array = (long[]) val;
      Long[] a = new Long[array.length];
      for (int i = 0;i < array.length;i++) {
        a[i] = array[i];
      }
      return a;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Long[] arr = new Long[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = elt.longValue();
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Long[] arr = new Long[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = (long)elt.ordinal();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Long[] longArray = new Long[array.length];
      for (int i = 0;i < array.length;i++) {
        longArray[i] = ((Number) array[i]).longValue();
      }
      return longArray;
    } else {
      return (Long[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Float[]) {
      return (Float[]) val;
    } else if (val instanceof float[]) {
      float[] array = (float[]) val;
      Float[] a = new Float[array.length];
      for (int i = 0;i < array.length;i++) {
        a[i] = array[i];
      }
      return a;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Float[] arr = new Float[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = elt.floatValue();
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Float[] arr = new Float[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = (float)elt.ordinal();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Float[] floatArray = new Float[array.length];
      for (int i = 0;i < array.length;i++) {
        floatArray[i] = ((Number) array[i]).floatValue();
      }
      return floatArray;
    } else {
      return (Float[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Double[]) {
      return (Double[]) val;
    } else if (val instanceof double[]) {
      double[] array = (double[]) val;
      Double[] a = new Double[array.length];
      for (int i = 0;i < array.length;i++) {
        a[i] = array[i];
      }
      return a;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Double[] arr = new Double[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = elt.doubleValue();
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Double[] arr = new Double[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = (double)elt.ordinal();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Double[] doubleArray = new Double[array.length];
      for (int i = 0;i < array.length;i++) {
        doubleArray[i] = ((Number) array[i]).doubleValue();
      }
      return doubleArray;
    } else {
      return (Double[]) val; // Throw CCE
    }
  }

  /**
   * Get an array of {@link Numeric} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default Numeric[] getArrayOfNumerics(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof Numeric[]) {
      return (Numeric[]) val;
    } else if (val instanceof Number[]) {
      Number[] a = (Number[]) val;
      int len = a.length;
      Numeric[] arr = new Numeric[len];
      for (int i = 0; i < len; i++) {
        Number elt = a[i];
        if (elt != null) {
          arr[i] = Numeric.create(elt);
        }
      }
      return arr;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      Numeric[] arr = new Numeric[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = Numeric.create(elt.ordinal());
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      Numeric[] doubleArray = new Numeric[array.length];
      for (int i = 0;i < array.length;i++) {
        doubleArray[i] = Numeric.create((Number) array[i]);
      }
      return doubleArray;
    } else {
      throw new ClassCastException();
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof String[]) {
      return (String[]) val;
    } else if (val instanceof Enum[]) {
      Enum<?>[] a = (Enum<?>[]) val;
      int len = a.length;
      String[] arr = new String[len];
      for (int i = 0; i < len; i++) {
        Enum<?> elt = a[i];
        if (elt != null) {
          arr[i] = elt.name();
        }
      }
      return arr;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      String[] stringArray = new String[array.length];
      for (int i = 0;i < array.length;i++) {
        stringArray[i] = (String) array[i];
      }
      return stringArray;
    } else {
      return (String[]) val; // Throw CCE
    }
  }

  /**
   * Get an array of  {@link JsonObject} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonObject[] getArrayOfJsonObjects(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      JsonObject[] jsonObjectArray = new JsonObject[array.length];
      for (int i = 0;i < array.length;i++) {
        jsonObjectArray[i] = (JsonObject) array[i];
      }
      return jsonObjectArray;
    } else {
      return (JsonObject[]) val; // Throw CCE
    }
  }

  /**
   * Get an array of  {@link JsonArray} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default JsonArray[] getArrayOfJsonArrays(int pos) {
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val.getClass() == Object[].class) {
      Object[] array = (Object[]) val;
      JsonArray[] jsonObjectArray = new JsonArray[array.length];
      for (int i = 0;i < array.length;i++) {
        jsonObjectArray[i] = (JsonArray) array[i];
      }
      return jsonObjectArray;
    } else {
      return (JsonArray[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof LocalDate[]) {
      return (LocalDate[]) val;
    } else if (val instanceof LocalDateTime[]) {
      LocalDateTime[] a = (LocalDateTime[]) val;
      int len = a.length;
      LocalDate[] arr = new LocalDate[len];
      for (int i = 0; i < len; i++) {
        LocalDateTime elt = a[i];
        if (elt != null) {
          arr[i] = elt.toLocalDate();
        }
      }
      return arr;
    } else {
      return (LocalDate[]) val; // Throw CCE
    }
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof LocalTime[]) {
      return (LocalTime[]) val;
    } else if (val instanceof LocalDateTime[]) {
      LocalDateTime[] a = (LocalDateTime[]) val;
      int len = a.length;
      LocalTime[] arr = new LocalTime[len];
      for (int i = 0; i < len; i++) {
        LocalDateTime elt = a[i];
        if (elt != null) {
          arr[i] = elt.toLocalTime();
        }
      }
      return arr;
    } else {
      return (LocalTime[]) val; // Throw CCE
    }
  }

  /**
   * Get an array of  {@link LocalDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default LocalDateTime[] getArrayOfLocalDateTimes(int pos) {
    return (LocalDateTime[]) getValue(pos);
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
    Object val = getValue(pos);
    if (val == null) {
      return null;
    } else if (val instanceof OffsetTime[]) {
      return (OffsetTime[]) val;
    } else if (val instanceof OffsetDateTime[]) {
      OffsetDateTime[] a = (OffsetDateTime[]) val;
      int len = a.length;
      OffsetTime[] arr = new OffsetTime[len];
      for (int i = 0; i < len; i++) {
        OffsetDateTime elt = a[i];
        if (elt != null) {
          arr[i] = elt.toOffsetTime();
        }
      }
      return arr;
    } else {
      return (OffsetTime[]) val; // Throw CCE
    }
  }

  /**
   * Get an array of  {@link OffsetDateTime} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default OffsetDateTime[] getArrayOfOffsetDateTimes(int pos) {
    return (OffsetDateTime[]) getValue(pos);
  }

  /**
   * Get an array of  {@link Buffer} value at {@code pos}.
   *
   * @param pos the position
   * @return the value
   */
  @GenIgnore
  default Buffer[] getArrayOfBuffers(int pos) {
    return (Buffer[]) getValue(pos);
  }

  /**
   * Get an array of {@link UUID} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default UUID[] getArrayOfUUIDs(int pos) {
    return (UUID[]) getValue(pos);
  }

  /**
   * Get an array of {@link BigDecimal} value at {@code pos}.
   *
   * @param pos the column
   * @return the value
   */
  @GenIgnore(GenIgnore.PERMITTED_TYPE)
  default BigDecimal[] getArrayOfBigDecimals(int pos) {
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
   * Get the the at the specified {@code position} and the specified {@code type}.
   *
   * <p>The type can be one of the types returned by the row (e.g {@code String.class}) or an array
   * of the type (e.g {@code String[].class})).
   *
   * @param type the expected value type
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
