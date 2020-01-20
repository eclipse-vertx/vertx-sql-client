package io.vertx.pgclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.tck.PreparedQueryCachedTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgPreparedQueryCachedTest extends PreparedQueryCachedTestBase {
  @ClassRule
  public static PgRule rule = PgRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }
}
