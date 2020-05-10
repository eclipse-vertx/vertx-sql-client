package io.vertx.sqlclient.template;

/**
 * Mapper for {@link TestDataObject}.
 * NOTE: This class has been automatically generated from the {@link TestDataObject} original class using Vert.x codegen.
 */
public class TestDataObjectRowMapper implements java.util.function.Function<io.vertx.sqlclient.Row, TestDataObject> {

  public static final java.util.function.Function<io.vertx.sqlclient.Row, TestDataObject> INSTANCE = new TestDataObjectRowMapper();

  public static final java.util.stream.Collector<io.vertx.sqlclient.Row, ?, java.util.List<TestDataObject>> COLLECTOR = java.util.stream.Collectors.mapping(INSTANCE, java.util.stream.Collectors.toList());

  public TestDataObject apply(io.vertx.sqlclient.Row row) {
    TestDataObject obj = new TestDataObject();
    Object val;
    val = row.getBooleanArray("booleanList");
    if (val != null) {
      obj.setBooleanList(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getBoolean("booleanMethodMappedDataObject");
    if (val != null) {
      obj.setBooleanMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toBoolean((java.lang.Boolean)val));
    }
    val = row.getBooleanArray("booleanMethodMappedDataObjectList");
    if (val != null) {
      obj.setBooleanMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toBoolean(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getBooleanArray("booleanMethodMappedDataObjectSet");
    if (val != null) {
      obj.setBooleanMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toBoolean(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getBooleanArray("booleanSet");
    if (val != null) {
      obj.setBooleanSet(java.util.Arrays.stream((java.lang.Boolean[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getBoolean("boxedBoolean");
    if (val != null) {
      obj.setBoxedBoolean((boolean)val);
    }
    val = row.getDouble("boxedDouble");
    if (val != null) {
      obj.setBoxedDouble((double)val);
    }
    val = row.getFloat("boxedFloat");
    if (val != null) {
      obj.setBoxedFloat((float)val);
    }
    val = row.getInteger("boxedInteger");
    if (val != null) {
      obj.setBoxedInteger((int)val);
    }
    val = row.getLong("boxedLong");
    if (val != null) {
      obj.setBoxedLong((long)val);
    }
    val = row.getShort("boxedShort");
    if (val != null) {
      obj.setBoxedShort((short)val);
    }
    val = row.getBuffer("buffer");
    if (val != null) {
      obj.setBuffer((io.vertx.core.buffer.Buffer)val);
    }
    val = row.getBufferArray("bufferList");
    if (val != null) {
      obj.setBufferList(java.util.Arrays.stream((io.vertx.core.buffer.Buffer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getBufferArray("bufferSet");
    if (val != null) {
      obj.setBufferSet(java.util.Arrays.stream((io.vertx.core.buffer.Buffer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getDoubleArray("doubleList");
    if (val != null) {
      obj.setDoubleList(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getDouble("doubleMethodMappedDataObject");
    if (val != null) {
      obj.setDoubleMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toDouble((java.lang.Double)val));
    }
    val = row.getDoubleArray("doubleMethodMappedDataObjectList");
    if (val != null) {
      obj.setDoubleMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toDouble(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getDoubleArray("doubleMethodMappedDataObjectSet");
    if (val != null) {
      obj.setDoubleMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toDouble(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getDoubleArray("doubleSet");
    if (val != null) {
      obj.setDoubleSet(java.util.Arrays.stream((java.lang.Double[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getFloatArray("floatList");
    if (val != null) {
      obj.setFloatList(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getFloat("floatMethodMappedDataObject");
    if (val != null) {
      obj.setFloatMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toFloat((java.lang.Float)val));
    }
    val = row.getFloatArray("floatMethodMappedDataObjectList");
    if (val != null) {
      obj.setFloatMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toFloat(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getFloatArray("floatMethodMappedDataObjectSet");
    if (val != null) {
      obj.setFloatMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toFloat(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getFloatArray("floatSet");
    if (val != null) {
      obj.setFloatSet(java.util.Arrays.stream((java.lang.Float[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getIntegerArray("integerList");
    if (val != null) {
      obj.setIntegerList(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getInteger("integerMethodMappedDataObject");
    if (val != null) {
      obj.setIntegerMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toInteger((java.lang.Integer)val));
    }
    val = row.getIntegerArray("integerMethodMappedDataObjectList");
    if (val != null) {
      obj.setIntegerMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toInteger(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getIntegerArray("integerMethodMappedDataObjectSet");
    if (val != null) {
      obj.setIntegerMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toInteger(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getIntegerArray("integerSet");
    if (val != null) {
      obj.setIntegerSet(java.util.Arrays.stream((java.lang.Integer[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getJsonArray("jsonArray");
    if (val != null) {
      obj.setJsonArray((io.vertx.core.json.JsonArray)val);
    }
    val = row.getJsonArrayArray("jsonArrayList");
    if (val != null) {
      obj.setJsonArrayList(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getJsonArray("jsonArrayMethodMappedDataObject");
    if (val != null) {
      obj.setJsonArrayMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toJsonArray((io.vertx.core.json.JsonArray)val));
    }
    val = row.getJsonArrayArray("jsonArrayMethodMappedDataObjectList");
    if (val != null) {
      obj.setJsonArrayMethodMappedDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toJsonArray(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getJsonArrayArray("jsonArrayMethodMappedDataObjectSet");
    if (val != null) {
      obj.setJsonArrayMethodMappedDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toJsonArray(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getJsonArrayArray("jsonArraySet");
    if (val != null) {
      obj.setJsonArraySet(java.util.Arrays.stream((io.vertx.core.json.JsonArray[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getJsonObject("jsonObject");
    if (val != null) {
      obj.setJsonObject((io.vertx.core.json.JsonObject)val);
    }
    val = row.getJsonObject("jsonObjectDataObject");
    if (val != null) {
      obj.setJsonObjectDataObject(new io.vertx.sqlclient.template.JsonObjectDataObject((io.vertx.core.json.JsonObject)val));
    }
    val = row.getJsonObjectArray("jsonObjectDataObjectList");
    if (val != null) {
      obj.setJsonObjectDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> new io.vertx.sqlclient.template.JsonObjectDataObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getJsonObjectArray("jsonObjectDataObjectSet");
    if (val != null) {
      obj.setJsonObjectDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> new io.vertx.sqlclient.template.JsonObjectDataObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getJsonObjectArray("jsonObjectList");
    if (val != null) {
      obj.setJsonObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getJsonObject("jsonObjectMethodMappedDataObject");
    if (val != null) {
      obj.setJsonObjectMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toJsonObject((io.vertx.core.json.JsonObject)val));
    }
    val = row.getJsonObjectArray("jsonObjectMethodMappedDataObjectList");
    if (val != null) {
      obj.setJsonObjectMethodMappedDataObjectList(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toJsonObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getJsonObjectArray("jsonObjectMethodMappedDataObjectSet");
    if (val != null) {
      obj.setJsonObjectMethodMappedDataObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toJsonObject(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getJsonObjectArray("jsonObjectSet");
    if (val != null) {
      obj.setJsonObjectSet(java.util.Arrays.stream((io.vertx.core.json.JsonObject[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getLocalDate("localDate");
    if (val != null) {
      obj.setLocalDate((java.time.LocalDate)val);
    }
    val = row.getLocalDateArray("localDateList");
    if (val != null) {
      obj.setLocalDateList(java.util.Arrays.stream((java.time.LocalDate[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getLocalDateArray("localDateSet");
    if (val != null) {
      obj.setLocalDateSet(java.util.Arrays.stream((java.time.LocalDate[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getLocalDateTime("localDateTime");
    if (val != null) {
      obj.setLocalDateTime((java.time.LocalDateTime)val);
    }
    val = row.getLocalDateTimeArray("localDateTimeList");
    if (val != null) {
      obj.setLocalDateTimeList(java.util.Arrays.stream((java.time.LocalDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getLocalDateTimeArray("localDateTimeSet");
    if (val != null) {
      obj.setLocalDateTimeSet(java.util.Arrays.stream((java.time.LocalDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getLocalTime("localTime");
    if (val != null) {
      obj.setLocalTime((java.time.LocalTime)val);
    }
    val = row.getLocalTimeArray("localTimeList");
    if (val != null) {
      obj.setLocalTimeList(java.util.Arrays.stream((java.time.LocalTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getLocalTimeArray("localTimeSet");
    if (val != null) {
      obj.setLocalTimeSet(java.util.Arrays.stream((java.time.LocalTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getLongArray("longList");
    if (val != null) {
      obj.setLongList(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getLong("longMethodMappedDataObject");
    if (val != null) {
      obj.setLongMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toLong((java.lang.Long)val));
    }
    val = row.getLongArray("longMethodMappedDataObjectList");
    if (val != null) {
      obj.setLongMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toLong(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getLongArray("longMethodMappedDataObjectSet");
    if (val != null) {
      obj.setLongMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toLong(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getLongArray("longSet");
    if (val != null) {
      obj.setLongSet(java.util.Arrays.stream((java.lang.Long[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getOffsetDateTime("offsetDateTime");
    if (val != null) {
      obj.setOffsetDateTime((java.time.OffsetDateTime)val);
    }
    val = row.getOffsetDateTimeArray("offsetDateTimeList");
    if (val != null) {
      obj.setOffsetDateTimeList(java.util.Arrays.stream((java.time.OffsetDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getOffsetDateTimeArray("offsetDateTimeSet");
    if (val != null) {
      obj.setOffsetDateTimeSet(java.util.Arrays.stream((java.time.OffsetDateTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getOffsetTime("offsetTime");
    if (val != null) {
      obj.setOffsetTime((java.time.OffsetTime)val);
    }
    val = row.getOffsetTimeArray("offsetTimeList");
    if (val != null) {
      obj.setOffsetTimeList(java.util.Arrays.stream((java.time.OffsetTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getOffsetTimeArray("offsetTimeSet");
    if (val != null) {
      obj.setOffsetTimeSet(java.util.Arrays.stream((java.time.OffsetTime[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getBoolean("primitiveBoolean");
    if (val != null) {
      obj.setPrimitiveBoolean((boolean)val);
    }
    val = row.getDouble("primitiveDouble");
    if (val != null) {
      obj.setPrimitiveDouble((double)val);
    }
    val = row.getFloat("primitiveFloat");
    if (val != null) {
      obj.setPrimitiveFloat((float)val);
    }
    val = row.getInteger("primitiveInt");
    if (val != null) {
      obj.setPrimitiveInt((int)val);
    }
    val = row.getLong("primitiveLong");
    if (val != null) {
      obj.setPrimitiveLong((long)val);
    }
    val = row.getShort("primitiveShort");
    if (val != null) {
      obj.setPrimitiveShort((short)val);
    }
    val = row.getShortArray("shortList");
    if (val != null) {
      obj.setShortList(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getShort("shortMethodMappedDataObject");
    if (val != null) {
      obj.setShortMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toShort((java.lang.Short)val));
    }
    val = row.getShortArray("shortMethodMappedDataObjectList");
    if (val != null) {
      obj.setShortMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toShort(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getShortArray("shortMethodMappedDataObjectSet");
    if (val != null) {
      obj.setShortMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toShort(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getShortArray("shortSet");
    if (val != null) {
      obj.setShortSet(java.util.Arrays.stream((java.lang.Short[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getString("string");
    if (val != null) {
      obj.setString((java.lang.String)val);
    }
    val = row.getStringArray("stringList");
    if (val != null) {
      obj.setStringList(java.util.Arrays.stream((java.lang.String[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getString("stringMethodMappedDataObject");
    if (val != null) {
      obj.setStringMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toString((java.lang.String)val));
    }
    val = row.getStringArray("stringMethodMappedDataObjectList");
    if (val != null) {
      obj.setStringMethodMappedDataObjectList(java.util.Arrays.stream((java.lang.String[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toString(elt)).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getStringArray("stringMethodMappedDataObjectSet");
    if (val != null) {
      obj.setStringMethodMappedDataObjectSet(java.util.Arrays.stream((java.lang.String[])val).map(elt -> io.vertx.sqlclient.template.DataObjectMapper.toString(elt)).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getStringArray("stringSet");
    if (val != null) {
      obj.setStringSet(java.util.Arrays.stream((java.lang.String[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getTemporal("temporal");
    if (val != null) {
      obj.setTemporal((java.time.temporal.Temporal)val);
    }
    val = row.getTemporalArray("temporalList");
    if (val != null) {
      obj.setTemporalList(java.util.Arrays.stream((java.time.temporal.Temporal[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getTemporalArray("temporalSet");
    if (val != null) {
      obj.setTemporalSet(java.util.Arrays.stream((java.time.temporal.Temporal[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.get(java.util.concurrent.TimeUnit.class, "timeUnit");
    if (val != null) {
      obj.setTimeUnit((java.util.concurrent.TimeUnit)val);
    }
    val = row.get(java.util.concurrent.TimeUnit[].class, "timeUnitList");
    if (val != null) {
      obj.setTimeUnitList(java.util.Arrays.stream((java.util.concurrent.TimeUnit[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.get(java.util.concurrent.TimeUnit[].class, "timeUnitSet");
    if (val != null) {
      obj.setTimeUnitSet(java.util.Arrays.stream((java.util.concurrent.TimeUnit[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getUUID("uuid");
    if (val != null) {
      obj.setUUID((java.util.UUID)val);
    }
    val = row.getUUIDArray("uuidList");
    if (val != null) {
      obj.setUUIDList(java.util.Arrays.stream((java.util.UUID[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.ArrayList::new)));
    }
    val = row.getUUIDArray("uuidSet");
    if (val != null) {
      obj.setUUIDSet(java.util.Arrays.stream((java.util.UUID[])val).map(elt -> elt).collect(java.util.stream.Collectors.toCollection(java.util.HashSet::new)));
    }
    val = row.getBooleanArray("addedBooleanMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Boolean elt : (java.lang.Boolean[])val) {
        obj.addAddedBooleanMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toBoolean(elt));
      }
    }
    val = row.getBooleanArray("addedBooleans");
    if (val != null) {
      for (java.lang.Boolean elt : (java.lang.Boolean[])val) {
        obj.addAddedBoolean(elt);
      }
    }
    val = row.getBufferArray("addedBuffers");
    if (val != null) {
      for (io.vertx.core.buffer.Buffer elt : (io.vertx.core.buffer.Buffer[])val) {
        obj.addAddedBuffer(elt);
      }
    }
    val = row.getDoubleArray("addedDoubleMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Double elt : (java.lang.Double[])val) {
        obj.addAddedDoubleMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toDouble(elt));
      }
    }
    val = row.getDoubleArray("addedDoubles");
    if (val != null) {
      for (java.lang.Double elt : (java.lang.Double[])val) {
        obj.addAddedDouble(elt);
      }
    }
    val = row.getFloatArray("addedFloatMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Float elt : (java.lang.Float[])val) {
        obj.addAddedFloatMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toFloat(elt));
      }
    }
    val = row.getFloatArray("addedFloats");
    if (val != null) {
      for (java.lang.Float elt : (java.lang.Float[])val) {
        obj.addAddedFloat(elt);
      }
    }
    val = row.getIntegerArray("addedIntegerMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Integer elt : (java.lang.Integer[])val) {
        obj.addAddedIntegerMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toInteger(elt));
      }
    }
    val = row.getIntegerArray("addedIntegers");
    if (val != null) {
      for (java.lang.Integer elt : (java.lang.Integer[])val) {
        obj.addAddedInteger(elt);
      }
    }
    val = row.getJsonArrayArray("addedJsonArrayMethodMappedDataObjects");
    if (val != null) {
      for (io.vertx.core.json.JsonArray elt : (io.vertx.core.json.JsonArray[])val) {
        obj.addAddedJsonArrayMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toJsonArray(elt));
      }
    }
    val = row.getJsonArrayArray("addedJsonArrays");
    if (val != null) {
      for (io.vertx.core.json.JsonArray elt : (io.vertx.core.json.JsonArray[])val) {
        obj.addAddedJsonArray(elt);
      }
    }
    val = row.getJsonObjectArray("addedJsonObjectDataObjects");
    if (val != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObjectDataObject(new io.vertx.sqlclient.template.JsonObjectDataObject(elt));
      }
    }
    val = row.getJsonObjectArray("addedJsonObjectMethodMappedDataObjects");
    if (val != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObjectMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toJsonObject(elt));
      }
    }
    val = row.getJsonObjectArray("addedJsonObjects");
    if (val != null) {
      for (io.vertx.core.json.JsonObject elt : (io.vertx.core.json.JsonObject[])val) {
        obj.addAddedJsonObject(elt);
      }
    }
    val = row.getLocalDateTimeArray("addedLocalDateTimes");
    if (val != null) {
      for (java.time.LocalDateTime elt : (java.time.LocalDateTime[])val) {
        obj.addAddedLocalDateTime(elt);
      }
    }
    val = row.getLocalDateArray("addedLocalDates");
    if (val != null) {
      for (java.time.LocalDate elt : (java.time.LocalDate[])val) {
        obj.addAddedLocalDate(elt);
      }
    }
    val = row.getLocalTimeArray("addedLocalTimes");
    if (val != null) {
      for (java.time.LocalTime elt : (java.time.LocalTime[])val) {
        obj.addAddedLocalTime(elt);
      }
    }
    val = row.getLongArray("addedLongMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Long elt : (java.lang.Long[])val) {
        obj.addAddedLongMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toLong(elt));
      }
    }
    val = row.getLongArray("addedLongs");
    if (val != null) {
      for (java.lang.Long elt : (java.lang.Long[])val) {
        obj.addAddedLong(elt);
      }
    }
    val = row.getOffsetDateTimeArray("addedOffsetDateTimes");
    if (val != null) {
      for (java.time.OffsetDateTime elt : (java.time.OffsetDateTime[])val) {
        obj.addAddedOffsetDateTime(elt);
      }
    }
    val = row.getOffsetTimeArray("addedOffsetTimes");
    if (val != null) {
      for (java.time.OffsetTime elt : (java.time.OffsetTime[])val) {
        obj.addAddedOffsetTime(elt);
      }
    }
    val = row.getShortArray("addedShortMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.Short elt : (java.lang.Short[])val) {
        obj.addAddedShortMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toShort(elt));
      }
    }
    val = row.getShortArray("addedShorts");
    if (val != null) {
      for (java.lang.Short elt : (java.lang.Short[])val) {
        obj.addAddedShort(elt);
      }
    }
    val = row.getStringArray("addedStringMethodMappedDataObjects");
    if (val != null) {
      for (java.lang.String elt : (java.lang.String[])val) {
        obj.addAddedStringMethodMappedDataObject(io.vertx.sqlclient.template.DataObjectMapper.toString(elt));
      }
    }
    val = row.getStringArray("addedStrings");
    if (val != null) {
      for (java.lang.String elt : (java.lang.String[])val) {
        obj.addAddedString(elt);
      }
    }
    val = row.getTemporalArray("addedTemporals");
    if (val != null) {
      for (java.time.temporal.Temporal elt : (java.time.temporal.Temporal[])val) {
        obj.addAddedTemporal(elt);
      }
    }
    val = row.get(java.util.concurrent.TimeUnit[].class, "addedTimeUnits");
    if (val != null) {
      for (java.util.concurrent.TimeUnit elt : (java.util.concurrent.TimeUnit[])val) {
        obj.addAddedTimeUnit(elt);
      }
    }
    val = row.getUUIDArray("addedUUIDs");
    if (val != null) {
      for (java.util.UUID elt : (java.util.UUID[])val) {
        obj.addAddedUUID(elt);
      }
    }
    return obj;
  }
}
