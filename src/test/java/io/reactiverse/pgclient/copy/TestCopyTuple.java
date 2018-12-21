package io.reactiverse.pgclient.copy;

import io.reactiverse.pgclient.codec.DataType;
import io.reactiverse.pgclient.data.Json;
import io.reactiverse.pgclient.data.Line;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.util.UUID;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class TestCopyTuple {

  private CopyTuple tuple;

  @Before
  public void createTuple() {
    tuple = CopyTuple.tuple();
  }

  @Test
  public void testJsonTypes() {
    Json json = Json.create("{\"a\": 10");
    tuple.addJsonb(json);
    tuple.addJson(json);
    Assert.assertEquals(DataType.JSONB, tuple.getDataType(0));
    Assert.assertEquals(json, tuple.getJsonb(0));
    Assert.assertEquals(DataType.JSON, tuple.getDataType(1));
    Assert.assertEquals(json, tuple.getJson(1));
  }

  @Test
  public void testNumericTypes() {
    tuple.addInteger(100);
    tuple.addShort((short) 100);
    tuple.addDouble(10.5);
    tuple.addFloat((float)10.5);
    tuple.addLong(100L);
    tuple.addBigDecimal(new BigDecimal(100));
    Assert.assertEquals(DataType.INT4, tuple.getDataType(0));
    Assert.assertEquals(new Integer(100), tuple.getInteger(0));
    Assert.assertEquals(DataType.INT2, tuple.getDataType(1));
    Assert.assertEquals(new Short((short)100), tuple.getShort(1));
    Assert.assertEquals(DataType.FLOAT8, tuple.getDataType(2));
    Assert.assertEquals(new Double(10.5), tuple.getDouble(2));
    Assert.assertEquals(DataType.FLOAT4, tuple.getDataType(3));
    Assert.assertEquals(new Float((float)10.5), tuple.getFloat(3));
    Assert.assertEquals(DataType.INT8, tuple.getDataType(4));
    Assert.assertEquals(new Long(100), tuple.getLong(4));
  }

  @Test
  public void testTextTypes() {
    tuple.addString("hello");
    tuple.addVarChar("goodbye");
    Assert.assertEquals(DataType.TEXT, tuple.getDataType(0));
    Assert.assertEquals("hello", tuple.getString(0));
    Assert.assertEquals(DataType.VARCHAR, tuple.getDataType(1));
    Assert.assertEquals("goodbye", tuple.getVarChar(1));
  }

  @Test
  public void testDateTypes() {
    LocalDate localDate = LocalDate.now();
    tuple.addLocalDate(localDate);
    LocalTime localTime = LocalTime.now();
    tuple.addLocalTime(localTime);
    OffsetTime offsetTime = OffsetTime.now();
    tuple.addOffsetTime(offsetTime);
    LocalDateTime localDateTime = LocalDateTime.now();
    tuple.addLocalDateTime(localDateTime);
    OffsetDateTime offsetDateTime = OffsetDateTime.now();
    tuple.addOffsetDateTime(offsetDateTime);
    Assert.assertEquals(DataType.DATE, tuple.getDataType(0));
    Assert.assertEquals(localDate, tuple.getLocalDate(0));
    Assert.assertEquals(DataType.TIME, tuple.getDataType(1));
    Assert.assertEquals(localTime, tuple.getLocalTime(1));
    Assert.assertEquals(DataType.TIMETZ, tuple.getDataType(2));
    Assert.assertEquals(offsetTime, tuple.getOffsetTime(2));
    Assert.assertEquals(DataType.TIMESTAMP, tuple.getDataType(3));
    Assert.assertEquals(localDateTime, tuple.getLocalDateTime(3));
    Assert.assertEquals(DataType.TIMESTAMPTZ, tuple.getDataType(4));
    Assert.assertEquals(offsetDateTime, tuple.getOffsetDateTime(4));
  }

  @Test
  public void testUuidType() {
    UUID uuid = UUID.randomUUID();
    tuple.addUUID(uuid);
    Assert.assertEquals(DataType.UUID, tuple.getDataType(0));
    Assert.assertEquals(uuid, tuple.getUUID(0));
  }

  @Test
  public void testBooleanType() {
    tuple.addBoolean(true);
    Assert.assertEquals(DataType.BOOL, tuple.getDataType(0));
    Assert.assertEquals(true, tuple.getBoolean(0));
  }

  @Test
  public void testArrayTypes() {
    tuple.addValue(new Line[] {null});
    Assert.assertEquals(DataType.LINE_ARRAY, tuple.getDataType(0));
  }
}
