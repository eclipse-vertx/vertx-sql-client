package io.vertx.sqltemplates;

import io.vertx.codegen.annotations.DataObject;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.sqltemplates.annotations.ParametersMapped;
import io.vertx.sqltemplates.annotations.RowMapped;
import io.vertx.sqltemplates.annotations.TemplateParameter;
import io.vertx.sqltemplates.wrappers.BooleanWrapper;
import io.vertx.sqltemplates.wrappers.DoubleWrapper;
import io.vertx.sqltemplates.wrappers.FloatWrapper;
import io.vertx.sqltemplates.wrappers.IntegerWrapper;
import io.vertx.sqltemplates.wrappers.JsonArrayWrapper;
import io.vertx.sqltemplates.wrappers.JsonObjectWrapper;
import io.vertx.sqltemplates.wrappers.LongWrapper;
import io.vertx.sqltemplates.wrappers.ShortWrapper;
import io.vertx.sqltemplates.wrappers.StringWrapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.temporal.Temporal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@DataObject
@RowMapped
@ParametersMapped
public class TestDataObject {

  private boolean primitiveBoolean;
  private short primitiveShort;
  private int primitiveInt;
  private long primitiveLong;
  private float primitiveFloat;
  private double primitiveDouble;

  private boolean boxedBoolean;
  private short boxedShort;
  private int boxedInteger;
  private long boxedLong;
  private float boxedFloat;
  private double boxedDouble;
  @TemplateParameter(name = "the_string") private String string;
  private JsonObject jsonObject;
  private JsonArray jsonArray;
  private Buffer buffer;
  private UUID uuid;
  private LocalDateTime localDateTime;
  private LocalDate localDate;
  private LocalTime localTime;
  private OffsetTime offsetTime;
  private OffsetDateTime offsetDateTime;
  private Temporal temporal;
  private JsonObjectDataObject jsonObjectDataObject;
  private ShortWrapper shortMethodMappedDataObject;
  private BooleanWrapper booleanMethodMappedDataObject;;
  private IntegerWrapper integerMethodMappedDataObject;
  private LongWrapper longMethodMappedDataObject;
  private FloatWrapper floatMethodMappedDataObject;
  private DoubleWrapper doubleMethodMappedDataObject;
  private StringWrapper stringMethodMappedDataObject;
  private JsonObjectWrapper jsonObjectMethodMappedDataObject;
  private JsonArrayWrapper jsonArrayMethodMappedDataObject;
  private TimeUnit timeUnit;

  private List<Boolean> booleanList;
  private List<Short> shortList;
  private List<Integer> integerList;
  private List<Long> longList;
  private List<Float> floatList;
  private List<Double> doubleList;
  private List<String> stringList;
  private List<JsonObject> jsonObjectList;
  private List<JsonArray> jsonArrayList;
  private List<Buffer> bufferList;
  private List<UUID> uuidList;
  private List<LocalDateTime> localDateTimeList;
  private List<LocalDate> localDateList;
  private List<LocalTime> localTimeList;
  private List<OffsetTime> offsetTimeList;
  private List<OffsetDateTime> offsetDateTimeList;
  private List<Temporal> temporalList;
  private List<JsonObjectDataObject> jsonObjectDataObjectList;
  private List<BooleanWrapper> booleanMethodMappedDataObjectList;
  private List<ShortWrapper> shortMethodMappedDataObjectList;
  private List<IntegerWrapper> integerMethodMappedDataObjectList;
  private List<LongWrapper> longMethodMappedDataObjectList;
  private List<FloatWrapper> floatMethodMappedDataObjectList;
  private List<DoubleWrapper> doubleMethodMappedDataObjectList;
  private List<StringWrapper> stringMethodMappedDataObjectList;
  private List<JsonObjectWrapper> jsonObjectMethodMappedDataObjectList;
  private List<JsonArrayWrapper> jsonArrayMethodMappedDataObjectList;
  private List<TimeUnit> timeUnitList;

