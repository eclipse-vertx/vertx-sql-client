package io.vertx.sqlclient;

import io.vertx.pgclient.data.Numeric;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class BinaryDataTypeEncodeTestBase extends DataTypeTestBase {
  protected abstract String statement(String... parts);

  @Test
  public void testSmallInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_2", Short.class, (short) Short.MIN_VALUE);
  }

  @Test
  public void testInteger(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_4", Integer.class, (int) Integer.MIN_VALUE);
  }

  @Test
  public void testBigInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_8", Long.class, (long) Long.MIN_VALUE);
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testEncodeGeneric(ctx, "test_float_4", Float.class, (float) -3.402823e38F);
  }

  @Test
  public void testDouble(TestContext ctx) {
    testEncodeGeneric(ctx, "test_float_8", Double.class, (double) Double.MIN_VALUE);
  }

  @Test
  public void testNumeric(TestContext ctx) {
    testEncodeGeneric(ctx, "test_numeric", Numeric.class, Numeric.parse("-999.99"));
  }

  @Test
  public void testDecimal(TestContext ctx) {
    testEncodeGeneric(ctx, "test_decimal", Numeric.class, Numeric.parse("-12345"));
  }

  @Test
  public void testChar(TestContext ctx) {
    testEncodeGeneric(ctx, "test_char", String.class, "newchar0");
  }

  @Test
  public void testVarchar(TestContext ctx) {
    testEncodeGeneric(ctx, "test_varchar", String.class, "newvarchar");
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testEncodeGeneric(ctx, "test_boolean", Boolean.class, false);
  }

  @Test
  public void testDate(TestContext ctx) {
    testEncodeGeneric(ctx, "test_date", LocalDate.class, LocalDate.parse("1999-12-31"));
  }

  @Test
  public void testTime(TestContext ctx) {
    testEncodeGeneric(ctx, "test_time", LocalTime.class, LocalTime.of(12,1,30));
  }

  protected <T> void testEncodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<T> clazz,
                                       T expected) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("UPDATE basicdatatype SET " + columnName + " = ", " WHERE id = 2"), Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 2", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue(columnName));
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
          async.complete();
        }));
      }));
    }));
  }
}
