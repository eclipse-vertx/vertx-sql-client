package io.vertx.db2client;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2QueriesTest extends DB2TestBase {

  @Test
  public void testRowNumber(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM (" +
          "SELECT id, message, row_number() OVER (ORDER BY id) rn FROM immutable" +
          ") r_0_ WHERE r_0_.rn <= ? + ? AND r_0_.rn > ? ORDER BY r_0_.rn").execute(Tuple.of(1, 1, 1))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          ctx.assertEquals(2, rows.iterator().next().getInteger("rn"));
          conn.close();
        }));
    }));
  }
}