  private Set<Boolean> booleanSet;
  private Set<Short> shortSet;
  private Set<Integer> integerSet;
  private Set<Long> longSet;
  private Set<Float> floatSet;
  private Set<Double> doubleSet;
  private Set<String> stringSet;
  private Set<JsonObject> jsonObjectSet;
  private Set<JsonArray> jsonArraySet;
  private Set<Buffer> bufferSet;
  private Set<UUID> uuidSet;
  private Set<LocalDateTime> localDateTimeSet;
  private Set<LocalDate> localDateSet;
  private Set<LocalTime> localTimeSet;
  private Set<OffsetTime> offsetTimeSet;
  private Set<OffsetDateTime> offsetDateTimeSet;
  private Set<Temporal> temporalSet;
  private Set<JsonObjectDataObject> jsonObjectDataObjectSet;
  private Set<BooleanWrapper> booleanMethodMappedDataObjectSet;
  private Set<ShortWrapper> shortMethodMappedDataObjectSet;
  private Set<IntegerWrapper> integerMethodMappedDataObjectSet;
  private Set<LongWrapper> longMethodMappedDataObjectSet;
  private Set<FloatWrapper> floatMethodMappedDataObjectSet;
  private Set<DoubleWrapper> doubleMethodMappedDataObjectSet;
  private Set<StringWrapper> stringMethodMappedDataObjectSet;
  private Set<JsonObjectWrapper> jsonObjectMethodMappedDataObjectSet;
  private Set<JsonArrayWrapper> jsonArrayMethodMappedDataObjectSet;
  private Set<TimeUnit> timeUnitSet;

  private List<Boolean> addedBooleans = new ArrayList<>();
  private List<Short> addedShorts = new ArrayList<>();
  private List<Integer> addedIntegers = new ArrayList<>();
  private List<Long> addedLongs = new ArrayList<>();
  private List<Float> addedFloats = new ArrayList<>();
  private List<Double> addedDoubles = new ArrayList<>();
  private List<String> addedStrings = new ArrayList<>();
  private List<JsonObject> addedJsonObjects = new ArrayList<>();
  private List<JsonArray> addedJsonArrays = new ArrayList<>();
  private List<Buffer> addedBuffers = new ArrayList<>();
  private List<UUID> addedUUIDs = new ArrayList<>();
  private List<LocalDateTime> addedLocalDateTimes = new ArrayList<>();
  private List<LocalDate> addedLocalDates = new ArrayList<>();
  private List<LocalTime> addedLocalTimes = new ArrayList<>();
  private List<OffsetTime> addedOffsetTimes = new ArrayList<>();
  private List<OffsetDateTime> addedOffsetDateTimes = new ArrayList<>();
  private List<Temporal> addedTemporals = new ArrayList<>();
  private List<JsonObjectDataObject> addedJsonObjectDataObjects = new ArrayList<>();
  private List<BooleanWrapper> booleanAddedMethodMappedDataObjects = new ArrayList<>();
  private List<ShortWrapper> shortAddedMethodMappedDataObjects = new ArrayList<>();
  private List<IntegerWrapper> integerAddedMethodMappedDataObjects = new ArrayList<>();
  private List<LongWrapper> longAddedMethodMappedDataObjects = new ArrayList<>();
  private List<FloatWrapper> floatAddedMethodMappedDataObjects = new ArrayList<>();
  private List<DoubleWrapper> doubleAddedMethodMappedDataObjects = new ArrayList<>();
  private List<StringWrapper> stringAddedMethodMappedDataObjects = new ArrayList<>();
  private List<JsonObjectWrapper> jsonObjectAddedMethodMappedDataObjects = new ArrayList<>();
  private List<JsonArrayWrapper> jsonArrayAddedMethodMappedDataObjects = new ArrayList<>();
  private List<TimeUnit> addedTimeUnits = new ArrayList<>();

  public boolean isPrimitiveBoolean() {
    return primitiveBoolean;
  }

  public void setPrimitiveBoolean(boolean primitiveBoolean) {
    this.primitiveBoolean = primitiveBoolean;
  }

  public short getPrimitiveShort() {
    return primitiveShort;
  }

  public void setPrimitiveShort(short primitiveShort) {
    this.primitiveShort = primitiveShort;
  }

  public int getPrimitiveInt() {
    return primitiveInt;
  }

  public void setPrimitiveInt(int primitiveInt) {
    this.primitiveInt = primitiveInt;
  }

  public long getPrimitiveLong() {
    return primitiveLong;
  }

  public void setPrimitiveLong(long primitiveLong) {
    this.primitiveLong = primitiveLong;
  }

  public float getPrimitiveFloat() {
    return primitiveFloat;
  }

  public void setPrimitiveFloat(float primitiveFloat) {
    this.primitiveFloat = primitiveFloat;
  }

  public double getPrimitiveDouble() {
    return primitiveDouble;
  }

  public void setPrimitiveDouble(double primitiveDouble) {
    this.primitiveDouble = primitiveDouble;
  }

