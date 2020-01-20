package io.vertx.pgclient.tck;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.tck.CollectorTestBase;

@RunWith(VertxUnitRunner.class)
public class PgCollectorTest extends CollectorTestBase {
  @ClassRule
  public static PgRule rule = PgRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
