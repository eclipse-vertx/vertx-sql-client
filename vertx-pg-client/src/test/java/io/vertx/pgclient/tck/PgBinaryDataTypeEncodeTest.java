package io.vertx.pgclient.tck;

import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
    @ClassRule
  public static PgRule rule = new PgRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
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
