package io.vertx.pgclient;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.PoolOptions;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ContextTest extends PgTestBase {

  private Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnection(TestContext testCtx) {
    Async async = testCtx.async();
    Context connCtx = vertx.getOrCreateContext();
    connCtx.runOnContext(v1 -> {
      PgConnection.connect(vertx, options, testCtx.asyncAssertSuccess(conn -> {
        testCtx.assertEquals(connCtx, Vertx.currentContext());
        conn
          .query("SELECT *  FROM (VALUES ('Hello world')) t1 (col1) WHERE 1 = 1")
          .execute(testCtx.asyncAssertSuccess(result -> {
          testCtx.assertEquals(connCtx, Vertx.currentContext());
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testPooledConnection(TestContext testCtx) {
    Context appCtx = vertx.getOrCreateContext();
    Async async = testCtx.async();
    Context connCtx = vertx.getOrCreateContext();
    connCtx.runOnContext(v1 -> {
      PgPool pool = PgPool.pool(vertx, options, new PoolOptions());
      appCtx.runOnContext(v -> {
        pool.getConnection(testCtx.asyncAssertSuccess(conn -> {
          testCtx.assertEquals(appCtx, Vertx.currentContext());
          conn
            .query("SELECT *  FROM (VALUES ('Hello world')) t1 (col1) WHERE 1 = 1")
            .execute(testCtx.asyncAssertSuccess(result -> {
            testCtx.assertEquals(appCtx, Vertx.currentContext());
            async.complete();
          }));
        }));
      });
    });
  }
}
