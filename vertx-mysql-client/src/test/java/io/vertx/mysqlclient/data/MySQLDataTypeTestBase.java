package io.vertx.mysqlclient.data;

import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.mysqlclient.MySQLTestBase;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;

public abstract class MySQLDataTypeTestBase extends MySQLTestBase {
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

  protected <T> void testTextDecodeGenericWithTable(TestContext ctx,
                                                    String columnName,
                                                    T expected) {
    testTextDecodeGenericWithTable(ctx, columnName, (row, cn) -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(cn));
    });
  }

  protected <T> void testTextDecodeGenericWithTable(TestContext ctx,
                                                    String columnName,
                                                    BiConsumer<Row, String> expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT `" + columnName + "` FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        expected.accept(row, columnName);
        conn.close();
      }));
    }));
  }

  protected <T> void testBinaryDecodeGenericWithTable(TestContext ctx,
                                                      String columnName,
                                                      T expected) {
    testBinaryDecodeGenericWithTable(ctx, columnName, (row, cn) -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(cn));
    });
  }

  protected <T> void testBinaryDecodeGenericWithTable(TestContext ctx,
                                                      String columnName,
                                                      BiConsumer<Row, String> expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 1", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        expected.accept(row, columnName);
        conn.close();
      }));
    }));
  }

  protected <T> void testBinaryEncodeGeneric(TestContext ctx,
                                             String columnName,
                                             T expected) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE datatype SET `" + columnName + "` = ?" + " WHERE id = 2", Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT `" + columnName + "` FROM datatype WHERE id = 2", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(expected, row.getValue(0));
          ctx.assertEquals(expected, row.getValue(columnName));
          conn.close();
        }));
      }));
    }));
  }

  protected void testBinaryDecode(TestContext ctx, String sql, Tuple params, Consumer<RowSet<Row>> checker) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(sql, params, ctx.asyncAssertSuccess(result -> {
        checker.accept(result);
        conn.close();
      }));
    }));
  }
}
