package io.vertx.pgclient.tck;

import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.ConnectionTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static PgRule rule = new PgRule();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  public void tearDown(TestContext ctx) {
    connector.close();
    super.tearDown(ctx);
  }
}
