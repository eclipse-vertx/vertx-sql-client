package io.reactiverse.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MysqlCommandTest extends MysqlTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MysqlTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPing(TestContext ctx) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.ping(ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Ping succeed");
          } else {
            System.out.println("Ping fail");
            ar2.cause().printStackTrace();
          }
          async.complete();
        });
      } else {
        System.out.println("Connection fail!");
        ar.cause().printStackTrace();
      }
    });
    async.await();
  }

  @Test
  public void testSuccessiveCommands(TestContext ctx) {
    Async async = ctx.async(2);

    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.ping(ar1 -> {
          if (ar1.succeeded()) {
            System.out.println("Ping 1 succeed");
          } else {
            System.out.println("Ping 1 fail");
            ar1.cause().printStackTrace();
          }
          async.countDown();
        });
        mySQLConnection.ping(ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Ping 2 succeed");
          } else {
            System.out.println("Ping 2 fail");
            ar2.cause().printStackTrace();
          }
          async.countDown();
        });
      } else {
        System.out.println("Connection fail!");
        ar.cause().printStackTrace();
      }
    });
    async.await();
  }
}
