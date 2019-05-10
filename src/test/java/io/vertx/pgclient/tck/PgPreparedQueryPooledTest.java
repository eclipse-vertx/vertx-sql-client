package io.vertx.pgclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgPreparedQueryPooledTest extends PgPreparedQueryTestBase {
  @Override
  protected void initConnector() {
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }
}
