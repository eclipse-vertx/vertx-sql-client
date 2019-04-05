package io.vertx.mysqlclient.tck;

import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.SimpleQueryTestBase;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLSimpleQueryPooledTest extends SimpleQueryTestBase {
  @ClassRule
  public static MySQLRule rule = new MySQLRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }
}
