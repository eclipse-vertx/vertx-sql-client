package io.vertx.mysqlclient.tck;

import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.BinaryDataTypeEncodeTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  @ClassRule
  public static MySQLRule rule = new MySQLRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Ignore
  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    // does not pass due to it's TINYINT type
    super.testBoolean(ctx);
  }

  @Ignore
  @Test
  @Override
  public void testTime(TestContext ctx) {
    // does not pass because of no implementation
    super.testTime(ctx);
  }
}
