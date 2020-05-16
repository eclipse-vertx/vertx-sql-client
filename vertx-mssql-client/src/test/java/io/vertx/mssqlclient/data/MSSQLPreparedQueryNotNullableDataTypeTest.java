package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNotNullableDataTypeTest extends MSSQLNotNullableDataTypeTestBase {
  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_tinyint", (short) 255, row -> {
      ctx.assertEquals((short) 255, row.getValue("test_tinyint"));
      ctx.assertEquals((short) 255, row.getValue(0));
      ctx.assertEquals((short) 255, row.get(Short.class, "test_tinyint"));
      ctx.assertEquals((short) 255, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_smallint", (short) -32768, row -> {
      ctx.assertEquals((short) -32768, row.getValue("test_smallint"));
      ctx.assertEquals((short) -32768, row.getValue(0));
      ctx.assertEquals((short) -32768, row.getShort("test_smallint"));
      ctx.assertEquals((short) -32768, row.getShort(0));
      ctx.assertEquals((short) -32768, row.get(Short.class, "test_smallint"));
      ctx.assertEquals((short) -32768, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_int", -2147483648, row -> {
      ctx.assertEquals(-2147483648, row.getValue("test_int"));
      ctx.assertEquals(-2147483648, row.getValue(0));
      ctx.assertEquals(-2147483648, row.getInteger("test_int"));
      ctx.assertEquals(-2147483648, row.getInteger(0));
      ctx.assertEquals(-2147483648, row.get(Integer.class, "test_int"));
      ctx.assertEquals(-2147483648, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_bigint", -9223372036854775808L, row -> {
      ctx.assertEquals(-9223372036854775808L, row.getValue("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getValue(0));
      ctx.assertEquals(-9223372036854775808L, row.getLong("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getLong(0));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, 0));
    });
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_float_4", -3.40282E38, row -> {
      ctx.assertEquals((float) -3.40282E38, row.getValue("test_float_4"));
      ctx.assertEquals((float) -3.40282E38, row.getValue(0));
      ctx.assertEquals((float) -3.40282E38, row.getFloat("test_float_4"));
      ctx.assertEquals((float) -3.40282E38, row.getFloat(0));
      ctx.assertEquals((float) -3.40282E38, row.get(Float.class, "test_float_4"));
      ctx.assertEquals((float) -3.40282E38, row.get(Float.class, 0));
    });
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_float_8", -1.7976931348623157E308, row -> {
      ctx.assertEquals(-1.7976931348623157E308, row.getValue("test_float_8"));
      ctx.assertEquals(-1.7976931348623157E308, row.getValue(0));
      ctx.assertEquals(-1.7976931348623157E308, row.getDouble("test_float_8"));
      ctx.assertEquals(-1.7976931348623157E308, row.getDouble(0));
      ctx.assertEquals(-1.7976931348623157E308, row.get(Double.class, "test_float_8"));
      ctx.assertEquals(-1.7976931348623157E308, row.get(Double.class, 0));
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeNumeric(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_numeric", Numeric.create(new BigDecimal("123456789.127")), row -> {
      ctx.assertEquals(Numeric.create(123456789.13), row.getValue("test_numeric"));
      ctx.assertEquals(Numeric.create(123456789.13), row.getValue(0));
      ctx.assertEquals(Numeric.create(123456789.13), row.get(Numeric.class, "test_numeric"));
      ctx.assertEquals(Numeric.create(123456789.13), row.get(Numeric.class, 0));
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeDecimal(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_decimal", Numeric.create(new BigDecimal("123456789.127")), row -> {
      ctx.assertEquals(Numeric.create(123456789), row.getValue("test_decimal"));
      ctx.assertEquals(Numeric.create(123456789), row.getValue(0));
      ctx.assertEquals(Numeric.create(123456789), row.get(Numeric.class, "test_decimal"));
      ctx.assertEquals(Numeric.create(123456789), row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testEncodeBit(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_boolean", false, row -> {
      ctx.assertEquals(false, row.getValue("test_boolean"));
      ctx.assertEquals(false, row.getValue(0));
      ctx.assertEquals(false, row.getBoolean("test_boolean"));
      ctx.assertEquals(false, row.getBoolean(0));
      ctx.assertEquals(false, row.get(Boolean.class, "test_boolean"));
      ctx.assertEquals(false, row.get(Boolean.class, 0));
    });
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_char", "chartest", row -> {
      ctx.assertEquals("chartest", row.getValue("test_char"));
      ctx.assertEquals("chartest", row.getValue(0));
      ctx.assertEquals("chartest", row.getString("test_char"));
      ctx.assertEquals("chartest", row.getString(0));
      ctx.assertEquals("chartest", row.get(String.class, "test_char"));
      ctx.assertEquals("chartest", row.get(String.class, 0));
    });
  }

  @Test
  public void testEncodeVarChar(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_varchar", "testedvarchar", row -> {
      ctx.assertEquals("testedvarchar", row.getValue("test_varchar"));
      ctx.assertEquals("testedvarchar", row.getValue(0));
      ctx.assertEquals("testedvarchar", row.getString("test_varchar"));
      ctx.assertEquals("testedvarchar", row.getString(0));
      ctx.assertEquals("testedvarchar", row.get(String.class, "test_varchar"));
      ctx.assertEquals("testedvarchar", row.get(String.class, 0));
    });
  }

  @Test
  public void testEncodeDate(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_date", LocalDate.of(1999, 12, 31), row -> {
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.getValue("test_date"));
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.getValue(0));
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.getLocalDate("test_date"));
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.getLocalDate(0));
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.get(LocalDate.class, "test_date"));
      ctx.assertEquals(LocalDate.of(1999, 12, 31), row.get(LocalDate.class, 0));
    });
  }

  @Test
  public void testEncodeTime(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_time", LocalTime.of(23, 10, 45), row -> {
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.getValue("test_time"));
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.getValue(0));
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.getLocalTime("test_time"));
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.getLocalTime(0));
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.get(LocalTime.class, "test_time"));
      ctx.assertEquals(LocalTime.of(23, 10, 45), row.get(LocalTime.class, 0));
    });
  }

  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    testPreparedQueryDecodeGeneric(ctx, "not_nullable_datatype", columnName, "1", checker);
  }
}
