package io.reactiverse.sqlclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

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
  public void testVarchar(TestContext ctx) {
//    testEncodeGeneric(ctx, "varchar", "VARCHAR(20)", "varchar", Tuple::getString, Row::getString, "varchar");
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
