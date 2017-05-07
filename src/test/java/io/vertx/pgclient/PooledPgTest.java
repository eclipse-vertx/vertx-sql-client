package io.vertx.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PooledPgTest extends PgTestBase {

  public PooledPgTest() {
    super((client, handler) -> {
      PostgresConnectionPool pool = client.createPool(1);
      pool.getConnection(handler);
    });
  }

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PostgresClient client = PostgresClient.create(vertx, options);
    PostgresConnectionPool pool = client.createPool(4);
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.execute("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            Result result = ar.result();
            ctx.assertEquals(0, result.getUpdatedRows());
            ctx.assertEquals(10000, result.size());
          } else {
            ctx.assertEquals("closed", ar.cause().getMessage());
          }
          conn.close();
          async.countDown();
        });
      }));
    }
  }

  @Override
  public void testClose(TestContext ctx) {
    // Does not pass yet
    ctx.fail("Todo");
  }

  @Override
  public void testCloseWithErrorInProgress(TestContext ctx) {
    // Does not pass yet
    ctx.fail("Todo");
  }

  @Override
  public void testCloseWithQueryInProgress(TestContext ctx) {
    // Does not pass yet
    ctx.fail("Todo");
  }

  @Override
  public void testQueueQueries(TestContext ctx) {
    // Does not pass yet
    ctx.fail("Todo");
  }

  @Test
  public void testThatPoolReconnect(TestContext ctx) {
    // Implement me
    // perhaps it should be possible to have it on the PostgresConnection as well
    ctx.fail("Todo");
  }
}
