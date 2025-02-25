package io.vertx.tests.pgclient.tck;

import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.tests.sqlclient.tck.ConnectionTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

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

  @Override
  protected void validateDatabaseMetaData(TestContext ctx, DatabaseMetadata md) {
    ctx.assertTrue(md.majorVersion() >= 9);
    ctx.assertTrue(md.productName().contains("PostgreSQL"));
  }
}
