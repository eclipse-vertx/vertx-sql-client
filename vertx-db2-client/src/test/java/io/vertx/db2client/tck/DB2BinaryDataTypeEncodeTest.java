package io.vertx.db2client.tck;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
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
  
}
