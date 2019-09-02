package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLTlsTest {

  @ClassRule
  public static MySQLRule rule = new MySQLRule().ssl(true);

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(rule.options());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testFailWithTlsDisabled(TestContext ctx) {
    options.setSsl(false);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertFailure(error -> {
      // TLS support is forced to be enabled on the client side
    }));
  }
}
