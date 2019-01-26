package io.reactiverse.mysqlclient;

import io.reactiverse.pgclient.PgResult;
import io.reactiverse.pgclient.PgRowSet;
import io.reactiverse.pgclient.Row;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class MysqlQueryTest extends MysqlTestBase {
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
  public void test1(TestContext ctx) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.query("SELECT * FROM BasicDataType", ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Query succeed");
            PgRowSet rowSet = ar2.result();
            //TODO It seems PgRowSet#rowCount() can not be built from MySQL OK_Packet response(semantics collision)?
//            ctx.assertEquals(2, rowSet.rowCount());
            ctx.assertEquals(2, rowSet.size());
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
  public void test2(TestContext ctx) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.query("SELECT * FROM BasicDataType WHERE id = 2", ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Query succeed");
            PgRowSet rowSet = ar2.result();
            ctx.assertEquals(1, rowSet.size());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(2, row.getInteger("id"));
            ctx.assertEquals((short) 32767, row.getShort("Int2"));
            ctx.assertEquals(8388607, row.getInteger("Int3"));
            ctx.assertEquals(2147483647, row.getInteger("Int4"));
            ctx.assertEquals(9223372036854775807L, row.getLong("Int8"));
            ctx.assertEquals(123.456f, row.getFloat("Float"));
            ctx.assertEquals(1.234567d, row.getDouble("Double"));
            ctx.assertEquals("hello,world", row.getString("Varchar"));
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
  public void testCollector(TestContext ctx) {
    Async async = ctx.async();

    Collector<Row, ?, Map<Integer, DummyObject>> collector = Collectors.toMap(
      row -> row.getInteger("id"),
      row -> new DummyObject(row.getInteger("id"),
        row.getShort("Int2"),
        row.getInteger("Int3"),
        row.getInteger("Int4"),
        row.getLong("Int8"),
        row.getFloat("Float"),
        row.getDouble("Double"),
        row.getString("Varchar"))
    );

    DummyObject expected = new DummyObject(1, (short) 32767, 8388607, 2147483647, 9223372036854775807L,
      123.456f, 1.234567d, "HELLO,WORLD");

    MySQLClient.connect(vertx, options, ar -> {
      if (ar.succeeded()) {
        MySQLConnection mySQLConnection = ar.result();
        mySQLConnection.query("SELECT * FROM BasicDataType WHERE id = 1", collector, ar2 -> {
          if (ar2.succeeded()) {
            System.out.println("Query succeed");
            PgResult<Map<Integer, DummyObject>> result = ar2.result();
            Map<Integer, DummyObject> map = result.value();
            DummyObject actual = map.get(1);
            ctx.assertEquals(expected, actual);
            System.out.println(actual.toString());
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
