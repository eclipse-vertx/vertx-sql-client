package io.reactiverse.sqlclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

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
    testDecodeGeneric(ctx, "test_float_4", Float.class, (float) 3.4028235E38);
  }

  @Test
  public void testDouble(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_8", Double.class, (double) 1.7976931348623157E308);
  }

  @Test
  public void testVarchar(TestContext ctx) {
//    testDecodeGeneric(ctx, "varchar", "VARCHAR(20)", "varchar", Tuple::getString, Row::getString, "varchar");
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<T> clazz,
                                       T expected) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
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
  }
}