  public boolean isBoxedBoolean() {
    return boxedBoolean;
  }

  public void setBoxedBoolean(boolean boxedBoolean) {
    this.boxedBoolean = boxedBoolean;
  }

  public short getBoxedShort() {
    return boxedShort;
  }

  public void setBoxedShort(short boxedShort) {
    this.boxedShort = boxedShort;
  }

  public int getBoxedInteger() {
    return boxedInteger;
  }

  public void setBoxedInteger(int boxedInteger) {
    this.boxedInteger = boxedInteger;
  }

  public long getBoxedLong() {
    return boxedLong;
  }

  public void setBoxedLong(long boxedLong) {
    this.boxedLong = boxedLong;
  }

  public float getBoxedFloat() {
    return boxedFloat;
  }

  public void setBoxedFloat(float boxedFloat) {
    this.boxedFloat = boxedFloat;
  }

  public double getBoxedDouble() {
    return boxedDouble;
  }

  public void setBoxedDouble(double boxedDouble) {
    this.boxedDouble = boxedDouble;
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public JsonObject getJsonObject() {
    return jsonObject;
  }

  public void setJsonObject(JsonObject jsonObject) {
    this.jsonObject = jsonObject;
  }

  public JsonArray getJsonArray() {
    return jsonArray;
  }

  public void setJsonArray(JsonArray jsonArray) {
    this.jsonArray = jsonArray;
  }

  public Buffer getBuffer() {
    return buffer;
  }

  public void setBuffer(Buffer buffer) {
    this.buffer = buffer;
  }

  public UUID getUUID() {
    return uuid;
  }

  public void setUUID(UUID uuid) {
    this.uuid = uuid;
  }

  public LocalDateTime getLocalDateTime() {
    return localDateTime;
  }

  public void setLocalDateTime(LocalDateTime localDateTime) {
    this.localDateTime = localDateTime;
  }

  public LocalDate getLocalDate() {
    return localDate;
  }

  public void setLocalDate(LocalDate localDate) {
    this.localDate = localDate;
  }

  public LocalTime getLocalTime() {
    return localTime;
  }

  public void setLocalTime(LocalTime localTime) {
    this.localTime = localTime;
  }

  public OffsetTime getOffsetTime() {
    return offsetTime;
  }

  public void setOffsetTime(OffsetTime offsetTime) {
    this.offsetTime = offsetTime;
  }

  public OffsetDateTime getOffsetDateTime() {
    return offsetDateTime;
  }

  public void setOffsetDateTime(OffsetDateTime offsetDateTime) {
    this.offsetDateTime = offsetDateTime;
  }

  public Temporal getTemporal() {
    return temporal;
  }

  public void setTemporal(Temporal temporal) {
    this.temporal = temporal;
  }

  public JsonObjectDataObject getJsonObjectDataObject() {
    return jsonObjectDataObject;
  }

  public void setJsonObjectDataObject(JsonObjectDataObject jsonObjectDataObject) {
    this.jsonObjectDataObject = jsonObjectDataObject;
  }

  public BooleanWrapper getBooleanMethodMappedDataObject() {
    return booleanMethodMappedDataObject;
  }

  public void setBooleanMethodMappedDataObject(BooleanWrapper booleanMethodMappedDataObject) {
    this.booleanMethodMappedDataObject = booleanMethodMappedDataObject;
  }

  public ShortWrapper getShortMethodMappedDataObject() {
    return shortMethodMappedDataObject;
  }

  public void setShortMethodMappedDataObject(ShortWrapper shortMethodMappedDataObject) {
    this.shortMethodMappedDataObject = shortMethodMappedDataObject;
  }

  public IntegerWrapper getIntegerMethodMappedDataObject() {
    return integerMethodMappedDataObject;
  }

  public void setIntegerMethodMappedDataObject(IntegerWrapper integerMethodMappedDataObject) {
    this.integerMethodMappedDataObject = integerMethodMappedDataObject;
  }

  public LongWrapper getLongMethodMappedDataObject() {
    return longMethodMappedDataObject;
  }

  public void setLongMethodMappedDataObject(LongWrapper longMethodMappedDataObject) {
    this.longMethodMappedDataObject = longMethodMappedDataObject;
  }

  public FloatWrapper getFloatMethodMappedDataObject() {
    return floatMethodMappedDataObject;
  }

  public void setFloatMethodMappedDataObject(FloatWrapper floatMethodMappedDataObject) {
    this.floatMethodMappedDataObject = floatMethodMappedDataObject;
  }

  public DoubleWrapper getDoubleMethodMappedDataObject() {
    return doubleMethodMappedDataObject;
  }

  public void setDoubleMethodMappedDataObject(DoubleWrapper doubleMethodMappedDataObject) {
    this.doubleMethodMappedDataObject = doubleMethodMappedDataObject;
  }

  public StringWrapper getStringMethodMappedDataObject() {
    return stringMethodMappedDataObject;
  }

  public void setStringMethodMappedDataObject(StringWrapper stringMethodMappedDataObject) {
    this.stringMethodMappedDataObject = stringMethodMappedDataObject;
  }

  public JsonObjectWrapper getJsonObjectMethodMappedDataObject() {
    return jsonObjectMethodMappedDataObject;
  }

  public void setJsonObjectMethodMappedDataObject(JsonObjectWrapper jsonObjectMethodMappedDataObject) {
    this.jsonObjectMethodMappedDataObject = jsonObjectMethodMappedDataObject;
  }

  public JsonArrayWrapper getJsonArrayMethodMappedDataObject() {
    return jsonArrayMethodMappedDataObject;
  }

  public void setJsonArrayMethodMappedDataObject(JsonArrayWrapper jsonArrayMethodMappedDataObject) {
    this.jsonArrayMethodMappedDataObject = jsonArrayMethodMappedDataObject;
  }

  public TimeUnit getTimeUnit() {
    return timeUnit;
  }

  public void setTimeUnit(TimeUnit timeUnit) {
    this.timeUnit = timeUnit;
  }

  public List<Boolean> getBooleanList() {
    return booleanList;
  }

  public void setBooleanList(List<Boolean> booleanList) {
    this.booleanList = booleanList;
  }

  public List<Short> getShortList() {
    return shortList;
  }

  public void setShortList(List<Short> shortList) {
    this.shortList = shortList;
  }

  public List<Integer> getIntegerList() {
    return integerList;
  }

  public void setIntegerList(List<Integer> integerList) {
    this.integerList = integerList;
  }

  public List<Long> getLongList() {
    return longList;
  }

  public void setLongList(List<Long> longList) {
    this.longList = longList;
  }

  public List<Float> getFloatList() {
    return floatList;
  }

  public void setFloatList(List<Float> floatList) {
    this.floatList = floatList;
  }

  public List<Double> getDoubleList() {
    return doubleList;
  }

  public void setDoubleList(List<Double> doubleList) {
    this.doubleList = doubleList;
  }

  public List<String> getStringList() {
    return stringList;
  }

  public void setStringList(List<String> stringList) {
    this.stringList = stringList;
  }

  public List<JsonObject> getJsonObjectList() {
    return jsonObjectList;
  }

  public void setJsonObjectList(List<JsonObject> jsonObjectList) {
    this.jsonObjectList = jsonObjectList;
  }

  public List<JsonArray> getJsonArrayList() {
    return jsonArrayList;
  }

  public void setJsonArrayList(List<JsonArray> jsonArrayList) {
    this.jsonArrayList = jsonArrayList;
  }

  public List<Buffer> getBufferList() {
    return bufferList;
  }

  public void setBufferList(List<Buffer> bufferList) {
    this.bufferList = bufferList;
  }

  public List<UUID> getUUIDList() {
    return uuidList;
  }

  public void setUUIDList(List<UUID> uuidList) {
    this.uuidList = uuidList;
  }

  public List<LocalDateTime> getLocalDateTimeList() {
    return localDateTimeList;
  }

  public void setLocalDateTimeList(List<LocalDateTime> localDateTimeList) {
    this.localDateTimeList = localDateTimeList;
  }

  public List<LocalDate> getLocalDateList() {
    return localDateList;
  }

  public void setLocalDateList(List<LocalDate> localDateList) {
    this.localDateList = localDateList;
  }

  public List<LocalTime> getLocalTimeList() {
    return localTimeList;
  }

  public void setLocalTimeList(List<LocalTime> localTimeList) {
    this.localTimeList = localTimeList;
  }

  public List<OffsetTime> getOffsetTimeList() {
    return offsetTimeList;
  }

  public void setOffsetTimeList(List<OffsetTime> offsetTimeList) {
    this.offsetTimeList = offsetTimeList;
  }

  public List<OffsetDateTime> getOffsetDateTimeList() {
    return offsetDateTimeList;
  }

  public void setOffsetDateTimeList(List<OffsetDateTime> offsetDateTimeList) {
    this.offsetDateTimeList = offsetDateTimeList;
  }

  public List<Temporal> getTemporalList() {
    return temporalList;
  }

  public void setTemporalList(List<Temporal> temporalList) {
    this.temporalList = temporalList;
  }

  public List<JsonObjectDataObject> getJsonObjectDataObjectList() {
    return jsonObjectDataObjectList;
  }

  public void setJsonObjectDataObjectList(List<JsonObjectDataObject> jsonObjectDataObjectList) {
    this.jsonObjectDataObjectList = jsonObjectDataObjectList;
  }

  public List<BooleanWrapper> getBooleanMethodMappedDataObjectList() {
    return booleanMethodMappedDataObjectList;
  }

  public void setBooleanMethodMappedDataObjectList(List<BooleanWrapper> booleanMethodMappedDataObjectList) {
    this.booleanMethodMappedDataObjectList = booleanMethodMappedDataObjectList;
  }

  public List<ShortWrapper> getShortMethodMappedDataObjectList() {
    return shortMethodMappedDataObjectList;
  }

  public void setShortMethodMappedDataObjectList(List<ShortWrapper> shortMethodMappedDataObjectList) {
    this.shortMethodMappedDataObjectList = shortMethodMappedDataObjectList;
  }

  public List<IntegerWrapper> getIntegerMethodMappedDataObjectList() {
    return integerMethodMappedDataObjectList;
  }

  public void setIntegerMethodMappedDataObjectList(List<IntegerWrapper> integerMethodMappedDataObjectList) {
    this.integerMethodMappedDataObjectList = integerMethodMappedDataObjectList;
  }

  public List<LongWrapper> getLongMethodMappedDataObjectList() {
    return longMethodMappedDataObjectList;
  }

  public void setLongMethodMappedDataObjectList(List<LongWrapper> longMethodMappedDataObjectList) {
    this.longMethodMappedDataObjectList = longMethodMappedDataObjectList;
  }

  public List<FloatWrapper> getFloatMethodMappedDataObjectList() {
    return floatMethodMappedDataObjectList;
  }

  public void setFloatMethodMappedDataObjectList(List<FloatWrapper> floatMethodMappedDataObjectList) {
    this.floatMethodMappedDataObjectList = floatMethodMappedDataObjectList;
  }

  public List<DoubleWrapper> getDoubleMethodMappedDataObjectList() {
    return doubleMethodMappedDataObjectList;
  }

  public void setDoubleMethodMappedDataObjectList(List<DoubleWrapper> doubleMethodMappedDataObjectList) {
    this.doubleMethodMappedDataObjectList = doubleMethodMappedDataObjectList;
  }

  public List<StringWrapper> getStringMethodMappedDataObjectList() {
    return stringMethodMappedDataObjectList;
  }

  public void setStringMethodMappedDataObjectList(List<StringWrapper> stringMethodMappedDataObjectList) {
    this.stringMethodMappedDataObjectList = stringMethodMappedDataObjectList;
  }

  public List<JsonObjectWrapper> getJsonObjectMethodMappedDataObjectList() {
    return jsonObjectMethodMappedDataObjectList;
  }

  public void setJsonObjectMethodMappedDataObjectList(List<JsonObjectWrapper> jsonObjectMethodMappedDataObjectList) {
    this.jsonObjectMethodMappedDataObjectList = jsonObjectMethodMappedDataObjectList;
  }

  public List<JsonArrayWrapper> getJsonArrayMethodMappedDataObjectList() {
    return jsonArrayMethodMappedDataObjectList;
  }

  public void setJsonArrayMethodMappedDataObjectList(List<JsonArrayWrapper> jsonArrayMethodMappedDataObjectList) {
    this.jsonArrayMethodMappedDataObjectList = jsonArrayMethodMappedDataObjectList;
  }

  public List<TimeUnit> getTimeUnitList() {
    return timeUnitList;
  }

  public void setTimeUnitList(List<TimeUnit> timeUnitList) {
    this.timeUnitList = timeUnitList;
  }

  public Set<Boolean> getBooleanSet() {
    return booleanSet;
  }

  public void setBooleanSet(Set<Boolean> booleanSet) {
    this.booleanSet = booleanSet;
  }

  public Set<Short> getShortSet() {
    return shortSet;
  }

  public void setShortSet(Set<Short> shortSet) {
    this.shortSet = shortSet;
  }

  public Set<Integer> getIntegerSet() {
    return integerSet;
  }

  public void setIntegerSet(Set<Integer> integerSet) {
    this.integerSet = integerSet;
  }

  public Set<Long> getLongSet() {
    return longSet;
  }

  public void setLongSet(Set<Long> longSet) {
    this.longSet = longSet;
  }

  public Set<Float> getFloatSet() {
    return floatSet;
  }

  public void setFloatSet(Set<Float> floatSet) {
    this.floatSet = floatSet;
  }

  public Set<Double> getDoubleSet() {
    return doubleSet;
  }

  public void setDoubleSet(Set<Double> doubleSet) {
    this.doubleSet = doubleSet;
  }

  public Set<String> getStringSet() {
    return stringSet;
  }

  public void setStringSet(Set<String> stringSet) {
    this.stringSet = stringSet;
  }

  public Set<JsonObject> getJsonObjectSet() {
    return jsonObjectSet;
  }

  public void setJsonObjectSet(Set<JsonObject> jsonObjectSet) {
    this.jsonObjectSet = jsonObjectSet;
  }

  public Set<JsonArray> getJsonArraySet() {
    return jsonArraySet;
  }

  public void setJsonArraySet(Set<JsonArray> jsonArraySet) {
    this.jsonArraySet = jsonArraySet;
  }

  public Set<Buffer> getBufferSet() {
    return bufferSet;
  }

  public void setBufferSet(Set<Buffer> bufferSet) {
    this.bufferSet = bufferSet;
  }

  public Set<UUID> getUUIDSet() {
    return uuidSet;
  }

  public void setUUIDSet(Set<UUID> uuidSet) {
    this.uuidSet = uuidSet;
  }

  public Set<LocalDateTime> getLocalDateTimeSet() {
    return localDateTimeSet;
  }

  public void setLocalDateTimeSet(Set<LocalDateTime> localDateTimeSet) {
    this.localDateTimeSet = localDateTimeSet;
  }

  public Set<LocalDate> getLocalDateSet() {
    return localDateSet;
  }

  public void setLocalDateSet(Set<LocalDate> localDateSet) {
    this.localDateSet = localDateSet;
  }

  public Set<LocalTime> getLocalTimeSet() {
    return localTimeSet;
  }

  public void setLocalTimeSet(Set<LocalTime> localTimeSet) {
    this.localTimeSet = localTimeSet;
  }

  public Set<OffsetTime> getOffsetTimeSet() {
    return offsetTimeSet;
  }

  public void setOffsetTimeSet(Set<OffsetTime> offsetTimeSet) {
    this.offsetTimeSet = offsetTimeSet;
  }

  public Set<OffsetDateTime> getOffsetDateTimeSet() {
    return offsetDateTimeSet;
  }

  public void setOffsetDateTimeSet(Set<OffsetDateTime> offsetDateTimeSet) {
    this.offsetDateTimeSet = offsetDateTimeSet;
  }

  public Set<Temporal> getTemporalSet() {
    return temporalSet;
  }

  public void setTemporalSet(Set<Temporal> temporalSet) {
    this.temporalSet = temporalSet;
  }

  public Set<JsonObjectDataObject> getJsonObjectDataObjectSet() {
    return jsonObjectDataObjectSet;
  }

  public void setJsonObjectDataObjectSet(Set<JsonObjectDataObject> jsonObjectDataObjectSet) {
    this.jsonObjectDataObjectSet = jsonObjectDataObjectSet;
  }

  public Set<BooleanWrapper> getBooleanMethodMappedDataObjectSet() {
    return booleanMethodMappedDataObjectSet;
  }

  public void setBooleanMethodMappedDataObjectSet(Set<BooleanWrapper> booleanMethodMappedDataObjectSet) {
    this.booleanMethodMappedDataObjectSet = booleanMethodMappedDataObjectSet;
  }

  public Set<ShortWrapper> getShortMethodMappedDataObjectSet() {
    return shortMethodMappedDataObjectSet;
  }

  public void setShortMethodMappedDataObjectSet(Set<ShortWrapper> shortMethodMappedDataObjectSet) {
    this.shortMethodMappedDataObjectSet = shortMethodMappedDataObjectSet;
  }

  public Set<IntegerWrapper> getIntegerMethodMappedDataObjectSet() {
    return integerMethodMappedDataObjectSet;
  }

  public void setIntegerMethodMappedDataObjectSet(Set<IntegerWrapper> integerMethodMappedDataObjectSet) {
    this.integerMethodMappedDataObjectSet = integerMethodMappedDataObjectSet;
  }

  public Set<LongWrapper> getLongMethodMappedDataObjectSet() {
    return longMethodMappedDataObjectSet;
  }

  public void setLongMethodMappedDataObjectSet(Set<LongWrapper> longMethodMappedDataObjectSet) {
    this.longMethodMappedDataObjectSet = longMethodMappedDataObjectSet;
  }

  public Set<FloatWrapper> getFloatMethodMappedDataObjectSet() {
    return floatMethodMappedDataObjectSet;
  }

  public void setFloatMethodMappedDataObjectSet(Set<FloatWrapper> floatMethodMappedDataObjectSet) {
    this.floatMethodMappedDataObjectSet = floatMethodMappedDataObjectSet;
  }

  public Set<DoubleWrapper> getDoubleMethodMappedDataObjectSet() {
    return doubleMethodMappedDataObjectSet;
  }

  public void setDoubleMethodMappedDataObjectSet(Set<DoubleWrapper> doubleMethodMappedDataObjectSet) {
    this.doubleMethodMappedDataObjectSet = doubleMethodMappedDataObjectSet;
  }

  public Set<StringWrapper> getStringMethodMappedDataObjectSet() {
    return stringMethodMappedDataObjectSet;
  }

  public void setStringMethodMappedDataObjectSet(Set<StringWrapper> stringMethodMappedDataObjectSet) {
    this.stringMethodMappedDataObjectSet = stringMethodMappedDataObjectSet;
  }

  public Set<JsonObjectWrapper> getJsonObjectMethodMappedDataObjectSet() {
    return jsonObjectMethodMappedDataObjectSet;
  }

  public void setJsonObjectMethodMappedDataObjectSet(Set<JsonObjectWrapper> jsonObjectMethodMappedDataObjectSet) {
    this.jsonObjectMethodMappedDataObjectSet = jsonObjectMethodMappedDataObjectSet;
  }

  public Set<JsonArrayWrapper> getJsonArrayMethodMappedDataObjectSet() {
    return jsonArrayMethodMappedDataObjectSet;
  }

  public void setJsonArrayMethodMappedDataObjectSet(Set<JsonArrayWrapper> jsonArrayMethodMappedDataObjectSet) {
    this.jsonArrayMethodMappedDataObjectSet = jsonArrayMethodMappedDataObjectSet;
  }

  public Set<TimeUnit> getTimeUnitSet() {
    return timeUnitSet;
  }

  public void setTimeUnitSet(Set<TimeUnit> timeUnitSet) {
    this.timeUnitSet = timeUnitSet;
  }

  public void addAddedBoolean(Boolean value) {
    addedBooleans.add(value);
  }

  public List<Boolean> getAddedBooleans() {
    return addedBooleans;
  }

  public void addAddedShort(Short value) {
    addedShorts.add(value);
  }

  public List<Short> getAddedShorts() {
    return addedShorts;
  }

  public void addAddedInteger(Integer value) {
    addedIntegers.add(value);
  }

  public List<Integer> getAddedIntegers() {
    return addedIntegers;
  }

  public void addAddedLong(Long value) {
    addedLongs.add(value);
  }

  public List<Long> getAddedLongs() {
    return addedLongs;
  }

  public void addAddedFloat(Float value) {
    addedFloats.add(value);
  }

  public List<Float> getAddedFloats() {
    return addedFloats;
  }

  public void addAddedDouble(Double value) {
    addedDoubles.add(value);
  }

  public List<Double> getAddedDoubles() {
    return addedDoubles;
  }

  public void addAddedString(String value) {
    addedStrings.add(value);
  }

  public List<String> getAddedStrings() {
    return addedStrings;
  }

  public void addAddedJsonObject(JsonObject value) {
    addedJsonObjects.add(value);
  }

  public List<JsonObject> getAddedJsonObjects() {
    return addedJsonObjects;
  }

  public void addAddedJsonArray(JsonArray value) {
    addedJsonArrays.add(value);
  }

  public List<JsonArray> getAddedJsonArrays() {
    return addedJsonArrays;
  }

  public void addAddedBuffer(Buffer value) {
    addedBuffers.add(value);
  }

  public List<Buffer> getAddedBuffers() {
    return addedBuffers;
  }

  public void addAddedUUID(UUID value) {
    addedUUIDs.add(value);
  }

  public List<UUID> getAddedUUIDs() {
    return addedUUIDs;
  }

  public void addAddedLocalDateTime(LocalDateTime value) {
    addedLocalDateTimes.add(value);
  }

  public List<LocalDateTime> getAddedLocalDateTimes() {
    return addedLocalDateTimes;
  }

  public void addAddedLocalDate(LocalDate value) {
    addedLocalDates.add(value);
  }

  public List<LocalDate> getAddedLocalDates() {
    return addedLocalDates;
  }

  public void addAddedLocalTime(LocalTime value) {
    addedLocalTimes.add(value);
  }

  public List<LocalTime> getAddedLocalTimes() {
    return addedLocalTimes;
  }

  public void addAddedOffsetTime(OffsetTime value) {
    addedOffsetTimes.add(value);
  }

  public List<OffsetTime> getAddedOffsetTimes() {
    return addedOffsetTimes;
  }

  public void addAddedOffsetDateTime(OffsetDateTime value) {
    addedOffsetDateTimes.add(value);
  }

  public List<OffsetDateTime> getAddedOffsetDateTimes() {
    return addedOffsetDateTimes;
  }

  public void addAddedTemporal(Temporal value) {
    addedTemporals.add(value);
  }

  public List<Temporal> getAddedTemporals() {
    return addedTemporals;
  }

  public void addAddedJsonObjectDataObject(JsonObjectDataObject value) {
    addedJsonObjectDataObjects.add(value);
  }

  public List<JsonObjectDataObject> getAddedJsonObjectDataObjects() {
    return addedJsonObjectDataObjects;
  }

  public void addAddedBooleanMethodMappedDataObject(BooleanWrapper value) {
    booleanAddedMethodMappedDataObjects.add(value);
  }

  public List<BooleanWrapper> getAddedBooleanMethodMappedDataObjects() {
    return booleanAddedMethodMappedDataObjects;
  }

  public void addAddedShortMethodMappedDataObject(ShortWrapper value) {
    shortAddedMethodMappedDataObjects.add(value);
  }

  public List<ShortWrapper> getAddedShortMethodMappedDataObjects() {
    return shortAddedMethodMappedDataObjects;
  }

  public void addAddedIntegerMethodMappedDataObject(IntegerWrapper value) {
    integerAddedMethodMappedDataObjects.add(value);
  }

  public List<IntegerWrapper> getAddedIntegerMethodMappedDataObjects() {
    return integerAddedMethodMappedDataObjects;
  }

  public void addAddedLongMethodMappedDataObject(LongWrapper value) {
    longAddedMethodMappedDataObjects.add(value);
  }

  public List<LongWrapper> getAddedLongMethodMappedDataObjects() {
    return longAddedMethodMappedDataObjects;
  }

  public void addAddedFloatMethodMappedDataObject(FloatWrapper value) {
    floatAddedMethodMappedDataObjects.add(value);
  }

  public List<FloatWrapper> getAddedFloatMethodMappedDataObjects() {
    return floatAddedMethodMappedDataObjects;
  }

  public void addAddedDoubleMethodMappedDataObject(DoubleWrapper value) {
    doubleAddedMethodMappedDataObjects.add(value);
  }

  public List<DoubleWrapper> getAddedDoubleMethodMappedDataObjects() {
    return doubleAddedMethodMappedDataObjects;
  }

  public void addAddedStringMethodMappedDataObject(StringWrapper value) {
    stringAddedMethodMappedDataObjects.add(value);
  }

  public List<StringWrapper> getAddedStringMethodMappedDataObjects() {
    return stringAddedMethodMappedDataObjects;
  }

  public void addAddedJsonObjectMethodMappedDataObject(JsonObjectWrapper value) {
    jsonObjectAddedMethodMappedDataObjects.add(value);
  }

  public List<JsonObjectWrapper> getAddedJsonObjectMethodMappedDataObjects() {
    return jsonObjectAddedMethodMappedDataObjects;
  }

  public void addAddedJsonArrayMethodMappedDataObject(JsonArrayWrapper value) {
    jsonArrayAddedMethodMappedDataObjects.add(value);
  }

  public List<JsonArrayWrapper> getAddedJsonArrayMethodMappedDataObjects() {
    return jsonArrayAddedMethodMappedDataObjects;
  }

  public void addAddedTimeUnit(TimeUnit value) {
    addedTimeUnits.add(value);
  }

  public List<TimeUnit> getAddedTimeUnits() {
    return addedTimeUnits;
  }
}
