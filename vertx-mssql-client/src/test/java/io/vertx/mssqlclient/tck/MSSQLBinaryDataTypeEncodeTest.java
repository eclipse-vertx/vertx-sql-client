package io.vertx.mssqlclient.tck;

import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("@p").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Test
  @Ignore
  @Override
  public void testNumeric(TestContext ctx) {
    //TODO do we need wrapped type?
    super.testNumeric(ctx);
  }

  @Test
  @Ignore
  @Override
  public void testDecimal(TestContext ctx) {
    //TODO do we need wrapped type?
    super.testDecimal(ctx);
  }

}
