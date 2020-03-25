package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public abstract class PreparedBatchTestBase {
  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected abstract String statement(String... parts);

  protected abstract void initConnector();

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
    cleanTestTable(ctx);
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testInsert(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(79991, "batch one"));
      batch.add(Tuple.of(79992, "batch two"));
      batch.add(Tuple.wrap(Arrays.asList(79993, "batch three")));
      batch.add(Tuple.wrap(Arrays.asList(79994, "batch four")));

      conn.preparedQuery(statement("INSERT INTO mutable (id, val) VALUES (", ", ", ")")).executeBatch(batch, ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.preparedQuery(statement("SELECT * FROM mutable WHERE id=", "")).execute(Tuple.of(79991), ctx.asyncAssertSuccess(ar1 -> {
          ctx.assertEquals(1, ar1.size());
          Row one = ar1.iterator().next();
          ctx.assertEquals(79991, one.getInteger("id"));
          ctx.assertEquals("batch one", one.getString("val"));
          conn.preparedQuery(statement("SELECT * FROM mutable WHERE id=", "")).execute(Tuple.of(79992), ctx.asyncAssertSuccess(ar2 -> {
            ctx.assertEquals(1, ar2.size());
            Row two = ar2.iterator().next();
            ctx.assertEquals(79992, two.getInteger("id"));
            ctx.assertEquals("batch two", two.getString("val"));
            conn.preparedQuery(statement("SELECT * FROM mutable WHERE id=", "")).execute(Tuple.of(79993), ctx.asyncAssertSuccess(ar3 -> {
              ctx.assertEquals(1, ar3.size());
              Row three = ar3.iterator().next();
              ctx.assertEquals(79993, three.getInteger("id"));
              ctx.assertEquals("batch three", three.getString("val"));
              conn.preparedQuery(statement("SELECT * FROM mutable WHERE id=", "")).execute(Tuple.of(79994), ctx.asyncAssertSuccess(ar4 -> {
                ctx.assertEquals(1, ar4.size());
                Row four = ar4.iterator().next();
                ctx.assertEquals(79994, four.getInteger("id"));
                ctx.assertEquals("batch four", four.getString("val"));
              }));
            }));
          }));
        }));
      }));
    }));
  }

  private void cleanTestTable(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}
