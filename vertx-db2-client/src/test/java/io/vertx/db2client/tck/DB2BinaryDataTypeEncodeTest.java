package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
  
  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test // @AGG TODO
  @Ignore("Works in JDBC but fails with Vertx, we get sqlCode=-302 sqlState=22003 which means value is too large")
  @Override
  public void testDouble(TestContext ctx) {
    super.testDouble(ctx);
  }
  
  @Override
  public void testBoolean(TestContext ctx) {
    if (!rule.isZOS()) {
      super.testBoolean(ctx);
      return;
    }
    
    // DB2/Z doesn't have a BOOLEAN column type and uses TINYINT instead
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("UPDATE basicdatatype SET test_boolean = ? WHERE id = 2")
        .execute(Tuple.tuple().addValue(false), ctx.asyncAssertSuccess(updateResult -> {
        conn
          .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 2")
          .execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals((short) 0, row.getValue(0));
          ctx.assertEquals((short) 0, row.getValue("test_boolean"));
          ctx.assertEquals(false, row.getBoolean(0));
          conn.close();
        }));
      }));
    }));
  }

}
