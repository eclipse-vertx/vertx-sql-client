package io.vertx.pgclient.context;

import io.vertx.core.Context;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgTestBase;
import io.vertx.sqlclient.Pool;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ContextTest extends PgTestBase {

  protected Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected abstract Context createContext();

  @Test
  public void testConnection(TestContext testCtx) {
    Async async = testCtx.async();
    Context connCtx = createContext();
    connCtx.runOnContext(v1 -> {
      PgConnection.connect(vertx, options).onComplete(testCtx.asyncAssertSuccess(conn -> {
        testCtx.assertEquals(connCtx, Vertx.currentContext());
        conn
          .query("SELECT *  FROM (VALUES ('Hello world')) t1 (col1) WHERE 1 = 1")
          .execute()
          .onComplete(testCtx.asyncAssertSuccess(result -> {
          testCtx.assertEquals(connCtx, Vertx.currentContext());
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testPooledConnection(TestContext testCtx) {
    Context appCtx = createContext();
    Async async = testCtx.async();
    Context connCtx = vertx.getOrCreateContext();
    connCtx.runOnContext(v1 -> {
      Pool pool = PgBuilder.pool().connectingTo(options).using(vertx).build();
      appCtx.runOnContext(v -> {
        pool.getConnection().onComplete(testCtx.asyncAssertSuccess(conn -> {
          testCtx.assertEquals(appCtx, Vertx.currentContext());
          conn
            .query("SELECT *  FROM (VALUES ('Hello world')) t1 (col1) WHERE 1 = 1")
            .execute()
            .onComplete(testCtx.asyncAssertSuccess(result -> {
            testCtx.assertEquals(appCtx, Vertx.currentContext());
            async.complete();
          }));
        }));
      });
    });
  }

  @Test
  public void testPoolQuery(TestContext testCtx) {
    Context appCtx = createContext();
    Async async = testCtx.async();
    Context connCtx = vertx.getOrCreateContext();
    connCtx.runOnContext(v1 -> {
      Pool pool = PgBuilder.pool().connectingTo(options).using(vertx).build();
      appCtx.runOnContext(v -> {
        pool
          .query("SELECT *  FROM (VALUES ('Hello world')) t1 (col1) WHERE 1 = 1")
          .execute()
          .onComplete(testCtx.asyncAssertSuccess(result -> {
            testCtx.assertEquals(appCtx, Vertx.currentContext());
            async.complete();
          }));
      });
    });
  }
}
