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

package io.vertx.sqlclient.impl;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.NullValue;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.math.BigDecimal;
import java.time.*;
import java.time.temporal.Temporal;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import static org.junit.Assert.*;

@RunWith(Parameterized.class)
public class TupleTest {

  enum TupleKind {

    ELEMENTS() {
      @Override
      Tuple tuple() {
        return Tuple.tuple();
      }

      @Override
      Tuple of(Object... elements) {
        if (elements.length == 0) {
          throw new IllegalArgumentException();
        }
        return Tuple.of(elements[0], Arrays.copyOfRange(elements, 1, elements.length));
      }
    },LIST() {
      @Override
      Tuple tuple() {
        return Tuple.tuple();
      }

      @Override
      Tuple of(Object... elements) {
        if (elements.length == 0) {
          throw new IllegalArgumentException();
        }
        return Tuple.tuple(Arrays.asList(elements));
      }
    }, WRAP_LIST() {
      @Override
      Tuple tuple() {
        return Tuple.wrap(new ArrayList<>());
      }

      @Override
      Tuple of(Object... elements) {
        if (elements.length == 0) {
          throw new IllegalArgumentException();
        }
        return Tuple.wrap(new ArrayList<>(Arrays.asList(elements)));
      }
    }, WRAP_ARRAY() {
      @Override
      Tuple tuple() {
        return Tuple.wrap(new ArrayList<>());
      }

      @Override
      Tuple of(Object... elements) {
        return Tuple.wrap(elements);
      }
    }, FROM_LIST() {
      @Override
      Tuple tuple() {
        return Tuple.from(new ArrayList<>());
      }

      @Override
      Tuple of(Object... elements) {
        if (elements.length == 0) {
          throw new IllegalArgumentException();
        }
        return Tuple.from(new ArrayList<>(Arrays.asList(elements)));
      }
    }, FROM_ARRAY() {
      @Override
      Tuple tuple() {
        return Tuple.from(new Object[0]);
      }

      @Override
      Tuple of(Object... elements) {
        return Tuple.from(elements);
      }
    };

    abstract Tuple tuple();

