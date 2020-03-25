package io.vertx.sqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class BinaryDataTypeDecodeTestBase extends DataTypeTestBase {
  @Test
  public void testSmallInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_2", Short.class, (short) 32767);
  }

  @Test
  public void testInteger(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_4", Integer.class, (int) 2147483647);
  }

  @Test
  public void testBigInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_8", Long.class, (long) 9223372036854775807L);
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_4", Float.class, (float) 3.40282e38F);
  }

  @Test
  public void testDouble(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_8", Double.class, (double) 1.7976931348623157E308);
  }

  @Test
  public void testNumeric(TestContext ctx) {
    testDecodeGeneric(ctx, "test_numeric", Numeric.class, Numeric.parse("999.99"));
  }

  @Test
  public void testDecimal(TestContext ctx) {
    testDecodeGeneric(ctx, "test_decimal", Numeric.class, Numeric.parse("12345"));
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testDecodeGeneric(ctx, "test_boolean", Boolean.class, true);
  }

  @Test
  public void testChar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_char", String.class, "testchar");
  }

  @Test
  public void testVarchar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_varchar", String.class, "testvarchar");
  }

  @Test
  public void testDate(TestContext ctx) {
    testDecodeGeneric(ctx, "test_date", LocalDate.class, LocalDate.of(2019, 1, 1));
  }

  @Test
  public void testTime(TestContext ctx) {
    testDecodeGeneric(ctx, "test_time", LocalTime.class, LocalTime.of(18, 45, 2));
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<T> clazz,
                                       T expected) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 1").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
        conn.close();
      }));
    }));
  }

  @Test
  public void testNullValues(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT " +
        "test_int_2," +
        "test_int_4," +
        "test_int_8," +
        "test_float_4," +
        "test_float_8," +
        "test_numeric," +
        "test_decimal," +
        "test_boolean," +
        "test_char," +
        "test_varchar," +
        "test_date," +
        "test_time " +
        "from basicdatatype where id = 3").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(12, row.size());
        for (int i = 0; i < 12; i++) {
          ctx.assertNull(row.getValue(i));
        }
        conn.close();
      }));
    }));
  }

}
