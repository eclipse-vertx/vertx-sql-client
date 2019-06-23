package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.tck.PreparedBatchTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLPreparedBatchTest extends PreparedBatchTestBase {
  @ClassRule
  public static MySQLRule rule = new MySQLRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
}
