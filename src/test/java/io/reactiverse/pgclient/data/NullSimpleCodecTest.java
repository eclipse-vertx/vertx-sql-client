package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgConnection;
import io.reactiverse.sqlclient.Row;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class NullSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testNull(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT null \"NullValue\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "NullValue").forRow(row);
          async.complete();
        }));
    }));
  }
}
