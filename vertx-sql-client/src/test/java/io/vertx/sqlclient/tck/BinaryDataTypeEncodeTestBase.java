package io.vertx.sqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class BinaryDataTypeEncodeTestBase extends DataTypeTestBase {
  protected abstract String statement(String... parts);

  @Test
  public void testSmallInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_2", Short.class, Row::getShort, (short) Short.MIN_VALUE);
  }

  @Test
  public void testInteger(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_4", Integer.class, Row::getInteger, (int) Integer.MIN_VALUE);
  }

  @Test
  public void testBigInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_8", Long.class, Row::getLong, (long) Long.MIN_VALUE);
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testEncodeGeneric(ctx, "test_float_4", Float.class, Row::getFloat, (float) -3.402823e38F);
  }

  @Test
  public void testDouble(TestContext ctx) {
    testEncodeGeneric(ctx, "test_float_8", Double.class, Row::getDouble, (double) Double.MIN_VALUE);
  }

  @Test
  public void testNumeric(TestContext ctx) {
    testEncodeGeneric(ctx, "test_numeric", Numeric.class, null, Numeric.parse("-999.99"));
  }

  @Test
  public void testDecimal(TestContext ctx) {
    testEncodeGeneric(ctx, "test_decimal", Numeric.class, null, Numeric.parse("-12345"));
  }

  @Test
  public void testChar(TestContext ctx) {
    testEncodeGeneric(ctx, "test_char", String.class, Row::getString, "newchar0");
  }

  @Test
  public void testVarchar(TestContext ctx) {
    testEncodeGeneric(ctx, "test_varchar", String.class, Row::getString, "newvarchar");
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testEncodeGeneric(ctx, "test_boolean", Boolean.class, Row::getBoolean, false);
  }

  @Test
  public void testDate(TestContext ctx) {
    testEncodeGeneric(ctx, "test_date", LocalDate.class, Row::getLocalDate, LocalDate.parse("1999-12-31"));
  }

  @Test
  public void testTime(TestContext ctx) {
    testEncodeGeneric(ctx, "test_time", LocalTime.class, Row::getLocalTime, LocalTime.of(12,1,30));
  }

  protected <T> void testEncodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<T> clazz,
                                       BiFunction<Row,String,T> getter,
                                       T expected) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(statement("UPDATE basicdatatype SET " + columnName + " = ", " WHERE id = 2")).execute(Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 2").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue(columnName));
          if (getter != null) {
            ctx.assertEquals(expected, getter.apply(row, columnName));
          }
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
          conn.close();
        }));
      }));
    }));
  }
}
