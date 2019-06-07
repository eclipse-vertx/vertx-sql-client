package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLUtilityCommandTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPingCommand(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.ping(ctx.asyncAssertSuccess(v -> {
        conn.close();
      }));
    }));
  }

  @Test
  public void testChangeSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT DATABASE();", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn.specifySchema("emptyschema", ctx.asyncAssertSuccess(v -> {
          conn.query("SELECT DATABASE();", ctx.asyncAssertSuccess(result2 -> {
            ctx.assertEquals("emptyschema", result2.iterator().next().getString(0));
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testChangeToInvalidSchema(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT DATABASE();", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals("testschema", result.iterator().next().getString(0));
        conn.specifySchema("invalidschema", ctx.asyncAssertFailure(error -> {
          conn.close();
        }));
      }));
    }));
  }
}
