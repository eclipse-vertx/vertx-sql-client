package io.vertx.pgclient;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CloseConnectionTest extends PgTestBase {

  protected Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCloseConnection(TestContext ctx) {
    testCloseConnection(ctx, () -> {
      PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.close().onComplete(ctx.asyncAssertSuccess());
      }));
    });
  }

  @Test
  public void testClosePooledConnection(TestContext ctx) {
    testCloseConnection(ctx, () -> {
      Pool pool = PgBuilder.pool().connectingTo(options).with(new PoolOptions().setMaxSize(1)).using(vertx).build();
      pool.getConnection().onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
          pool.close().onComplete(ctx.asyncAssertSuccess());
        }));
      }));
    });
  }

  private void testCloseConnection(TestContext ctx, Runnable test) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      Buffer data = Buffer.buffer();
      conn.clientHandler(buff -> {
        data.appendBuffer(buff);
        conn.serverSocket().write(buff);
      });
      conn.serverCloseHandler(v -> {
        ctx.assertTrue(data.length() > 5);
        String hex = ByteBufUtil.hexDump(data.slice(data.length() - 5, data.length()).getBytes());
        // Connection close packet
        ctx.assertEquals("5800000004", hex);
        async.complete();
      });
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");
      test.run();
    }));
  }

  @Test
  public void testTransactionInProgressShouldFail(TestContext ctx) {
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.connect();
      vertx.setTimer(1_000, l -> conn.close());
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      options.setPort(8080).setHost("localhost");

      Pool pool = Pool.pool(vertx, options, new PoolOptions().setMaxSize(1));
      pool.withTransaction(conn -> conn.query("select pg_sleep(60)").execute())
        .onComplete(ctx.asyncAssertFailure())
      ;
    }));
  }
}
