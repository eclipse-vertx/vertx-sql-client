package io.vertx.pgclient.tck;

import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;
import org.junit.ClassRule;

public abstract class PgPreparedQueryTestBase extends PreparedQueryTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

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
