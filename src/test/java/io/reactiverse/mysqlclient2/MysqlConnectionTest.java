package io.reactiverse.mysqlclient2;

import io.reactiverse.pgclient.MyClient;
import io.reactiverse.pgclient.PgConnectOptions;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MysqlConnectionTest extends MysqlTestBase {

  Vertx vertx;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(MysqlTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnect(TestContext ctx) {
    Async async = ctx.async();
    MyClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        System.out.println("Connection success!");
      } else {
        System.out.println("Connection fail!");
        ar.cause().printStackTrace();
      }
      async.complete();
    });
    async.await();
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(pgConnection -> {
      pgConnection.closeHandler(v -> {
        async.complete();
      });
      pgConnection.close();
    }));
    async.await();
  }
}
