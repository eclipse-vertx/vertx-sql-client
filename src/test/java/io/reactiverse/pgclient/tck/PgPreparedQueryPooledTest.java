package io.reactiverse.pgclient.tck;

import io.reactiverse.pgclient.junit.PgRule;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgPreparedQueryPooledTest extends PgPreparedQueryTestBase {

  @ClassRule
  public static PgRule rule = new PgRule();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }

  @Override
  public void tearDown(TestContext ctx) {
    connector.close();
    super.tearDown(ctx);
  }
}
