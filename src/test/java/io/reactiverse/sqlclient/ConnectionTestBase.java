package io.reactiverse.sqlclient;

import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.PgConnection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class ConnectionTestBase {
  protected Vertx vertx;
  protected Connector<PgConnection> connector;

  protected PgConnectOptions options;

  protected void connect(Handler<AsyncResult<PgConnection>> handler) {
    connector.connect(handler);
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnect(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    options.setDatabase("invalidDatabase");
    connect(ctx.asyncAssertFailure(err -> {
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    options.setPassword("invalidPassword");
    connect(ctx.asyncAssertFailure(err -> {
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    options.setUser("invalidUsername");
    connect(ctx.asyncAssertFailure(err -> {
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async async = ctx.async();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        async.complete();
      });
      conn.close();
    }));
    async.await();
  }


  @Test
  public void testCloseWithErrorInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(2, async.count());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
    async.await();
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from world", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(10000, result.size());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
    async.await();
  }
}
