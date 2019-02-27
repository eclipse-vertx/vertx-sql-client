package io.reactiverse.mysqlclient2;

import io.reactiverse.pgclient.MyClient;
import io.reactiverse.pgclient.PgConnectOptions;
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

import java.util.Map;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@RunWith(VertxUnitRunner.class)
public class MysqlQueryTest extends MysqlTestBase {

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
  public void testSimpleQuery(TestContext ctx) {
    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM BasicDataType", ctx.asyncAssertSuccess(rowSet -> {
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
      }));
    }));
  }

  @Test
  public void testSimpleQueryCollector(TestContext ctx) {
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

    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM BasicDataType WHERE id = 1", collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
      }));
    }));
  }

  @Test
  public void testPrepare(TestContext ctx) {
    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM BasicDataType WHERE id = ?", ctx.asyncAssertSuccess(query -> {
        // Ok
      }));
    }));
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM BasicDataType WHERE id = ?", Tuple.of(1), ctx.asyncAssertSuccess(rowSet -> {
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
      }));
    }));
  }

  @Test
  public void testPreparedCollector(TestContext ctx) {
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

    MyClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM BasicDataType WHERE id = ?", Tuple.of(1), collector, ctx.asyncAssertSuccess(result -> {
        Map<Integer, DummyObject> map = result.value();
        DummyObject actual = map.get(1);
        ctx.assertEquals(expected, actual);
      }));
    }));
  }
}
