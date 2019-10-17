package io.vertx.mssqlclient.data;

import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLTestBase;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import org.junit.After;
import org.junit.Before;

public abstract class MSSQLDataTypeTestBase extends MSSQLTestBase {
  Vertx vertx;
  MSSQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected <T> void testQueryDecodeGenericWithoutTable(TestContext ctx,
                                                        String columnName,
                                                        String type,
                                                        String value,
                                                        T expected) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT CAST(" + value + " AS " + type + ") AS " + columnName, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        conn.close();
      }));
    }));
  }

  protected <T> void testPreparedQueryDecodeGenericWithoutTable(TestContext ctx,
                                                                String columnName,
                                                                String type,
                                                                String value,
                                                                T expected) {
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT CAST(" + value + " AS " + type + ") AS " + columnName, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        conn.close();
      }));
    }));
  }
}
