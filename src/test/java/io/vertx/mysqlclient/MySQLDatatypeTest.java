package io.vertx.mysqlclient;

import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLDatatypeTest extends MySQLTestBase {
  Vertx vertx;
  PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTextDecodeBinary(TestContext ctx) {
    testTextDecodeGeneric(ctx, "Binary", "HELLO");
  }

  @Test
  public void testBinaryDecodeBinary(TestContext ctx) {
    testBinaryDecodeGeneric(ctx, "Binary", "HELLO");
  }

  @Test
  public void testBinaryEncodeBinary(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "Binary", "HELLO");
  }

  @Test
  public void testBinaryEncodeVarBinary(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "VarBinary", "HELLO, WORLD");
  }

  @Test
  public void testTextDecodeVarBinary(TestContext ctx) {
    testTextDecodeGeneric(ctx, "VarBinary", "HELLO, WORLD");
  }

  @Test
  public void testBinaryDecodeVarBinary(TestContext ctx) {
    testBinaryDecodeGeneric(ctx, "VarBinary", "HELLO, WORLD");
  }

  private <T> void testTextDecodeGeneric(TestContext ctx,
                                         String columnName,
                                         T expected) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT `" + columnName + "` FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        async.complete();
      }));
    }));
  }

  private <T> void testBinaryDecodeGeneric(TestContext ctx,
                                           String columnName,
                                           T expected) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        async.complete();
      }));
    }));
  }

  private <T> void testBinaryEncodeGeneric(TestContext ctx,
                                           String columnName,
                                           T expected) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE datatype SET `" + columnName + "` = ?" + " WHERE id = 2", Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 2", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue(columnName));
          async.complete();
        }));
      }));
    }));
  }
}
