package com.julienviet.pgclient;

import io.vertx.ext.unit.TestContext;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class MultiplexedPoolTest extends PoolTestBase {

  @Override
  protected PgConnectionPool createPool(PgClient client, int size) {
    return client.createMultiplexedPool();
  }

  @Override
  public void testPool(TestContext ctx) {
    super.testPool(ctx);
  }

  @Override
  public void testReconnect(TestContext ctx) {
    super.testReconnect(ctx);
  }

}
