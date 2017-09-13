package com.julienviet.pgclient;

import io.vertx.ext.unit.TestContext;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPooledConnectionTest extends PgConnectionTestBase {

  public PgPooledConnectionTest() {
    super((client, handler) -> {
      PgConnectionPool pool = client.createPool(new PgPoolOptions().setMaxSize(1));
      pool.getConnection(handler);
    });
  }

  @Override
  public void testBatchUpdate(TestContext ctx) {
  }

  @Override
  public void testClose(TestContext ctx) {
  }

  @Override
  public void testCloseWithErrorInProgress(TestContext ctx) {
  }

  @Override
  public void testCloseWithQueryInProgress(TestContext ctx) {
  }

  @Override
  public void testQueueQueries(TestContext ctx) {
  }

  @Test
  public void testThatPoolReconnect(TestContext ctx) {
  }

}
