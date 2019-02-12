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
public class MysqlPreparedStatementTest extends MysqlTestBase {
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
  public void testPrepare(TestContext ctx) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.prepare("SELECT * FROM BasicDataType WHERE id = ?", ar2 -> {
          if (ar2.succeeded()) {
            MySQLPreparedQuery mySQLPreparedQuery = ar2.result();
            System.out.println("prepare success");
          } else {
            System.out.println("Query fail");
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
}
