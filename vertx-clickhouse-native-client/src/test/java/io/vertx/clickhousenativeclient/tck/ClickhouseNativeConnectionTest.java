package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.tck.ConnectionTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

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
    ctx.assertTrue(md.majorVersion() >= 20);
    ctx.assertTrue(md.productName().toLowerCase().contains("ClickHouse".toLowerCase()));
  }
}
