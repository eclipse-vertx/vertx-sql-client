package io.reactiverse.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class DataTypeWideningTest extends DataTypeTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  @Test
  public void testIntegerToLong(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .prepare("SELECT * FROM \"NumericDataType\" WHERE \"Long\"=$1", ctx.asyncAssertSuccess(preparedQuery -> {
          preparedQuery.execute(Tuple.of(Integer.valueOf(5)), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(0, result.size());
            async.complete();
          }));
        }));
    }));
  }

}
