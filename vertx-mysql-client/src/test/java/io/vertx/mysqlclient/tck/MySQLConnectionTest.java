package io.vertx.mysqlclient.tck;

import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.tck.ConnectionTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

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
    if (rule.isUsingMariaDB()) {
      ctx.assertTrue(md.majorVersion() >= 10, "Expected DB major version >= 10 but was " + md.majorVersion());
    }
    else if (rule.isUsingMySQL5_6()) {
      ctx.assertEquals(5, md.majorVersion());
      ctx.assertEquals(6, md.minorVersion());
    }
    else if (rule.isUsingMySQL8()) {
      ctx.assertEquals(8, md.majorVersion());
    }
    else {
      ctx.assertTrue(md.majorVersion() >= 5, "Expected DB major version >= 5 but was " + md.majorVersion());
    }
  }
}
