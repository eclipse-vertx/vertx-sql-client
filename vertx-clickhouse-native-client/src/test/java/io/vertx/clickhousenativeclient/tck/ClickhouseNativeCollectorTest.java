package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.CollectorTestBase;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeCollectorTest extends CollectorTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
