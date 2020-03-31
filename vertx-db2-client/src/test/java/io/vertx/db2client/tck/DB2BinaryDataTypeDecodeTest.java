package io.vertx.db2client.tck;

import static org.junit.Assume.assumeFalse;

import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
  
  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    if (!rule.isZOS()) {
      super.testBoolean(ctx);
      return;
    }
    
    // DB2/Z does not support BOOLEAN column type, use TINYINT instead
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
    testDecodeGeneric(ctx, "test_float_8", Double.class, (double) 7.2E75);
  }
  
  @Test
  @Override
  public void testSelectAll(TestContext ctx) {
    assumeFalse(rule.isZOS());
    super.testSelectAll(ctx);
  }

}
