package io.vertx.sqlclient.templates;

/**
 * Mapper for {@link TestDataObject}.
 * NOTE: This class has been automatically generated from the {@link TestDataObject} original class using Vert.x codegen.
 */
@io.vertx.codegen.annotations.VertxGen
public interface TestDataObjectRowMapper extends io.vertx.sqlclient.templates.RowMapper<TestDataObject> {

  @io.vertx.codegen.annotations.GenIgnore
  TestDataObjectRowMapper INSTANCE = new TestDataObjectRowMapper() { };

  @io.vertx.codegen.annotations.GenIgnore
  java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<TestDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE::map, java.util.stream.Collectors.toList());

  @io.vertx.codegen.annotations.GenIgnore
  default TestDataObject map(io.vertx.sqlclient.Row row) {
    TestDataObject obj = new TestDataObject();
    Object val;
    int idx;
    if ((idx = row.getColumnIndex("booleanList")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      obj.setBooleanList(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("booleanMethodMappedDataObject")) != -1 && (val = row.getBoolean(idx)) != null) {
      obj.setBooleanMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toBoolean((java.lang.Boolean)val));
    }
    if ((idx = row.getColumnIndex("booleanMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      obj.setBooleanMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toBoolean(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("booleanMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      obj.setBooleanMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toBoolean(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("booleanSet")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      obj.setBooleanSet(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("boxedBoolean")) != -1 && (val = row.getBoolean(idx)) != null) {
      obj.setBoxedBoolean((boolean)val);
    }
    if ((idx = row.getColumnIndex("boxedDouble")) != -1 && (val = row.getDouble(idx)) != null) {
      obj.setBoxedDouble((double)val);
    }
    if ((idx = row.getColumnIndex("boxedFloat")) != -1 && (val = row.getFloat(idx)) != null) {
      obj.setBoxedFloat((float)val);
    }
    if ((idx = row.getColumnIndex("boxedInteger")) != -1 && (val = row.getInteger(idx)) != null) {
      obj.setBoxedInteger((int)val);
    }
    if ((idx = row.getColumnIndex("boxedLong")) != -1 && (val = row.getLong(idx)) != null) {
      obj.setBoxedLong((long)val);
    }
    if ((idx = row.getColumnIndex("boxedShort")) != -1 && (val = row.getShort(idx)) != null) {
      obj.setBoxedShort((short)val);
    }
    if ((idx = row.getColumnIndex("buffer")) != -1 && (val = row.getBuffer(idx)) != null) {
      obj.setBuffer((io.vertx.core.buffer.Buffer)val);
    }
    if ((idx = row.getColumnIndex("bufferList")) != -1 && (val = row.getArrayOfBuffers(idx)) != null) {
      obj.setBufferList(java.util.Arrays.stream((io.vertx.core.buffer.Buffer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("bufferSet")) != -1 && (val = row.getArrayOfBuffers(idx)) != null) {
      obj.setBufferSet(java.util.Arrays.stream((io.vertx.core.buffer.Buffer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("doubleList")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      obj.setDoubleList(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("doubleMethodMappedDataObject")) != -1 && (val = row.getDouble(idx)) != null) {
      obj.setDoubleMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toDouble((java.lang.Double)val));
    }
    if ((idx = row.getColumnIndex("doubleMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      obj.setDoubleMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toDouble(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("doubleMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      obj.setDoubleMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toDouble(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("doubleSet")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      obj.setDoubleSet(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("floatList")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      obj.setFloatList(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("floatMethodMappedDataObject")) != -1 && (val = row.getFloat(idx)) != null) {
      obj.setFloatMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toFloat((java.lang.Float)val));
    }
    if ((idx = row.getColumnIndex("floatMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      obj.setFloatMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toFloat(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("floatMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      obj.setFloatMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toFloat(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("floatSet")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      obj.setFloatSet(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("integerList")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      obj.setIntegerList(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("integerMethodMappedDataObject")) != -1 && (val = row.getInteger(idx)) != null) {
      obj.setIntegerMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toInteger((java.lang.Integer)val));
    }
    if ((idx = row.getColumnIndex("integerMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      obj.setIntegerMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toInteger(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("integerMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      obj.setIntegerMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toInteger(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("integerSet")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      obj.setIntegerSet(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("jsonArray")) != -1 && (val = row.getJsonArray(idx)) != null) {
      obj.setJsonArray((io.vertx.core.json.JsonArray)val);
    }
    if ((idx = row.getColumnIndex("jsonArrayList")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      obj.setJsonArrayList(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("jsonArrayMethodMappedDataObject")) != -1 && (val = row.getJsonArray(idx)) != null) {
      obj.setJsonArrayMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toJsonArray((io.vertx.core.json.JsonArray)val));
    }
    if ((idx = row.getColumnIndex("jsonArrayMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      obj.setJsonArrayMethodMappedDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toJsonArray(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("jsonArrayMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      obj.setJsonArrayMethodMappedDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toJsonArray(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("jsonArraySet")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      obj.setJsonArraySet(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("jsonObject")) != -1 && (val = row.getJsonObject(idx)) != null) {
      obj.setJsonObject((io.vertx.core.json.JsonObject)val);
    }
    if ((idx = row.getColumnIndex("jsonObjectDataObject")) != -1 && (val = row.getJsonObject(idx)) != null) {
      obj.setJsonObjectDataObject(new io.vertx.sqlclient.templates.JsonObjectDataObject((io.vertx.core.json.JsonObject)val));
    }
    if ((idx = row.getColumnIndex("jsonObjectDataObjectList")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> new io.vertx.sqlclient.templates.JsonObjectDataObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("jsonObjectDataObjectSet")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> new io.vertx.sqlclient.templates.JsonObjectDataObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("jsonObjectList")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("jsonObjectMethodMappedDataObject")) != -1 && (val = row.getJsonObject(idx)) != null) {
      obj.setJsonObjectMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toJsonObject((io.vertx.core.json.JsonObject)val));
    }
    if ((idx = row.getColumnIndex("jsonObjectMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectMethodMappedDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toJsonObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("jsonObjectMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectMethodMappedDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toJsonObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("jsonObjectSet")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      obj.setJsonObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("localDate")) != -1 && (val = row.getLocalDate(idx)) != null) {
      obj.setLocalDate((java.time.LocalDate)val);
    }
    if ((idx = row.getColumnIndex("localDateList")) != -1 && (val = row.getArrayOfLocalDates(idx)) != null) {
      obj.setLocalDateList(java.util.Arrays.stream((java.time.LocalDate[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("localDateSet")) != -1 && (val = row.getArrayOfLocalDates(idx)) != null) {
      obj.setLocalDateSet(java.util.Arrays.stream((java.time.LocalDate[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("localDateTime")) != -1 && (val = row.getLocalDateTime(idx)) != null) {
      obj.setLocalDateTime((java.time.LocalDateTime)val);
    }
    if ((idx = row.getColumnIndex("localDateTimeList")) != -1 && (val = row.getArrayOfLocalDateTimes(idx)) != null) {
      obj.setLocalDateTimeList(java.util.Arrays.stream((java.time.LocalDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("localDateTimeSet")) != -1 && (val = row.getArrayOfLocalDateTimes(idx)) != null) {
      obj.setLocalDateTimeSet(java.util.Arrays.stream((java.time.LocalDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("localTime")) != -1 && (val = row.getLocalTime(idx)) != null) {
      obj.setLocalTime((java.time.LocalTime)val);
    }
    if ((idx = row.getColumnIndex("localTimeList")) != -1 && (val = row.getArrayOfLocalTimes(idx)) != null) {
      obj.setLocalTimeList(java.util.Arrays.stream((java.time.LocalTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("localTimeSet")) != -1 && (val = row.getArrayOfLocalTimes(idx)) != null) {
      obj.setLocalTimeSet(java.util.Arrays.stream((java.time.LocalTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("longList")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      obj.setLongList(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("longMethodMappedDataObject")) != -1 && (val = row.getLong(idx)) != null) {
      obj.setLongMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toLong((java.lang.Long)val));
    }
    if ((idx = row.getColumnIndex("longMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      obj.setLongMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toLong(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("longMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      obj.setLongMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toLong(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("longSet")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      obj.setLongSet(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("offsetDateTime")) != -1 && (val = row.getOffsetDateTime(idx)) != null) {
      obj.setOffsetDateTime((java.time.OffsetDateTime)val);
    }
    if ((idx = row.getColumnIndex("offsetDateTimeList")) != -1 && (val = row.getArrayOfOffsetDateTimes(idx)) != null) {
      obj.setOffsetDateTimeList(java.util.Arrays.stream((java.time.OffsetDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("offsetDateTimeSet")) != -1 && (val = row.getArrayOfOffsetDateTimes(idx)) != null) {
      obj.setOffsetDateTimeSet(java.util.Arrays.stream((java.time.OffsetDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("offsetTime")) != -1 && (val = row.getOffsetTime(idx)) != null) {
      obj.setOffsetTime((java.time.OffsetTime)val);
    }
    if ((idx = row.getColumnIndex("offsetTimeList")) != -1 && (val = row.getArrayOfOffsetTimes(idx)) != null) {
      obj.setOffsetTimeList(java.util.Arrays.stream((java.time.OffsetTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("offsetTimeSet")) != -1 && (val = row.getArrayOfOffsetTimes(idx)) != null) {
      obj.setOffsetTimeSet(java.util.Arrays.stream((java.time.OffsetTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("primitiveBoolean")) != -1 && (val = row.getBoolean(idx)) != null) {
      obj.setPrimitiveBoolean((boolean)val);
    }
    if ((idx = row.getColumnIndex("primitiveDouble")) != -1 && (val = row.getDouble(idx)) != null) {
      obj.setPrimitiveDouble((double)val);
    }
    if ((idx = row.getColumnIndex("primitiveFloat")) != -1 && (val = row.getFloat(idx)) != null) {
      obj.setPrimitiveFloat((float)val);
    }
    if ((idx = row.getColumnIndex("primitiveInt")) != -1 && (val = row.getInteger(idx)) != null) {
      obj.setPrimitiveInt((int)val);
    }
    if ((idx = row.getColumnIndex("primitiveLong")) != -1 && (val = row.getLong(idx)) != null) {
      obj.setPrimitiveLong((long)val);
    }
    if ((idx = row.getColumnIndex("primitiveShort")) != -1 && (val = row.getShort(idx)) != null) {
      obj.setPrimitiveShort((short)val);
    }
    if ((idx = row.getColumnIndex("shortList")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      obj.setShortList(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("shortMethodMappedDataObject")) != -1 && (val = row.getShort(idx)) != null) {
      obj.setShortMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toShort((java.lang.Short)val));
    }
    if ((idx = row.getColumnIndex("shortMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      obj.setShortMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toShort(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("shortMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      obj.setShortMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toShort(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("shortSet")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      obj.setShortSet(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("string")) != -1 && (val = row.getString(idx)) != null) {
      obj.setString((java.lang.String)val);
    }
    if ((idx = row.getColumnIndex("stringList")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      obj.setStringList(java.util.Arrays.stream((java.lang.String[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("stringMethodMappedDataObject")) != -1 && (val = row.getString(idx)) != null) {
      obj.setStringMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toString((java.lang.String)val));
    }
    if ((idx = row.getColumnIndex("stringMethodMappedDataObjectList")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      obj.setStringMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.String[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toString(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("stringMethodMappedDataObjectSet")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      obj.setStringMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.String[])val).map(elt -> io.vertx.sqlclient.templates.DataObjectMapper.toString(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("stringSet")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      obj.setStringSet(java.util.Arrays.stream((java.lang.String[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("temporal")) != -1 && (val = row.getTemporal(idx)) != null) {
      obj.setTemporal((java.time.temporal.Temporal)val);
    }
    if ((idx = row.getColumnIndex("temporalList")) != -1 && (val = row.getArrayOfTemporals(idx)) != null) {
      obj.setTemporalList(java.util.Arrays.stream((java.time.temporal.Temporal[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("temporalSet")) != -1 && (val = row.getArrayOfTemporals(idx)) != null) {
      obj.setTemporalSet(java.util.Arrays.stream((java.time.temporal.Temporal[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("timeUnit")) != -1 && (val = row.get(java.util.concurrent.TimeUnit.class, idx)) != null) {
      obj.setTimeUnit((java.util.concurrent.TimeUnit)val);
    }
    if ((idx = row.getColumnIndex("timeUnitList")) != -1 && (val = row.get(java.util.concurrent.TimeUnit[].class, idx)) != null) {
      obj.setTimeUnitList(java.util.Arrays.stream((java.util.concurrent.TimeUnit[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("timeUnitSet")) != -1 && (val = row.get(java.util.concurrent.TimeUnit[].class, idx)) != null) {
      obj.setTimeUnitSet(java.util.Arrays.stream((java.util.concurrent.TimeUnit[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("uuid")) != -1 && (val = row.getUUID(idx)) != null) {
      obj.setUUID((java.util.UUID)val);
    }
    if ((idx = row.getColumnIndex("uuidList")) != -1 && (val = row.getArrayOfUUIDs(idx)) != null) {
      obj.setUUIDList(java.util.Arrays.stream((java.util.UUID[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    if ((idx = row.getColumnIndex("uuidSet")) != -1 && (val = row.getArrayOfUUIDs(idx)) != null) {
      obj.setUUIDSet(java.util.Arrays.stream((java.util.UUID[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    if ((idx = row.getColumnIndex("addedBooleanMethodMappedDataObjects")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      for (java.lang.Boolean elt : (java.lang.Boolean[])val) {
        obj.addAddedBooleanMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toBoolean(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedBooleans")) != -1 && (val = row.getArrayOfBooleans(idx)) != null) {
      for (java.lang.Boolean elt : (java.lang.Boolean[])val) {
        obj.addAddedBoolean(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedBuffers")) != -1 && (val = row.getArrayOfBuffers(idx)) != null) {
      for (io.vertx.core.buffer.Buffer elt : (io.vertx.core.buffer.Buffer[])val) {
        obj.addAddedBuffer(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedDoubleMethodMappedDataObjects")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      for (java.lang.Double elt : (java.lang.Double[])val) {
        obj.addAddedDoubleMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toDouble(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedDoubles")) != -1 && (val = row.getArrayOfDoubles(idx)) != null) {
      for (java.lang.Double elt : (java.lang.Double[])val) {
        obj.addAddedDouble(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedFloatMethodMappedDataObjects")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      for (java.lang.Float elt : (java.lang.Float[])val) {
        obj.addAddedFloatMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toFloat(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedFloats")) != -1 && (val = row.getArrayOfFloats(idx)) != null) {
      for (java.lang.Float elt : (java.lang.Float[])val) {
        obj.addAddedFloat(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedIntegerMethodMappedDataObjects")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      for (java.lang.Integer elt : (java.lang.Integer[])val) {
        obj.addAddedIntegerMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toInteger(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedIntegers")) != -1 && (val = row.getArrayOfIntegers(idx)) != null) {
      for (java.lang.Integer elt : (java.lang.Integer[])val) {
        obj.addAddedInteger(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedJsonArrayMethodMappedDataObjects")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      for (io.vertx.core.json.JsonArray elt : (io.vertx.core.json.JsonArray[])val) {
        obj.addAddedJsonArrayMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toJsonArray(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedJsonArrays")) != -1 && (val = row.getArrayOfJsonArrays(idx)) != null) {
      for (io.vertx.core.json.JsonArray elt : (io.vertx.core.json.JsonArray[])val) {
        obj.addAddedJsonArray(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedJsonObjectDataObjects")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObjectDataObject(new io.vertx.sqlclient.templates.JsonObjectDataObject(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedJsonObjectMethodMappedDataObjects")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObjectMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toJsonObject(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedJsonObjects")) != -1 && (val = row.getArrayOfJsonObjects(idx)) != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObject(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedLocalDateTimes")) != -1 && (val = row.getArrayOfLocalDateTimes(idx)) != null) {
      for (java.time.LocalDateTime elt : (java.time.LocalDateTime[])val) {
        obj.addAddedLocalDateTime(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedLocalDates")) != -1 && (val = row.getArrayOfLocalDates(idx)) != null) {
      for (java.time.LocalDate elt : (java.time.LocalDate[])val) {
        obj.addAddedLocalDate(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedLocalTimes")) != -1 && (val = row.getArrayOfLocalTimes(idx)) != null) {
      for (java.time.LocalTime elt : (java.time.LocalTime[])val) {
        obj.addAddedLocalTime(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedLongMethodMappedDataObjects")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      for (java.lang.Long elt : (java.lang.Long[])val) {
        obj.addAddedLongMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toLong(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedLongs")) != -1 && (val = row.getArrayOfLongs(idx)) != null) {
      for (java.lang.Long elt : (java.lang.Long[])val) {
        obj.addAddedLong(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedOffsetDateTimes")) != -1 && (val = row.getArrayOfOffsetDateTimes(idx)) != null) {
      for (java.time.OffsetDateTime elt : (java.time.OffsetDateTime[])val) {
        obj.addAddedOffsetDateTime(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedOffsetTimes")) != -1 && (val = row.getArrayOfOffsetTimes(idx)) != null) {
      for (java.time.OffsetTime elt : (java.time.OffsetTime[])val) {
        obj.addAddedOffsetTime(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedShortMethodMappedDataObjects")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      for (java.lang.Short elt : (java.lang.Short[])val) {
        obj.addAddedShortMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toShort(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedShorts")) != -1 && (val = row.getArrayOfShorts(idx)) != null) {
      for (java.lang.Short elt : (java.lang.Short[])val) {
        obj.addAddedShort(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedStringMethodMappedDataObjects")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      for (java.lang.String elt : (java.lang.String[])val) {
        obj.addAddedStringMethodMappedDataObject(io.vertx.sqlclient.templates.DataObjectMapper.toString(elt));
      }
    }
    if ((idx = row.getColumnIndex("addedStrings")) != -1 && (val = row.getArrayOfStrings(idx)) != null) {
      for (java.lang.String elt : (java.lang.String[])val) {
        obj.addAddedString(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedTemporals")) != -1 && (val = row.getArrayOfTemporals(idx)) != null) {
      for (java.time.temporal.Temporal elt : (java.time.temporal.Temporal[])val) {
        obj.addAddedTemporal(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedTimeUnits")) != -1 && (val = row.get(java.util.concurrent.TimeUnit[].class, idx)) != null) {
      for (java.util.concurrent.TimeUnit elt : (java.util.concurrent.TimeUnit[])val) {
        obj.addAddedTimeUnit(elt);
      }
    }
    if ((idx = row.getColumnIndex("addedUUIDs")) != -1 && (val = row.getArrayOfUUIDs(idx)) != null) {
      for (java.util.UUID elt : (java.util.UUID[])val) {
        obj.addAddedUUID(elt);
      }
    }
    return obj;
  }
}
