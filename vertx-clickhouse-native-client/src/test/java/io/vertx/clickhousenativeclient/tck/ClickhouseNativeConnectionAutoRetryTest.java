package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.tck.ConnectionAutoRetryTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeConnectionAutoRetryTest extends ConnectionAutoRetryTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
  }

  @Override
  public void tearDown(TestContext ctx) {
    connectionConnector.close();
    poolConnector.close();
    super.tearDown(ctx);
  }

  @Override
  protected void initialConnector(int proxyPort) {
    SqlConnectOptions proxyOptions = new ClickhouseNativeConnectOptions(options);
    proxyOptions.setPort(proxyPort);
    proxyOptions.setHost("localhost");
    connectionConnector = ClientConfig.CONNECT.connect(vertx, proxyOptions);
    poolConnector = ClientConfig.POOLED.connect(vertx, proxyOptions);
  }
}