    abstract Tuple of(Object... elements);
  }

  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
      { TupleKind.ELEMENTS},
      { TupleKind.LIST},
      { TupleKind.WRAP_LIST},
      { TupleKind.WRAP_ARRAY}
    });
  }

  private TupleKind kind;
  private final LocalTime localTime = LocalTime.parse("19:35:58.237666");
  private final OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.UTC);
  private final LocalDate localDate = LocalDate.parse("2017-05-14");
  private final LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
  private final OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
  private final JsonObject jsonObject = new JsonObject().put("msg", "hello");
  private final JsonArray jsonArray = new JsonArray().add(1).add(2);
  private final List<Number> numbers = Arrays.asList((short)3, 4, 5L, 6F, 7D);

  public TupleTest(TupleKind kind) {
    this.kind = kind;
  }

  public Tuple tuple() {
    return kind.tuple();
  }

  public Tuple of(Object... elements) {
    return kind.of(elements);
  }

  @Test
  public void testNumbers() {
    Tuple tuple = of((byte) 127, (short) 4000, 1_000_000, 1_000_000_000L, 4.5F, 4.5D);
    assertEquals(127, (short) tuple.getShort(0));
    assertEquals(4000, (short) tuple.getShort(1));
    assertEquals(4, (short) tuple.getShort(4));
    assertEquals(4, (short) tuple.getShort(5));
    assertEquals(127, (int) tuple.getInteger(0));
    assertEquals(4000, (int) tuple.getInteger(1));
    assertEquals(1_000_000, (int) tuple.getInteger(2));
    assertEquals(1_000_000_000, (int) tuple.getInteger(3));
    assertEquals(4, (int) tuple.getInteger(4));
    assertEquals(4, (int) tuple.getInteger(5));
    assertEquals(127, (long) tuple.getLong(0));
    assertEquals(4000, (long) tuple.getLong(1));
    assertEquals(1_000_000, (long) tuple.getLong(2));
    assertEquals(1_000_000_000, (long) tuple.getLong(3));
    assertEquals(4, (long) tuple.getLong(4));
    assertEquals(4, (long) tuple.getLong(5));
    assertEquals(127, tuple.getFloat(0), 0.0f);
    assertEquals(4000, tuple.getFloat(1), 0.0f);
    assertEquals(1_000_000, tuple.getFloat(2), 0.0f);
    assertEquals(1_000_000_000, tuple.getFloat(3), 0.0f);
    assertEquals(4.5, tuple.getFloat(4), 0.0f);
    assertEquals(4.5, tuple.getFloat(5), 0.0f);
    assertEquals(127, tuple.getDouble(0), 0.0D);
    assertEquals(4000, tuple.getDouble(1), 0.0D);
    assertEquals(1_000_000, tuple.getDouble(2), 0.0D);
    assertEquals(1_000_000_000, tuple.getDouble(3), 0.0D);
    assertEquals(4.5, tuple.getDouble(4), 0.0D);
    assertEquals(4.5, tuple.getDouble(5), 0.0D);

    testNumber(Number::shortValue, Tuple::getShort, Tuple::getArrayOfShorts);
    testNumber(Number::intValue, Tuple::getInteger, Tuple::getArrayOfIntegers);
    testNumber(Number::longValue, Tuple::getLong, Tuple::getArrayOfLongs);
    testNumber(Number::floatValue, Tuple::getFloat, Tuple::getArrayOfFloats);
    testNumber(Number::doubleValue, Tuple::getDouble, Tuple::getArrayOfDoubles);
  }

  private <T extends Number> void testNumber(Function<Number, T> f, BiFunction<Tuple, Integer, T> abc, BiFunction<Tuple, Integer, T[]> def) {
    Tuple tuple;
    for (Number number : numbers) {
      tuple = of(number);
      assertEquals(f.apply(number), abc.apply(tuple, 0));
    }
    tuple = of((Object)numbers.toArray(new Object[0]));
    T[] array = def.apply(tuple, 0);
    assertEquals(5, array.length);
    for (int i = 0;i < array.length;i++) {
      assertEquals(f.apply(numbers.get(i)), array[i]);
    }
  }

  @Test
  public void testAccessors() {
    Tuple tuple = tuple();
    tuple.addBoolean(true);
    tuple.addShort((short) 123);
    tuple.addInteger(12345);
    tuple.addLong((long) 12345678);
    tuple.addFloat(1.23f);
    tuple.addDouble(1.23d);
    tuple.addString("hello");
    tuple.addBuffer(Buffer.buffer("hello"));
    tuple.addTemporal(Instant.MAX);
    tuple.addLocalDate(LocalDate.MAX);
    tuple.addLocalTime(LocalTime.MAX);
    tuple.addLocalDateTime(LocalDateTime.MAX);
    tuple.addOffsetTime(OffsetTime.MAX);
    tuple.addOffsetDateTime(OffsetDateTime.MAX);
    UUID uuid = UUID.randomUUID();
    tuple.addUUID(uuid);
    tuple.addBigDecimal(BigDecimal.ZERO);
    assertEquals(true, tuple.getBoolean(0));
    assertEquals(true, tuple.getValue(0));
    assertEquals(true, tuple.get(Boolean.class, 0));
    assertEquals(123, (short) tuple.getShort(1));
    assertEquals(123, (short) tuple.getValue(1));
    assertEquals(123, (short) tuple.get(Short.class, 1));
    assertEquals(12345, (int) tuple.getInteger(2));
    assertEquals(12345, (int) tuple.getValue(2));
    assertEquals(12345, (int) tuple.get(Integer.class, 2));
    assertEquals(12345678, (long) tuple.getLong(3));
    assertEquals(12345678, (long) tuple.getValue(3));
    assertEquals(12345678, (long) tuple.get(Long.class, 3));
    assertEquals(1.23f, tuple.getFloat(4), 0.0f);
    assertEquals(1.23f, (Float) tuple.getValue(4), 0.0f);
    assertEquals(1.23f, tuple.get(Float.class, 4), 0.0f);
    assertEquals(1.23d, tuple.getDouble(5), 0.0d);
    assertEquals(1.23d, (Double) tuple.getValue(5), 0.0d);
    assertEquals(1.23d, tuple.get(Double.class, 5), 0.0d);
    assertEquals("hello", tuple.getString(6));
    assertEquals("hello", tuple.getValue(6));
    assertEquals("hello", tuple.get(String.class, 6));
    assertEquals(Buffer.buffer("hello"), tuple.getBuffer(7));
    assertEquals(Buffer.buffer("hello"), tuple.getValue(7));
    assertEquals(Buffer.buffer("hello"), tuple.get(Buffer.class, 7));
    assertEquals(Instant.MAX, tuple.getTemporal(8));
    assertEquals(Instant.MAX, tuple.getValue(8));
    assertEquals(Instant.MAX, tuple.get(Temporal.class, 8));
    assertEquals(LocalDate.MAX, tuple.getLocalDate(9));
    assertEquals(LocalDate.MAX, tuple.getValue(9));
    assertEquals(LocalDate.MAX, tuple.get(LocalDate.class, 9));
    assertEquals(LocalTime.MAX, tuple.getLocalTime(10));
    assertEquals(LocalTime.MAX, tuple.getValue(10));
    assertEquals(LocalTime.MAX, tuple.get(LocalTime.class, 10));
    assertEquals(LocalDateTime.MAX, tuple.getLocalDateTime(11));
    assertEquals(LocalDateTime.MAX, tuple.getValue(11));
    assertEquals(LocalDateTime.MAX, tuple.get(LocalDateTime.class, 11));
    assertEquals(OffsetTime.MAX, tuple.getOffsetTime(12));
    assertEquals(OffsetTime.MAX, tuple.getValue(12));
    assertEquals(OffsetTime.MAX, tuple.get(OffsetTime.class, 12));
    assertEquals(OffsetDateTime.MAX, tuple.getOffsetDateTime(13));
    assertEquals(OffsetDateTime.MAX, tuple.getValue(13));
    assertEquals(OffsetDateTime.MAX, tuple.get(OffsetDateTime.class, 13));
    assertEquals(uuid, tuple.getUUID(14));
    assertEquals(uuid, tuple.getValue(14));
    assertEquals(uuid, tuple.get(UUID.class, 14));
    assertEquals(BigDecimal.ZERO, tuple.getBigDecimal(15));
    assertEquals(BigDecimal.ZERO, tuple.getValue(15));
    assertEquals(BigDecimal.ZERO, tuple.get(BigDecimal.class, 15));
    try {
      tuple.get(String.class, 7);
    } catch (IllegalArgumentException e) {
      assertEquals("mismatched type [java.lang.String] for the value of type [io.vertx.core.buffer.impl.BufferImpl]", e.getMessage());
    }
    try {
      tuple.get(null, 1);
    } catch (IllegalArgumentException e) {
      assertEquals("Accessor type can not be null", e.getMessage());
    }
  }

  @Test
  public void testArrayAccessors() {
    Boolean[] booleanArray = new Boolean[]{true};
    Short[] shortArray = new Short[]{(short) 123};
    Integer[] integerArray = new Integer[]{12345};
    Long[] longArray = new Long[]{(long) 12345678};
    Float[] floatArray = new Float[]{1.23f};
    Double[] doubleArray = new Double[]{1.23d};
    String[] stringArray = new String[]{"hello"};
    Buffer[] bufferArray = new Buffer[]{Buffer.buffer("hello")};
    LocalDate[] localDateArray = new LocalDate[]{LocalDate.MAX};
    LocalTime[] localTimeArray = new LocalTime[]{LocalTime.MAX};
    LocalDateTime[] localDateTimeArray = new LocalDateTime[]{LocalDateTime.MAX};
    OffsetTime[] offsetTimeArray = new OffsetTime[]{OffsetTime.MAX};
    OffsetDateTime[] offsetDateTimeArray = new OffsetDateTime[]{OffsetDateTime.MAX};
    UUID[] uuidArray = new UUID[]{UUID.randomUUID()};

    Tuple tuple = tuple();
    tuple.addArrayOfBoolean(booleanArray);
    tuple.addArrayOfShort(shortArray);
    tuple.addArrayOfInteger(integerArray);
    tuple.addArrayOfLong(longArray);
    tuple.addArrayOfFloat(floatArray);
    tuple.addArrayOfDouble(doubleArray);
    tuple.addArrayOfString(stringArray);
    tuple.addArrayOfBuffer(bufferArray);
    tuple.addArrayOfLocalDate(localDateArray);
    tuple.addArrayOfLocalTime(localTimeArray);
    tuple.addArrayOfLocalDateTime(localDateTimeArray);
    tuple.addArrayOfOffsetTime(offsetTimeArray);
    tuple.addArrayOfOffsetDateTime(offsetDateTimeArray);
    tuple.addArrayOfUUID(uuidArray);

    assertArrayEquals(booleanArray, tuple.getArrayOfBooleans(0));
    assertArrayEquals(booleanArray, (Boolean[]) tuple.getValue(0));
    assertArrayEquals(booleanArray, tuple.get(Boolean[].class, 0));
    assertArrayEquals(shortArray, tuple.getArrayOfShorts(1));
    assertArrayEquals(shortArray, (Short[]) tuple.getValue(1));
    assertArrayEquals(shortArray, tuple.get(Short[].class, 1));
    assertArrayEquals(integerArray, tuple.getArrayOfIntegers(2));
    assertArrayEquals(integerArray, (Integer[]) tuple.getValue(2));
    assertArrayEquals(integerArray, tuple.get(Integer[].class, 2));
    assertArrayEquals(longArray, tuple.getArrayOfLongs(3));
    assertArrayEquals(longArray, (Long[]) tuple.getValue(3));
    assertArrayEquals(longArray, (Long[]) tuple.get(Long[].class, 3));
    assertArrayEquals(floatArray, tuple.getArrayOfFloats(4));
    assertArrayEquals(floatArray, (Float[]) tuple.getValue(4));
    assertArrayEquals(floatArray, tuple.get(Float[].class, 4));
    assertArrayEquals(doubleArray, tuple.getArrayOfDoubles(5));
    assertArrayEquals(doubleArray, (Double[]) tuple.getValue(5));
    assertArrayEquals(doubleArray, tuple.get(Double[].class, 5));
    assertArrayEquals(stringArray, tuple.getArrayOfStrings(6));
    assertArrayEquals(stringArray, (String[]) tuple.getValue(6));
    assertArrayEquals(stringArray, tuple.get(String[].class, 6));
    assertArrayEquals(bufferArray, tuple.getArrayOfBuffers(7));
    assertArrayEquals(bufferArray, (Buffer[]) tuple.getValue(7));
    assertArrayEquals(bufferArray, tuple.get(Buffer[].class, 7));
    assertArrayEquals(localDateArray, tuple.getArrayOfLocalDates(8));
    assertArrayEquals(localDateArray, (LocalDate[]) tuple.getValue(8));
    assertArrayEquals(localDateArray, tuple.get(LocalDate[].class, 8));
    assertArrayEquals(localTimeArray, tuple.getArrayOfLocalTimes(9));
    assertArrayEquals(localTimeArray, (LocalTime[]) tuple.getValue(9));
    assertArrayEquals(localTimeArray, tuple.get(LocalTime[].class, 9));
    assertArrayEquals(localDateTimeArray, tuple.getArrayOfLocalDateTimes(10));
    assertArrayEquals(localDateTimeArray, (LocalDateTime[]) tuple.getValue(10));
    assertArrayEquals(localDateTimeArray, tuple.get(LocalDateTime[].class, 10));
    assertArrayEquals(offsetTimeArray, tuple.getArrayOfOffsetTimes(11));
    assertArrayEquals(offsetTimeArray, (Object[]) tuple.getValue(11));
    assertArrayEquals(offsetTimeArray, tuple.get(OffsetTime[].class, 11));
    assertArrayEquals(offsetDateTimeArray, tuple.getArrayOfOffsetDateTimes(12));
    assertArrayEquals(offsetDateTimeArray, (OffsetDateTime[]) tuple.getValue(12));
    assertArrayEquals(offsetDateTimeArray, tuple.get(OffsetDateTime[].class, 12));
    assertArrayEquals(uuidArray, tuple.getArrayOfUUIDs(13));
    assertArrayEquals(uuidArray, (UUID[]) tuple.getValue(13));
    assertArrayEquals(uuidArray, tuple.get(UUID[].class, 13));
    try {
      tuple.get(Buffer[].class, 10);
    } catch (IllegalArgumentException e) {
      assertEquals("mismatched array element type [io.vertx.core.buffer.Buffer] for the value of type [[Ljava.time.LocalDateTime;]", e.getMessage());
    }
    try {
      tuple.get(null, 1);
    } catch (IllegalArgumentException e) {
      assertEquals("Accessor type can not be null", e.getMessage());
    }
  }

  @Test
  public void testShortArrayCoercion() {
    Tuple tuple = of((Object)new Long[]{5L, null, Long.MAX_VALUE});
    Short[] res = tuple.getArrayOfShorts(0);
    assertEquals(3, res.length);
    assertEquals(5, (int)res[0]);
    assertNull(res[1]);
    assertEquals(((Long)(Long.MAX_VALUE)).shortValue(), (int)res[2]);
  }

  @Test
  public void testIntegerArrayCoercion() {
    Tuple tuple = of((Object)new Long[]{5L, null, Long.MAX_VALUE});
    Integer[] res = tuple.getArrayOfIntegers(0);
    assertEquals(3, res.length);
    assertEquals(5, (int)res[0]);
    assertNull(res[1]);
    assertEquals(((Long)(Long.MAX_VALUE)).intValue(), (int)res[2]);
  }

  @Test
  public void testLongArrayCoercion() {
    Tuple tuple = of((Object)new Integer[]{5, null});
    Long[] res = tuple.getArrayOfLongs(0);
    assertEquals(2, res.length);
    assertEquals(5, (long)res[0]);
    assertNull(res[1]);
  }

  @Test
  public void testFloatArrayCoercion() {
    Tuple tuple = of((Object)new Double[]{5D, null, Double.MAX_VALUE});
    Float[] res = tuple.getArrayOfFloats(0);
    assertEquals(3, res.length);
    assertEquals(5, res[0], 0.0);
    assertNull(res[1]);
    assertEquals(((Double)(Double.MAX_VALUE)).floatValue(), res[2], 0.0);
  }

  @Test
  public void testDoubleArrayCoercion() {
    Tuple tuple = of((Object)new Float[]{5F, null, Float.MAX_VALUE});
    Double[] res = tuple.getArrayOfDoubles(0);
    assertEquals(3, res.length);
    assertEquals(5, res[0], 0.0);
    assertNull(res[1]);
    assertEquals(Float.MAX_VALUE, res[2], 0.0);
  }

  @Test
  public void testLocalTimeCoercion() {
    Tuple tuple = of(localDateTime);
    assertEquals(localTime, tuple.getLocalTime(0));
    tuple = of((Object)new LocalDateTime[]{localDateTime});
    LocalTime[] array = tuple.getArrayOfLocalTimes(0);
    assertEquals(1, array.length);
    assertEquals(localTime, array[0]);
  }

  @Test
  public void testLocalDateCoercion() {
    Tuple tuple = of(localDateTime);
    assertEquals(localDate, tuple.getLocalDate(0));
    tuple = of((Object)new LocalDateTime[]{localDateTime});
    LocalDate[] array = tuple.getArrayOfLocalDates(0);
    assertEquals(1, array.length);
    assertEquals(localDate, array[0]);
  }

  @Test
  public void testOffsetTimeCoercion() {
    Tuple tuple = of(offsetDateTime);
    assertEquals(offsetTime, tuple.getOffsetTime(0));
    tuple = of((Object)new OffsetDateTime[]{offsetDateTime});
    OffsetTime[] array = tuple.getArrayOfOffsetTimes(0);
    assertEquals(1, array.length);
    assertEquals(offsetTime, array[0]);
  }

  @Test
  public void testJsonObject() {
    Tuple tuple = of(jsonObject);
    assertEquals(jsonObject, tuple.getJsonObject(0));
    tuple = of((Object)new Object[]{jsonObject});
    JsonObject[] array = tuple.getArrayOfJsonObjects(0);
    assertEquals(1, array.length);
    assertEquals(jsonObject, array[0]);
  }

  @Test
  public void testJsonArray() {
    Tuple tuple = of(jsonArray);
    assertEquals(jsonArray, tuple.getJsonArray(0));
    tuple = of((Object)new Object[]{jsonArray});
    JsonArray[] array = tuple.getArrayOfJsonArrays(0);
    assertEquals(1, array.length);
    assertEquals(jsonArray, array[0]);
  }

  @Test
  public void testString() {
    String expected = "the-string";
    Tuple tuple = of(expected);
    assertEquals(expected, tuple.getString(0));
    tuple = of((Object)new Object[]{expected});
    String[] array = tuple.getArrayOfStrings(0);
    assertEquals(1, array.length);
    assertEquals(expected, array[0]);
  }

  @Test
  public void testBoolean() {
    Tuple tuple = of(true);
    assertEquals(true, tuple.getBoolean(0));
    tuple = of((Object)new Object[]{true,false});
    Boolean[] array = tuple.getArrayOfBooleans(0);
    assertEquals(2, array.length);
    assertEquals(true, array[0]);
    assertEquals(false, array[1]);
  }

  @Test
  public void testWrapList() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    Tuple tuple = Tuple.wrap(list);
    assertEquals(3, tuple.size());
  }

  @Test
  public void testFromListMutation() {
    List<Integer> list = new ArrayList<>();
    list.add(1);
    list.add(2);
    list.add(3);
    Tuple tuple = Tuple.from(list);
    assertEquals(3, tuple.size());
    list.add(4);
    assertEquals(3, tuple.size());
  }

  @Test
  public void testFromArrayMutation() {
    Object[] array = {
      1, 2, 3
    };
    Tuple tuple = Tuple.from(array);
    assertEquals(3, tuple.size());
    assertEquals(1, tuple.getValue(0));
    assertEquals(2, tuple.getValue(1));
    assertEquals(3, tuple.getValue(2));
    array[0] = 10;
    assertEquals(3, tuple.size());
    assertEquals(1, tuple.getValue(0));
    assertEquals(2, tuple.getValue(1));
    assertEquals(3, tuple.getValue(2));
  }

  @Test
  public void testBooleanNullValue() {
    testNullValue(Boolean.class, tuple -> tuple.addBoolean(null), (i, tuple) -> tuple.getBoolean(i));
  }

  @Test
  public void testShortNullValue() {
    testNullValue(Short.class, tuple -> tuple.addShort(null), (i, tuple) -> tuple.getShort(i));
  }

  @Test
  public void testIntegerNullValue() {
    testNullValue(Integer.class, tuple -> tuple.addInteger(null), (i, tuple) -> tuple.getInteger(i));
  }

  @Test
  public void testLongNullValue() {
    testNullValue(Long.class, tuple -> tuple.addLong(null), (i, tuple) -> tuple.getLong(i));
  }

  @Test
  public void testFloatNullValue() {
    testNullValue(Float.class, tuple -> tuple.addFloat(null), (i, tuple) -> tuple.getFloat(i));
  }

  @Test
  public void testDoubleNullValue() {
    testNullValue(Double.class, tuple -> tuple.addDouble(null), (i, tuple) -> tuple.getDouble(i));
  }

  @Test
  public void testStringNullValue() {
    testNullValue(String.class, tuple -> tuple.addString(null), (i, tuple) -> tuple.getString(i));
  }

  @Test
  public void testJsonObjectNullValue() {
    testNullValue(JsonObject.class, tuple -> tuple.addJsonObject(null), (i, tuple) -> tuple.getJsonObject(i));
  }

  @Test
  public void testJsonArrayNullValue() {
    testNullValue(JsonArray.class, tuple -> tuple.addJsonArray(null), (i, tuple) -> tuple.getJsonArray(i));
  }

  @Test
  public void testTemporalNullValue() {
    testNullValue(Temporal.class, tuple -> tuple.addTemporal(null), (i, tuple) -> tuple.getTemporal(i));
  }

  @Test
  public void testLocalDateNullValue() {
    testNullValue(LocalDate.class, tuple -> tuple.addLocalDate(null), (i, tuple) -> tuple.getLocalDate(i));
  }

  @Test
  public void testLocalTimeNullValue() {
    testNullValue(LocalTime.class, tuple -> tuple.addLocalTime(null), (i, tuple) -> tuple.getLocalTime(i));
  }

  @Test
  public void testLocalDateTimeNullValue() {
    testNullValue(LocalDateTime.class, tuple -> tuple.addLocalDateTime(null), (i, tuple) -> tuple.getLocalDateTime(i));
  }

  @Test
  public void testOffsetTimeNullValue() {
    testNullValue(OffsetTime.class, tuple -> tuple.addOffsetTime(null), (i, tuple) -> tuple.getOffsetTime(i));
  }

  @Test
  public void testOffsetDateTimeNullValue() {
    testNullValue(OffsetDateTime.class, tuple -> tuple.addOffsetDateTime(null), (i, tuple) -> tuple.getOffsetDateTime(i));
  }

  @Test
  public void testBufferNullValue() {
    testNullValue(Buffer.class, tuple -> tuple.addBuffer(null), (i, tuple) -> tuple.getBuffer(i));
  }

  @Test
  public void testUUIDNullValue() {
    testNullValue(UUID.class, tuple -> tuple.addUUID(null), (i, tuple) -> tuple.getUUID(i));
  }

  @Test
  public void testBigDecimalNullValue() {
    testNullValue(BigDecimal.class, tuple -> tuple.addBigDecimal(null), (i, tuple) -> tuple.getBigDecimal(i));
  }

  @Test
  public void testEnumNullValue() {
    testNullValue(Object.class, tuple -> tuple.addValue(null), (i, tuple) -> tuple.get(TupleKind.class, i));
  }

  @Test
  public void testArrayOfBooleanNullValue() {
    testNullValue(Boolean[].class, tuple -> tuple.addArrayOfBoolean(null), (i, tuple) -> tuple.getArrayOfBooleans(i));
  }

  @Test
  public void testArrayOfShortNullValue() {
    testNullValue(Short[].class, tuple -> tuple.addArrayOfShort(null), (i, tuple) -> tuple.getArrayOfShorts(i));
  }

  @Test
  public void testArrayOfIntegerNullValue() {
    testNullValue(Integer[].class, tuple -> tuple.addArrayOfInteger(null), (i, tuple) -> tuple.getArrayOfIntegers(i));
  }

  @Test
  public void testArrayOfLongNullValue() {
    testNullValue(Long[].class, tuple -> tuple.addArrayOfLong(null), (i, tuple) -> tuple.getArrayOfLongs(i));
  }

  @Test
  public void testArrayOfFloatNullValue() {
    testNullValue(Float[].class, tuple -> tuple.addArrayOfFloat(null), (i, tuple) -> tuple.getArrayOfFloats(i));
  }

  @Test
  public void testArrayOfDoubleNullValue() {
    testNullValue(Double[].class, tuple -> tuple.addArrayOfDouble(null), (i, tuple) -> tuple.getArrayOfDoubles(i));
  }

  @Test
  public void testArrayOfStringNullValue() {
    testNullValue(String[].class, tuple -> tuple.addArrayOfString(null), (i, tuple) -> tuple.getArrayOfStrings(i));
  }

  @Test
  public void testArrayOfJsonObjectNullValue() {
    testNullValue(JsonObject[].class, tuple -> tuple.addArrayOfJsonObject(null), (i, tuple) -> tuple.getArrayOfJsonObjects(i));
  }

  @Test
  public void testArrayOfJsonArrayNullValue() {
    testNullValue(JsonArray[].class, tuple -> tuple.addArrayOfJsonArray(null), (i, tuple) -> tuple.getArrayOfJsonArrays(i));
  }

  @Test
  public void testArrayOfTemporalNullValue() {
    testNullValue(Temporal[].class, tuple -> tuple.addArrayOfTemporal(null), (i, tuple) -> tuple.getArrayOfTemporals(i));
  }

  @Test
  public void testArrayOfLocalDateNullValue() {
    testNullValue(LocalDate[].class, tuple -> tuple.addArrayOfLocalDate(null), (i, tuple) -> tuple.getArrayOfLocalDates(i));
  }

  @Test
  public void testArrayOfLocalTimeNullValue() {
    testNullValue(LocalTime[].class, tuple -> tuple.addArrayOfLocalTime(null), (i, tuple) -> tuple.getArrayOfLocalTimes(i));
  }

  @Test
  public void testArrayOfLocalDateTimeNullValue() {
    testNullValue(LocalDateTime[].class, tuple -> tuple.addArrayOfLocalDateTime(null), (i, tuple) -> tuple.getArrayOfLocalDateTimes(i));
  }

  @Test
  public void testArrayOfOffsetTimeNullValue() {
    testNullValue(OffsetTime[].class, tuple -> tuple.addArrayOfOffsetTime(null), (i, tuple) -> tuple.getArrayOfOffsetTimes(i));
  }

  @Test
  public void testArrayOfOffsetDateTimeNullValue() {
    testNullValue(OffsetDateTime[].class, tuple -> tuple.addArrayOfOffsetDateTime(null), (i, tuple) -> tuple.getArrayOfOffsetDateTimes(i));
  }

  @Test
  public void testArrayOfBufferNullValue() {
    testNullValue(Buffer[].class, tuple -> tuple.addArrayOfBuffer(null), (i, tuple) -> tuple.getArrayOfBuffers(i));
  }

  @Test
  public void testArrayOfUUIDNullValue() {
    testNullValue(UUID[].class, tuple -> tuple.addArrayOfUUID(null), (i, tuple) -> tuple.getArrayOfUUIDs(i));
  }

  @Test
  public void testArrayOfBigDecimalNullValue() {
    testNullValue(BigDecimal[].class, tuple -> tuple.addArrayOfBigDecimal(null), (i, tuple) -> tuple.getArrayOfBigDecimals(i));
  }

  @Test
  public void testArbitraryClassNullValue() {
    testNullValue(LinkedHashSet.class, tuple -> tuple.addValue(NullValue.of(LinkedHashSet.class)), (i, tuple) -> tuple.getValue(i));
  }

  private void testNullValue(Class<?> c, Consumer<Tuple> addValue, BiFunction<Integer, Tuple, ?> getValue) {
    Tuple tuple = tuple();
    addValue.accept(tuple);
    assertNull(tuple.getValue(0));
    assertNull(getValue.apply(0, tuple));
    assertNull(tuple.get(c, 0));
    assertSame(c, tuple.types().get(0));
  }
}
