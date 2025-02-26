package io.vertx.tests.db2client.tck;

import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;

@RunWith(VertxUnitRunner.class)
public class DB2PreparedQueryPooledTest extends DB2PreparedQueryTestBase {
  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.POOLED.connect(vertx, options);
  }
}
