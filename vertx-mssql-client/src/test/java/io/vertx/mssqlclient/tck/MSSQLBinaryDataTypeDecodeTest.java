package io.vertx.mssqlclient.tck;

import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Ignore
  @Override
  public void testNullValues(TestContext ctx) {
    //TODO need to improve TCK a bit
    super.testNullValues(ctx);
  }
}
