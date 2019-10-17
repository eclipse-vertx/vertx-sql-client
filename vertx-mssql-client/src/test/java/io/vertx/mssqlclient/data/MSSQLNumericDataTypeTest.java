package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLNumericDataTypeTest extends MSSQLDataTypeTestBase {
  @Test
  public void testQueryLargeNumeric(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", Numeric.parse("99999999999999999999999999999999999999"));
  }

  @Test
  public void testPreparedQueryLargeNumeric(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", Numeric.parse("99999999999999999999999999999999999999"));
  }

  @Test
  public void testQueryLargeDecimal(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", Numeric.parse("99999999999999999999.9999999999"));
  }

  @Test
  public void testPreparedQueryLargeDecimal(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", Numeric.parse("99999999999999999999.9999999999"));
  }
}
