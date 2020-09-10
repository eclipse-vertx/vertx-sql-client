package io.vertx.db2client.tck;

import static org.junit.Assume.assumeFalse;

import java.sql.JDBCType;
import java.time.LocalTime;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    System.out.println(">>> BEGIN " + getClass().getSimpleName() + "." + testName.getMethodName());
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    // DB2/Z does not support BOOLEAN column type, use TINYINT instead
    // DB2/LUW has a BOOLEAN column type but it is just an alias for TINYINT
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 1")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals((short) 1, row.getValue(0));
        ctx.assertEquals((short) 1, row.getValue("test_boolean"));
        ctx.assertEquals(true, row.getBoolean(0));
        conn.close();
      }));
    }));
  }

  @Test
  @Override
  public void testDouble(TestContext ctx) {
    if (!rule.isZOS()) {
      super.testDouble(ctx);
      return;
    }

    // For DB2/z the largest value that can be stored in a DOUBLE column is 7.2E75
    testDecodeGeneric(ctx, "test_float_8", Double.class, JDBCType.DOUBLE, (double) 7.2E75);
  }
  
  @Override
  public void testChar(TestContext ctx) {
    // Override to expecting JDBCType.CHAR instead of VARCHAR
    testDecodeGeneric(ctx, "test_char", String.class, JDBCType.CHAR, "testchar");
  }
  
  @Override
  public void testNumeric(TestContext ctx) {
    // Override to expecting JDBCType.DECIMAL instead of NUMERIC
    testDecodeGeneric(ctx, "test_numeric", Numeric.class, JDBCType.DECIMAL, Numeric.parse("999.99"));
  }
  
  @Override
  public void testDecimal(TestContext ctx) {
    // Override to expecting JDBCType.DECIMAL instead of NUMERIC
    testDecodeGeneric(ctx, "test_decimal", Numeric.class, JDBCType.DECIMAL, Numeric.parse("12345"));
  }
  
  @Override
  public void testTime(TestContext ctx) {
    // Override to expecting JDBCType.TIME instead of DATE
    testDecodeGeneric(ctx, "test_time", LocalTime.class, JDBCType.TIME, LocalTime.of(18, 45, 2));
  }

  @Test
  @Override
  public void testSelectAll(TestContext ctx) {
    assumeFalse(rule.isZOS());
    super.testSelectAll(ctx);
  }

}
