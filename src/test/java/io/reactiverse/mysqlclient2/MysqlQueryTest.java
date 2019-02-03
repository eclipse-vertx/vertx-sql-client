package io.reactiverse.mysqlclient2;

import io.reactiverse.pgclient.MyClient;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.Row;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

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
  public void test1(TestContext ctx) {
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
}
