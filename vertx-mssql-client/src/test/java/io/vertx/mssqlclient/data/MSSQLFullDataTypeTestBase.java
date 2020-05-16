package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

public abstract class MSSQLFullDataTypeTestBase extends MSSQLDataTypeTestBase{

  @Test
  public void testDecodeAllColumns(TestContext ctx) {
    testDecodeNotNullValue(ctx, "*", row -> {
      ctx.assertEquals((short) 127, row.getValue("test_tinyint"));
      ctx.assertEquals((short) 32767, row.getValue("test_smallint"));
      ctx.assertEquals(2147483647, row.getValue("test_int"));
      ctx.assertEquals(9223372036854775807L, row.getValue("test_bigint"));
      ctx.assertEquals((float) 3.40282E38, row.getValue("test_float_4"));
      ctx.assertEquals(1.7976931348623157E308, row.getValue("test_float_8"));
      ctx.assertEquals(Numeric.create(999.99), row.getValue("test_numeric"));
      ctx.assertEquals(Numeric.create(12345), row.getValue("test_decimal"));
      ctx.assertEquals(true, row.getValue("test_boolean"));
      ctx.assertEquals("testchar", row.getValue("test_char"));
      ctx.assertEquals("testvarchar", row.getValue("test_varchar"));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getValue("test_date"));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.getValue("test_time"));
    });
  }

  @Test
  public void testDecodeTinyInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_tinyint", row -> {
      ctx.assertEquals((short) 127, row.getValue("test_tinyint"));
      ctx.assertEquals((short) 127, row.getValue(0));
      ctx.assertEquals((short) 127, row.getShort(0));
      ctx.assertEquals((short) 127, row.getShort(0));
      ctx.assertEquals((short) 127, row.get(Short.class, "test_tinyint"));
      ctx.assertEquals((short) 127, row.get(Short.class, 0));
    });
  }

  @Test
  public void testDecodeSmallIntInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_smallint", row -> {
      ctx.assertEquals((short) 32767, row.getValue("test_smallint"));
      ctx.assertEquals((short) 32767, row.getValue(0));
      ctx.assertEquals((short) 32767, row.getShort("test_smallint"));
      ctx.assertEquals((short) 32767, row.getShort(0));
      ctx.assertEquals((short) 32767, row.get(Short.class, "test_smallint"));
      ctx.assertEquals((short) 32767, row.get(Short.class, 0));
    });
  }

  @Test
  public void testDecodeInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_int", row -> {
      ctx.assertEquals(2147483647, row.getValue("test_int"));
      ctx.assertEquals(2147483647, row.getValue(0));
      ctx.assertEquals(2147483647, row.getInteger("test_int"));
      ctx.assertEquals(2147483647, row.getInteger(0));
      ctx.assertEquals(2147483647, row.get(Integer.class, "test_int"));
      ctx.assertEquals(2147483647, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testDecodeBigInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_bigint", row -> {
      ctx.assertEquals(9223372036854775807L, row.getValue("test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.getValue(0));
      ctx.assertEquals(9223372036854775807L, row.getLong("test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.getLong(0));
      ctx.assertEquals(9223372036854775807L, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.get(Long.class, 0));
    });
  }

  @Test
  public void testDecodeFloat4(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_float_4", row -> {
      ctx.assertEquals((float) 3.40282E38, row.getValue("test_float_4"));
      ctx.assertEquals((float) 3.40282E38, row.getValue(0));
      ctx.assertEquals((float) 3.40282E38, row.getFloat("test_float_4"));
      ctx.assertEquals((float) 3.40282E38, row.getFloat(0));
      ctx.assertEquals((float) 3.40282E38, row.get(Float.class, "test_float_4"));
      ctx.assertEquals((float) 3.40282E38, row.get(Float.class, 0));
    });
  }

  @Test
  public void testDecodeFloat8(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_float_8", row -> {
      ctx.assertEquals(1.7976931348623157E308, row.getValue("test_float_8"));
      ctx.assertEquals(1.7976931348623157E308, row.getValue(0));
      ctx.assertEquals(1.7976931348623157E308, row.getDouble("test_float_8"));
      ctx.assertEquals(1.7976931348623157E308, row.getDouble(0));
      ctx.assertEquals(1.7976931348623157E308, row.get(Double.class, "test_float_8"));
      ctx.assertEquals(1.7976931348623157E308, row.get(Double.class, 0));
    });
  }

  @Test
  public void testDecodeNumeric(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_numeric", row -> {
      ctx.assertEquals(Numeric.create(999.99), row.getValue("test_numeric"));
      ctx.assertEquals(Numeric.create(999.99), row.getValue(0));
      ctx.assertEquals(Numeric.create(999.99), row.get(Numeric.class, "test_numeric"));
      ctx.assertEquals(Numeric.create(999.99), row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testDecodeDecimal(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_decimal", row -> {
      ctx.assertEquals(Numeric.create(12345), row.getValue("test_decimal"));
      ctx.assertEquals(Numeric.create(12345), row.getValue(0));
      ctx.assertEquals(Numeric.create(12345), row.get(Numeric.class, "test_decimal"));
      ctx.assertEquals(Numeric.create(12345), row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testDecodeBit(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_boolean", row -> {
      ctx.assertEquals(true, row.getValue("test_boolean"));
      ctx.assertEquals(true, row.getValue(0));
      ctx.assertEquals(true, row.getBoolean("test_boolean"));
      ctx.assertEquals(true, row.getBoolean(0));
      ctx.assertEquals(true, row.get(Boolean.class, "test_boolean"));
      ctx.assertEquals(true, row.get(Boolean.class, 0));
    });
  }

  @Test
  public void testDecodeChar(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_char", row -> {
      ctx.assertEquals("testchar", row.getValue("test_char"));
      ctx.assertEquals("testchar", row.getValue(0));
      ctx.assertEquals("testchar", row.getString("test_char"));
      ctx.assertEquals("testchar", row.getString(0));
      ctx.assertEquals("testchar", row.get(String.class, "test_char"));
      ctx.assertEquals("testchar", row.get(String.class, 0));
    });
  }

  @Test
  public void testDecodeVarChar(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_varchar", row -> {
      ctx.assertEquals("testvarchar", row.getValue("test_varchar"));
      ctx.assertEquals("testvarchar", row.getValue(0));
      ctx.assertEquals("testvarchar", row.getString("test_varchar"));
      ctx.assertEquals("testvarchar", row.getString(0));
      ctx.assertEquals("testvarchar", row.get(String.class, "test_varchar"));
      ctx.assertEquals("testvarchar", row.get(String.class, 0));
    });
  }

  @Test
  public void testDecodeDate(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_date", row -> {
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getValue("test_date"));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getValue(0));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getLocalDate("test_date"));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.getLocalDate(0));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.get(LocalDate.class, "test_date"));
      ctx.assertEquals(LocalDate.of(2019, 1, 1), row.get(LocalDate.class, 0));
    });
  }

  @Test
  public void testDecodeTime(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_time", row -> {
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.getValue("test_time"));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.getValue(0));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.getLocalTime("test_time"));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.getLocalTime(0));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.get(LocalTime.class, "test_time"));
      ctx.assertEquals(LocalTime.of(18, 45, 2), row.get(LocalTime.class, 0));
    });
  }

  protected abstract void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker);

  private void testDecodeNotNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, false, columnName, checker);
  }
}
