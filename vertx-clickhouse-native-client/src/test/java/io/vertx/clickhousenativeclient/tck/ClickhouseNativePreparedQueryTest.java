package io.vertx.clickhousenativeclient.tck;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativePreparedQueryTest extends ClickhouseNativePreparedQueryTestBase {
  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }
}
