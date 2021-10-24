package io.vertx.mssqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.tck.PreparedBatchTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedBatchTest extends PreparedBatchTestBase {
  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

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

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Ignore
  @Override
  public void testIncorrectNumBatchArguments(TestContext ctx) {
    super.testIncorrectNumBatchArguments(ctx);
  }
}
