package io.vertx.mssqlclient.tck;

import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.ConnectionTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options().setDatabase("master");
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  public void tearDown(TestContext ctx) {
    super.tearDown(ctx);
  }

  /*
    TODO enable the tests when we support simple query
   */
  @Ignore
  @Test
  @Override
  public void testCloseWithErrorInProgress(TestContext ctx) {
    super.testCloseWithErrorInProgress(ctx);
  }

  @Ignore
  @Test
  @Override
  public void testCloseWithQueryInProgress(TestContext ctx) {
    super.testCloseWithQueryInProgress(ctx);
  }
}
