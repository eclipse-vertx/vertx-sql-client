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

package io.vertx.sqlclient.template;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.template.wrappers.BooleanWrapper;
import io.vertx.sqlclient.template.wrappers.DoubleWrapper;
import io.vertx.sqlclient.template.wrappers.FloatWrapper;
import io.vertx.sqlclient.template.wrappers.IntegerWrapper;
import io.vertx.sqlclient.template.wrappers.JsonArrayWrapper;
import io.vertx.sqlclient.template.wrappers.JsonObjectWrapper;
import io.vertx.sqlclient.template.wrappers.LongWrapper;
import io.vertx.sqlclient.template.wrappers.ShortWrapper;
import io.vertx.sqlclient.template.wrappers.StringWrapper;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DataObjectTypesTest extends PgTemplateTestBase {

  private final LocalTime localTime = LocalTime.parse("19:35:58.237666");
  private final OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.UTC);
  private final LocalDate localDate = LocalDate.parse("2017-05-14");
  private final LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
  private final OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
  private final UUID uuid = UUID.randomUUID();
  private final String string = "some-text";
  private final Buffer buffer = Buffer.buffer(string);
  private final JsonObject jsonObject = new JsonObject().put("string", "str-value").put("number", 1234);
  private final JsonArray jsonArray = new JsonArray().add(1).add(2).add(3);

  @Test
  public void testBoolean(TestContext ctx) {
    testGet(ctx, "BOOLEAN", true, true, "primitiveBoolean", TestDataObject::isPrimitiveBoolean);
    testGet(ctx, "BOOLEAN", null, false, "primitiveBoolean", TestDataObject::isPrimitiveBoolean);
    testGet(ctx, "BOOLEAN", true, true, "boxedBoolean", TestDataObject::isBoxedBoolean);
    testGet(ctx, "BOOLEAN[]", new Boolean[]{false, true}, Arrays.asList(false, true), "booleanList", TestDataObject::getBooleanList);
    testGet(ctx, "BOOLEAN[]", new Boolean[]{false, true}, new HashSet<>(Arrays.asList(false, true)), "booleanSet", TestDataObject::getBooleanSet);
    testGet(ctx, "BOOLEAN[]", new Boolean[]{false, true}, Arrays.asList(false, true), "addedBooleans", TestDataObject::getAddedBooleans);
  }

  @Test
  public void testShort(TestContext ctx) {
    testNumber(ctx, "INT2", (short)4);
    testNumberArray(ctx, "INT2[]", new Short[]{1,2,3});
  }

  @Test
  public void testInteger(TestContext ctx) {
    testNumber(ctx, "INT4", 4);
    testNumberArray(ctx, "INT4[]", new Integer[]{1,2,3});
  }

  @Test
  public void testLong(TestContext ctx) {
    testNumber(ctx, "INT8", 4L);
    testNumberArray(ctx, "INT8[]", new Long[]{1L,2L,3L});
  }

  @Test
  public void testFloat(TestContext ctx) {
    testNumber(ctx, "FLOAT4", 4F);
    testNumberArray(ctx, "FLOAT4[]", new Float[]{1F,2F,3F});
  }

  @Test
  public void testDouble(TestContext ctx) {
    testNumber(ctx, "FLOAT8", 4D);
    testNumberArray(ctx, "FLOAT8[]", new Double[]{1D,2D,3D});
  }

  private void testNumber(TestContext ctx, String sqlType, Object value) {
    testNumber(ctx, sqlType, value, (short)4, "primitiveShort", TestDataObject::getPrimitiveShort);
    testNumber(ctx, sqlType,value, 4, "primitiveInt", TestDataObject::getPrimitiveInt);
    testNumber(ctx, sqlType,value, 4L, "primitiveLong", TestDataObject::getPrimitiveLong);
    testNumber(ctx, sqlType,value, 4F, "primitiveFloat", TestDataObject::getPrimitiveFloat);
    testNumber(ctx, sqlType,value, 4D, "primitiveDouble", TestDataObject::getPrimitiveDouble);
    testNumber(ctx, sqlType, value, (short)4, "boxedShort", TestDataObject::getBoxedShort);
    testNumber(ctx, sqlType,value, 4, "boxedInteger", TestDataObject::getBoxedInteger);
    testNumber(ctx, sqlType,value, 4L, "boxedLong", TestDataObject::getBoxedLong);
    testNumber(ctx, sqlType,value, 4F, "boxedFloat", TestDataObject::getBoxedFloat);
    testNumber(ctx, sqlType,value, 4D, "boxedDouble", TestDataObject::getBoxedDouble);
  }

  private void testNumberArray(TestContext ctx, String sqlType, Object value) {
    testNumber(ctx, sqlType, value, Arrays.asList((short)1,(short)2,(short)3), "shortList", TestDataObject::getShortList);
    testNumber(ctx, sqlType, value, Arrays.asList(1,2,3), "integerList", TestDataObject::getIntegerList);
    testNumber(ctx, sqlType, value, Arrays.asList(1L,2L,3L), "longList", TestDataObject::getLongList);
    testNumber(ctx, sqlType, value, Arrays.asList(1F,2F,3F), "floatList", TestDataObject::getFloatList);
    testNumber(ctx, sqlType, value, Arrays.asList(1D,2D,3D), "doubleList", TestDataObject::getDoubleList);
    testNumber(ctx, sqlType, value, new HashSet<>(Arrays.asList((short)1,(short)2,(short)3)), "shortSet", TestDataObject::getShortSet);
    testNumber(ctx, sqlType, value, new HashSet<>(Arrays.asList(1,2,3)), "integerSet", TestDataObject::getIntegerSet);
    testNumber(ctx, sqlType, value, new HashSet<>(Arrays.asList(1L,2L,3L)), "longSet", TestDataObject::getLongSet);
    testNumber(ctx, sqlType, value, new HashSet<>(Arrays.asList(1F,2F,3F)), "floatSet", TestDataObject::getFloatSet);
    testNumber(ctx, sqlType, value, new HashSet<>(Arrays.asList(1D,2D,3D)), "doubleSet", TestDataObject::getDoubleSet);
    testNumber(ctx, sqlType, value, Arrays.asList((short)1,(short)2,(short)3), "addedShorts", TestDataObject::getAddedShorts);
    testNumber(ctx, sqlType, value, Arrays.asList(1,2,3), "addedIntegers", TestDataObject::getAddedIntegers);
    testNumber(ctx, sqlType, value, Arrays.asList(1L,2L,3L), "addedLongs", TestDataObject::getAddedLongs);
    testNumber(ctx, sqlType, value, Arrays.asList(1F,2F,3F), "addedFloats", TestDataObject::getAddedFloats);
    testNumber(ctx, sqlType, value, Arrays.asList(1D,2D,3D), "addedDoubles", TestDataObject::getAddedDoubles);
  }

  private <I, O> void testNumber(TestContext ctx, String sqlType, I value, O expected, String column, Function<TestDataObject, O> getter) {
    Async async = ctx.async();
    SqlTemplate<Map<String, Object>, RowSet<TestDataObject>> template = SqlTemplate
      .forQuery(connection, "SELECT ${value} :: " + sqlType + " \"" + column + "\"")
      .mapTo(TestDataObjectRowMapper.INSTANCE);
    template.execute(Collections.singletonMap("value", value), ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      ctx.assertEquals(expected, getter.apply(result.iterator().next()));
      async.complete();
    }));
    async.await(10000);
  }

  @Test
  public void testString(TestContext ctx) {
    testGet(ctx, "VARCHAR", string, string, "string", TestDataObject::getString);
    testGet(ctx, "VARCHAR[]", new String[]{string}, Collections.singletonList(string), "stringList", TestDataObject::getStringList);
    testGet(ctx, "VARCHAR[]", new String[]{string}, Collections.singleton(string), "stringSet", TestDataObject::getStringSet);
    testGet(ctx, "VARCHAR[]", new String[]{string}, Collections.singletonList(string), "addedStrings", TestDataObject::getAddedStrings);
  }

  @Test
  public void testEnum(TestContext ctx) {
    testGet(ctx, "VARCHAR", TimeUnit.MILLISECONDS.name(), TimeUnit.MILLISECONDS, "timeUnit", TestDataObject::getTimeUnit);
    testGet(ctx, "VARCHAR[]", new String[]{TimeUnit.MILLISECONDS.name()}, Collections.singletonList(TimeUnit.MILLISECONDS), "timeUnitList", TestDataObject::getTimeUnitList);
    testGet(ctx, "VARCHAR[]", new String[]{TimeUnit.MILLISECONDS.name()}, Collections.singleton(TimeUnit.MILLISECONDS), "timeUnitSet", TestDataObject::getTimeUnitSet);
    testGet(ctx, "VARCHAR[]", new String[]{TimeUnit.MILLISECONDS.name()}, Collections.singletonList(TimeUnit.MILLISECONDS), "addedTimeUnits", TestDataObject::getAddedTimeUnits);
  }

  @Test
  public void testJsonObject(TestContext ctx) {
    testGet(ctx, "JSON", jsonObject, jsonObject, "jsonObject", TestDataObject::getJsonObject);
    testGet(ctx, "JSON[]", new JsonObject[]{jsonObject}, Collections.singletonList(jsonObject), "jsonObjectList", TestDataObject::getJsonObjectList);
    testGet(ctx, "JSON[]", new JsonObject[]{jsonObject}, Collections.singleton(jsonObject), "jsonObjectSet", TestDataObject::getJsonObjectSet);
    testGet(ctx, "JSON[]", new JsonObject[]{jsonObject}, Collections.singletonList(jsonObject), "addedJsonObjects", TestDataObject::getAddedJsonObjects);
  }

  @Test
  public void testJsonArray(TestContext ctx) {
    testGet(ctx, "JSON", jsonArray, jsonArray, "jsonArray", TestDataObject::getJsonArray);
    testGet(ctx, "JSON[]", new JsonArray[]{jsonArray}, Collections.singletonList(jsonArray), "jsonArrayList", TestDataObject::getJsonArrayList);
    testGet(ctx, "JSON[]", new JsonArray[]{jsonArray}, Collections.singleton(jsonArray), "jsonArraySet", TestDataObject::getJsonArraySet);
    testGet(ctx, "JSON[]", new JsonArray[]{jsonArray}, Collections.singletonList(jsonArray), "addedJsonArrays", TestDataObject::getAddedJsonArrays);
  }

  @Test
  public void testBuffer(TestContext ctx) {
    testGet(ctx, "BYTEA", buffer, buffer, "buffer", TestDataObject::getBuffer);
    testGet(ctx, "BYTEA[]", new Buffer[]{buffer}, Collections.singletonList(buffer), "bufferList", TestDataObject::getBufferList);
    testGet(ctx, "BYTEA[]", new Buffer[]{buffer}, Collections.singleton(buffer), "bufferSet", TestDataObject::getBufferSet);
    testGet(ctx, "BYTEA[]", new Buffer[]{buffer}, Collections.singletonList(buffer), "addedBuffers", TestDataObject::getAddedBuffers);
  }

  @Test
  public void testUUID(TestContext ctx) {
    testGet(ctx, "UUID", uuid, uuid, "uuid", TestDataObject::getUUID);
    testGet(ctx, "UUID[]", new UUID[]{uuid}, Collections.singletonList(uuid), "uuidList", TestDataObject::getUUIDList);
    testGet(ctx, "UUID[]", new UUID[]{uuid}, Collections.singleton(uuid), "uuidSet", TestDataObject::getUUIDSet);
    testGet(ctx, "UUID[]", new UUID[]{uuid}, Collections.singletonList(uuid), "addedUUIDs", TestDataObject::getAddedUUIDs);
  }

  @Test
  public void testLocalDateTime(TestContext ctx) {
    testGet(ctx, "TIMESTAMP", localDateTime, localDateTime, "localDateTime", TestDataObject::getLocalDateTime);
    testGet(ctx, "TIMESTAMP[]", new LocalDateTime[]{localDateTime}, Collections.singletonList(localDateTime), "localDateTimeList", TestDataObject::getLocalDateTimeList);
  }

  @Test
  public void testLocalDate(TestContext ctx) {
    testGet(ctx, "DATE", localDate, localDate, "localDate", TestDataObject::getLocalDate);
    testGet(ctx, "TIMESTAMP", localDateTime, localDate, "localDate", TestDataObject::getLocalDate);
    testGet(ctx, "DATE[]", new LocalDate[]{localDate}, Collections.singletonList(localDate), "localDateList", TestDataObject::getLocalDateList);
    testGet(ctx, "TIMESTAMP[]", new LocalDateTime[] {localDateTime}, Collections.singletonList(localDate), "localDateList", TestDataObject::getLocalDateList);
  }

  @Test
  public void testLocalTime(TestContext ctx) {
    testGet(ctx, "TIME", localTime, localTime, "localTime", TestDataObject::getLocalTime);
    testGet(ctx, "TIMESTAMP", localDateTime, localTime, "localTime", TestDataObject::getLocalTime);
    testGet(ctx, "TIME[]", new LocalTime[]{localTime}, Collections.singletonList(localTime), "localTimeList", TestDataObject::getLocalTimeList);
    testGet(ctx, "TIMESTAMP[]", new LocalDateTime[] {localDateTime}, Collections.singletonList(localTime), "localTimeList", TestDataObject::getLocalTimeList);
  }

  @Test
  public void testOffsetDateTime(TestContext ctx) {
    testGet(ctx, "TIMESTAMPTZ", offsetDateTime, offsetDateTime, "offsetDateTime", TestDataObject::getOffsetDateTime);
    testGet(ctx, "TIMESTAMPTZ[]", new OffsetDateTime[]{offsetDateTime}, Collections.singletonList(offsetDateTime), "offsetDateTimeList", TestDataObject::getOffsetDateTimeList);
  }

  @Test
  public void testOffsetTime(TestContext ctx) {
    testGet(ctx, "TIMETZ", offsetTime, offsetTime, "offsetTime", TestDataObject::getOffsetTime);
    testGet(ctx, "TIMESTAMPTZ", offsetDateTime, offsetTime, "offsetTime", TestDataObject::getOffsetTime);
    testGet(ctx, "TIMETZ[]", new OffsetTime[]{offsetTime}, Collections.singletonList(offsetTime), "offsetTimeList", TestDataObject::getOffsetTimeList);
    testGet(ctx, "TIMESTAMPTZ[]", new OffsetDateTime[]{offsetDateTime}, Collections.singletonList(offsetTime), "offsetTimeList", TestDataObject::getOffsetTimeList);
  }

  @Test
  public void testTemporal(TestContext ctx) {
    testGet(ctx, "DATE", localDate, localDate, "temporal", TestDataObject::getTemporal);
    testGet(ctx, "TIME", localTime, localTime, "temporal", TestDataObject::getTemporal);
    testGet(ctx, "TIMETZ", offsetTime, offsetTime, "temporal", TestDataObject::getTemporal);
    testGet(ctx, "TIMESTAMP", localDateTime, localDateTime, "temporal", TestDataObject::getTemporal);
    testGet(ctx, "TIMESTAMPTZ", offsetDateTime, offsetDateTime, "temporal", TestDataObject::getTemporal);
    testGet(ctx, "DATE[]", new LocalDate[]{localDate}, Collections.singletonList(localDate), "temporalList", TestDataObject::getTemporalList);
    testGet(ctx, "TIME[]", new LocalTime[]{localTime}, Collections.singletonList(localTime), "temporalList", TestDataObject::getTemporalList);
    testGet(ctx, "TIMETZ[]", new OffsetTime[]{offsetTime}, Collections.singletonList(offsetTime), "temporalList", TestDataObject::getTemporalList);
    testGet(ctx, "TIMESTAMP[]", new LocalDateTime[]{localDateTime}, Collections.singletonList(localDateTime), "temporalList", TestDataObject::getTemporalList);
    testGet(ctx, "TIMESTAMPTZ[]", new OffsetDateTime[]{offsetDateTime}, Collections.singletonList(offsetDateTime), "temporalList", TestDataObject::getTemporalList);
  }

  @Test
  public void testJsonObjectDataObject(TestContext ctx) {
    testGet(ctx, "JSON", jsonObject, new JsonObjectDataObject(jsonObject), "jsonObjectDataObject", TestDataObject::getJsonObjectDataObject);
    testGet(ctx, "JSON[]", new Object[]{jsonObject}, Collections.singletonList(new JsonObjectDataObject(jsonObject)), "jsonObjectDataObjectList", TestDataObject::getJsonObjectDataObjectList);
    testGet(ctx, "JSON[]", new Object[]{jsonObject}, Collections.singleton(new JsonObjectDataObject(jsonObject)), "jsonObjectDataObjectSet", TestDataObject::getJsonObjectDataObjectSet);
    testGet(ctx, "JSON[]", new Object[]{jsonObject}, Collections.singletonList(new JsonObjectDataObject(jsonObject)), "addedJsonObjectDataObjects", TestDataObject::getAddedJsonObjectDataObjects);
  }

  @Test
  public void testBooleanMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "BOOLEAN", true, new BooleanWrapper(true), "booleanMethodMappedDataObject", TestDataObject::getBooleanMethodMappedDataObject);
    testGet(ctx, "BOOLEAN[]", new Boolean[] {true}, Collections.singletonList(new BooleanWrapper(true)), "booleanMethodMappedDataObjectList", TestDataObject::getBooleanMethodMappedDataObjectList);
    testGet(ctx, "BOOLEAN[]", new Boolean[] {true}, Collections.singleton(new BooleanWrapper(true)), "booleanMethodMappedDataObjectSet", TestDataObject::getBooleanMethodMappedDataObjectSet);
    testGet(ctx, "BOOLEAN[]", new Boolean[] {true}, Collections.singletonList(new BooleanWrapper(true)), "addedBooleanMethodMappedDataObjects", TestDataObject::getAddedBooleanMethodMappedDataObjects);
    testGet(ctx, "JSON", true, new BooleanWrapper(true), "booleanMethodMappedDataObject", TestDataObject::getBooleanMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {true}, Collections.singletonList(new BooleanWrapper(true)), "booleanMethodMappedDataObjectList", TestDataObject::getBooleanMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {true}, Collections.singleton(new BooleanWrapper(true)), "booleanMethodMappedDataObjectSet", TestDataObject::getBooleanMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {true}, Collections.singletonList(new BooleanWrapper(true)), "addedBooleanMethodMappedDataObjects", TestDataObject::getAddedBooleanMethodMappedDataObjects);
  }

  @Test
  public void testShortMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "INT2", 4, new ShortWrapper((short)4), "shortMethodMappedDataObject", TestDataObject::getShortMethodMappedDataObject);
    testGet(ctx, "INT2[]", new Short[] {4}, Collections.singletonList(new ShortWrapper((short)4)), "shortMethodMappedDataObjectList", TestDataObject::getShortMethodMappedDataObjectList);
    testGet(ctx, "INT2[]", new Short[] {4}, Collections.singleton(new ShortWrapper((short)4)), "shortMethodMappedDataObjectSet", TestDataObject::getShortMethodMappedDataObjectSet);
    testGet(ctx, "INT2[]", new Short[] {4}, Collections.singletonList(new ShortWrapper((short)4)), "addedShortMethodMappedDataObjects", TestDataObject::getAddedShortMethodMappedDataObjects);
    testGet(ctx, "JSON", 4, new ShortWrapper((short)4), "shortMethodMappedDataObject", TestDataObject::getShortMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singletonList(new ShortWrapper((short)4)), "shortMethodMappedDataObjectList", TestDataObject::getShortMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singleton(new ShortWrapper((short)4)), "shortMethodMappedDataObjectSet", TestDataObject::getShortMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singletonList(new ShortWrapper((short)4)), "addedShortMethodMappedDataObjects", TestDataObject::getAddedShortMethodMappedDataObjects);
  }

  @Test
  public void testIntegerMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "INT4", 4, new IntegerWrapper(4), "integerMethodMappedDataObject", TestDataObject::getIntegerMethodMappedDataObject);
    testGet(ctx, "INT4[]", new Integer[] {4}, Collections.singletonList(new IntegerWrapper(4)), "integerMethodMappedDataObjectList", TestDataObject::getIntegerMethodMappedDataObjectList);
    testGet(ctx, "INT4[]", new Integer[] {4}, Collections.singleton(new IntegerWrapper(4)), "integerMethodMappedDataObjectSet", TestDataObject::getIntegerMethodMappedDataObjectSet);
    testGet(ctx, "INT4[]", new Integer[] {4}, Collections.singletonList(new IntegerWrapper(4)), "addedIntegerMethodMappedDataObjects", TestDataObject::getAddedIntegerMethodMappedDataObjects);
    testGet(ctx, "JSON", 4, new IntegerWrapper(4), "integerMethodMappedDataObject", TestDataObject::getIntegerMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singletonList(new IntegerWrapper(4)), "integerMethodMappedDataObjectList", TestDataObject::getIntegerMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singleton(new IntegerWrapper(4)), "integerMethodMappedDataObjectSet", TestDataObject::getIntegerMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {4}, Collections.singletonList(new IntegerWrapper(4)), "addedIntegerMethodMappedDataObjects", TestDataObject::getAddedIntegerMethodMappedDataObjects);
  }

  @Test
  public void testLongMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "INT8", 4, new LongWrapper(4L), "longMethodMappedDataObject", TestDataObject::getLongMethodMappedDataObject);
    testGet(ctx, "INT8[]", new Long[] {4L}, Collections.singletonList(new LongWrapper(4L)), "longMethodMappedDataObjectList", TestDataObject::getLongMethodMappedDataObjectList);
    testGet(ctx, "INT8[]", new Long[] {4L}, Collections.singleton(new LongWrapper(4L)), "longMethodMappedDataObjectSet", TestDataObject::getLongMethodMappedDataObjectSet);
    testGet(ctx, "INT8[]", new Long[] {4L}, Collections.singletonList(new LongWrapper(4L)), "addedLongMethodMappedDataObjects", TestDataObject::getAddedLongMethodMappedDataObjects);
    testGet(ctx, "JSON", 4, new LongWrapper(4L), "longMethodMappedDataObject", TestDataObject::getLongMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {4L}, Collections.singletonList(new LongWrapper(4L)), "longMethodMappedDataObjectList", TestDataObject::getLongMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {4L}, Collections.singleton(new LongWrapper(4L)), "longMethodMappedDataObjectSet", TestDataObject::getLongMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {4L}, Collections.singletonList(new LongWrapper(4L)), "addedLongMethodMappedDataObjects", TestDataObject::getAddedLongMethodMappedDataObjects);
  }

  @Test
  public void testFloatMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "FLOAT4", 4F, new FloatWrapper(4F), "floatMethodMappedDataObject", TestDataObject::getFloatMethodMappedDataObject);
    testGet(ctx, "FLOAT4[]", new Float[] {4F}, Collections.singletonList(new FloatWrapper(4F)), "floatMethodMappedDataObjectList", TestDataObject::getFloatMethodMappedDataObjectList);
    testGet(ctx, "FLOAT4[]", new Float[] {4F}, Collections.singleton(new FloatWrapper(4F)), "floatMethodMappedDataObjectSet", TestDataObject::getFloatMethodMappedDataObjectSet);
    testGet(ctx, "FLOAT4[]", new Float[] {4F}, Collections.singletonList(new FloatWrapper(4F)), "addedFloatMethodMappedDataObjects", TestDataObject::getAddedFloatMethodMappedDataObjects);
    testGet(ctx, "JSON", 4, new FloatWrapper(4F), "floatMethodMappedDataObject", TestDataObject::getFloatMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {4F}, Collections.singletonList(new FloatWrapper(4F)), "floatMethodMappedDataObjectList", TestDataObject::getFloatMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {4F}, Collections.singleton(new FloatWrapper(4F)), "floatMethodMappedDataObjectSet", TestDataObject::getFloatMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {4F}, Collections.singletonList(new FloatWrapper(4F)), "addedFloatMethodMappedDataObjects", TestDataObject::getAddedFloatMethodMappedDataObjects);
  }

  @Test
  public void testDoubleMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "FLOAT8", 4D, new DoubleWrapper(4D), "doubleMethodMappedDataObject", TestDataObject::getDoubleMethodMappedDataObject);
    testGet(ctx, "FLOAT8[]", new Double[] {4D}, Collections.singletonList(new DoubleWrapper(4D)), "doubleMethodMappedDataObjectList", TestDataObject::getDoubleMethodMappedDataObjectList);
    testGet(ctx, "FLOAT8[]", new Double[] {4D}, Collections.singleton(new DoubleWrapper(4D)), "doubleMethodMappedDataObjectSet", TestDataObject::getDoubleMethodMappedDataObjectSet);
    testGet(ctx, "FLOAT8[]", new Double[] {4D}, Collections.singletonList(new DoubleWrapper(4D)), "addedDoubleMethodMappedDataObjects", TestDataObject::getAddedDoubleMethodMappedDataObjects);
    testGet(ctx, "JSON", 4, new DoubleWrapper(4D), "doubleMethodMappedDataObject", TestDataObject::getDoubleMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {4D}, Collections.singletonList(new DoubleWrapper(4D)), "doubleMethodMappedDataObjectList", TestDataObject::getDoubleMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {4D}, Collections.singleton(new DoubleWrapper(4D)), "doubleMethodMappedDataObjectSet", TestDataObject::getDoubleMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {4D}, Collections.singletonList(new DoubleWrapper(4D)), "addedDoubleMethodMappedDataObjects", TestDataObject::getAddedDoubleMethodMappedDataObjects);
  }

  @Test
  public void testStringMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "VARCHAR", "the-string", new StringWrapper("the-string"), "stringMethodMappedDataObject", TestDataObject::getStringMethodMappedDataObject);
    testGet(ctx, "VARCHAR[]", new String[] {"the-string"}, Collections.singletonList(new StringWrapper("the-string")), "stringMethodMappedDataObjectList", TestDataObject::getStringMethodMappedDataObjectList);
    testGet(ctx, "VARCHAR[]", new String[] {"the-string"}, Collections.singleton(new StringWrapper("the-string")), "stringMethodMappedDataObjectSet", TestDataObject::getStringMethodMappedDataObjectSet);
    testGet(ctx, "VARCHAR[]", new String[] {"the-string"}, Collections.singletonList(new StringWrapper("the-string")), "addedStringMethodMappedDataObjects", TestDataObject::getAddedStringMethodMappedDataObjects);
    testGet(ctx, "JSON", "the-string", new StringWrapper("the-string"), "stringMethodMappedDataObject", TestDataObject::getStringMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {"the-string"}, Collections.singletonList(new StringWrapper("the-string")), "stringMethodMappedDataObjectList", TestDataObject::getStringMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {"the-string"}, Collections.singleton(new StringWrapper("the-string")), "stringMethodMappedDataObjectSet", TestDataObject::getStringMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {"the-string"}, Collections.singletonList(new StringWrapper("the-string")), "addedStringMethodMappedDataObjects", TestDataObject::getAddedStringMethodMappedDataObjects);
  }

  @Test
  public void testJsonObjectMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "JSON", jsonObject, new JsonObjectWrapper(jsonObject), "jsonObjectMethodMappedDataObject", TestDataObject::getJsonObjectMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {jsonObject}, Collections.singletonList(new JsonObjectWrapper(jsonObject)), "jsonObjectMethodMappedDataObjectList", TestDataObject::getJsonObjectMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {jsonObject}, Collections.singleton(new JsonObjectWrapper(jsonObject)), "jsonObjectMethodMappedDataObjectSet", TestDataObject::getJsonObjectMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {jsonObject}, Collections.singletonList(new JsonObjectWrapper(jsonObject)), "addedJsonObjectMethodMappedDataObjects", TestDataObject::getAddedJsonObjectMethodMappedDataObjects);
  }

  @Test
  public void testJsonArrayMethodMappedDataObject(TestContext ctx) {
    testGet(ctx, "JSON", jsonArray, new JsonArrayWrapper(jsonArray), "jsonArrayMethodMappedDataObject", TestDataObject::getJsonArrayMethodMappedDataObject);
    testGet(ctx, "JSON[]", new Object[] {jsonArray}, Collections.singletonList(new JsonArrayWrapper(jsonArray)), "jsonArrayMethodMappedDataObjectList", TestDataObject::getJsonArrayMethodMappedDataObjectList);
    testGet(ctx, "JSON[]", new Object[] {jsonArray}, Collections.singleton(new JsonArrayWrapper(jsonArray)), "jsonArrayMethodMappedDataObjectSet", TestDataObject::getJsonArrayMethodMappedDataObjectSet);
    testGet(ctx, "JSON[]", new Object[] {jsonArray}, Collections.singletonList(new JsonArrayWrapper(jsonArray)), "addedJsonArrayMethodMappedDataObjects", TestDataObject::getAddedJsonArrayMethodMappedDataObjects);
  }

  private <P, V> void testGet(TestContext ctx, String sqlType, P param, V value, String column, Function<TestDataObject, V> getter) {
    super.testGet(
      ctx,
      sqlType,
      TestDataObjectRowMapper.INSTANCE,
      Function.identity(),
      "value",
      Collections.singletonMap("value", param),
      value,
      getter,
      column);
  }
}
