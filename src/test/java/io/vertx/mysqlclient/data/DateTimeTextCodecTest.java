package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLClient;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DateTimeTextCodecTest extends DateTimeCodecTest {
  @Test
  public void testDecodeYear(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_year", (short) 2019);
  }

  @Override
  protected <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected) {
    Async async = ctx.async();
    MySQLClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT CAST(\'" + data + "\' AS " + dataType + ") " + columnName, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        async.complete();
      }));
    }));
  }
}
