package io.reactiverse.mysqlclient;

import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
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

  @Test
  public void testExecute(TestContext ctx) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.prepare("SELECT * FROM BasicDataType WHERE id = ?", ar2 -> {
          if (ar2.succeeded()) {
            MySQLPreparedQuery mySQLPreparedQuery = ar2.result();
            System.out.println("prepare success");
            mySQLPreparedQuery.execute(Tuple.of(1), ar3 -> {
              if (ar3.succeeded()) {
                System.out.println("Query succeed");
                PgRowSet rowSet = ar3.result();
                ctx.assertEquals(1, rowSet.size());
                Row row = rowSet.iterator().next();
                ctx.assertEquals(1, row.getInteger(0));
                ctx.assertEquals((short) 32767, row.getShort(1));
                ctx.assertEquals(8388607, row.getInteger(2));
                ctx.assertEquals(2147483647, row.getInteger(3));
                ctx.assertEquals(9223372036854775807L, row.getLong(4));
                ctx.assertEquals(123.456f, row.getFloat(5));
                ctx.assertEquals(1.234567d, row.getDouble(6));
                ctx.assertEquals("HELLO,WORLD", row.getString(7));
              } else {
                System.out.println("Query fail");
                ar3.cause().printStackTrace();
              }
              async.complete();
            });
          } else {
            System.out.println("Query fail");
            ar2.cause().printStackTrace();
          }
        });
      } else {
        System.out.println("Connection fail!");
        ar.cause().printStackTrace();
      }
    });
    async.await();
  }
}
