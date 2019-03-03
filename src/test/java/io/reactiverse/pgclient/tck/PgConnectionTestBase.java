package io.reactiverse.pgclient.tck;

import io.reactiverse.pgclient.junit.PgRule;
import io.reactiverse.sqlclient.ConnectionTestBase;
import org.junit.ClassRule;

public abstract class PgConnectionTestBase extends ConnectionTestBase {

  @ClassRule
  public static PgRule rule = new PgRule();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
  }
}
