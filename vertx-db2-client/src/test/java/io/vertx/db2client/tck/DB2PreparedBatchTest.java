package io.vertx.db2client.tck;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.PreparedBatchTestBase;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class DB2PreparedBatchTest extends PreparedBatchTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
  
  @Override
  protected void cleanTestTable(TestContext ctx) {
      // use DELETE FROM because DB2 does not support TRUNCATE TABLE
      connect(ctx.asyncAssertSuccess(conn -> {
          conn.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {
              conn.close();
          }));
      }));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }
  
  @Test
  public void testBatchQuery(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(1));
      batch.add(Tuple.of(3));
      batch.add(Tuple.of(5));

      conn.preparedBatch(statement("SELECT * FROM immutable WHERE id=", ""), batch, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));

        result = result.next();
        ctx.assertEquals(1, result.size());
        row = result.iterator().next();
        ctx.assertEquals(3, row.getInteger(0));
        ctx.assertEquals("After enough decimal places, nobody gives a damn.", row.getString(1));

        result = result.next();
        ctx.assertEquals(1, result.size());
        row = result.iterator().next();
        ctx.assertEquals(5, row.getInteger(0));
        ctx.assertEquals("A computer program does what you tell it to do, not what you want it to do.", row.getString(1));
      }));
    }));
  }
}
