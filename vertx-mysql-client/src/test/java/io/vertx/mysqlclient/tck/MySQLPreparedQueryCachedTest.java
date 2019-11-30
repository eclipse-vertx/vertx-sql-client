package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.tck.PreparedQueryCachedTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLPreparedQueryCachedTest extends PreparedQueryCachedTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test
  @Ignore
  @Override
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    // we use implicit type conversion for MySQL client
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }
}
