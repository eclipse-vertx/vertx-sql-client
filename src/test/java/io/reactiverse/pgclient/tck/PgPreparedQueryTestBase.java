package io.reactiverse.pgclient.tck;

import io.reactiverse.pgclient.junit.PgRule;
import io.reactiverse.sqlclient.PreparedQueryTestBase;
import org.junit.ClassRule;

public abstract class PgPreparedQueryTestBase extends PreparedQueryTestBase {
  @ClassRule
  public static PgRule rule = new PgRule();

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
