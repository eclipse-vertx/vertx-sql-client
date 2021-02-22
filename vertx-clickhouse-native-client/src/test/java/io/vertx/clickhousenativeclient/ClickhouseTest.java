package io.vertx.clickhousenativeclient;

import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnectOptions;
import io.vertx.clickhouse.clickhousenative.ClickhouseNativeConnection;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlClient;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseTest {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();
  private ClickhouseNativeConnectOptions options;
  private Vertx vertx;

  @Before
  public void setup() {
    options = rule.options();
    vertx = Vertx.vertx();
  }

  @After
  public void teardDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void baseConnectTest(TestContext ctx) {
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(SqlClient::close));
  }

  @Test
  public void baseQueryTest(TestContext ctx) {
    ClickhouseNativeConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("select 4 as resource, 'aa' as str_col1, CAST('abcdef', 'FixedString(6)') as str_col2").execute(
        ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          conn.close();
        })
      );
    }));
  }
}
