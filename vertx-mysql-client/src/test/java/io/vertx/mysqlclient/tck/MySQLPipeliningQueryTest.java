package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLPool;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.tck.PipeliningQueryTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLPipeliningQueryTest extends PipeliningQueryTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected void init() {
    options = rule.options();
    MySQLConnectOptions mySQLConnectOptions = (MySQLConnectOptions) options;
    mySQLConnectOptions.setPipeliningLimit(64);
    connectionConnector = ClientConfig.CONNECT.connect(vertx, options);
    pooledConnectionConnector = ClientConfig.POOLED.connect(vertx, options);
    pooledClientSupplier = () -> MySQLPool.client(vertx, (MySQLConnectOptions) options, new PoolOptions().setMaxSize(8));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
}
