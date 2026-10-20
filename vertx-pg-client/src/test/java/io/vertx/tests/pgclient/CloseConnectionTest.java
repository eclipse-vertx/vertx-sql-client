package io.vertx.tests.pgclient;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.SslMode;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
  public void testReadIdleTimeoutClosesConnection(TestContext ctx) throws Exception {
    Async async = ctx.async();
    // Same fake server: completes auth then stays silent. When readIdleTimeout fires,
    // handleIdle() calls chctx.close(), which hits PgEncoder.close(ctx, promise).
    // Without the fix that propagates the close (ctx.close(promise) or ctx.close() in
    // encoder.close()), the channel half-closes and closeHandler never fires.
    CountDownLatch latch = new CountDownLatch(1);
    java.net.ServerSocket fakeServer = new java.net.ServerSocket(0);
    int serverPort = fakeServer.getLocalPort();
    Thread serverThread = new Thread(() -> {
      try (java.net.Socket socket = fakeServer.accept()) {
        java.io.DataInputStream in = new java.io.DataInputStream(socket.getInputStream());
        java.io.OutputStream out = socket.getOutputStream();
        int startupLen = in.readInt();
        in.readFully(new byte[startupLen - 4]);
        out.write(new byte[]{'R', 0, 0, 0, 8, 0, 0, 0, 0});
        out.write(new byte[]{'S', 0, 0, 0, 25});
        out.write("client_encoding\0UTF8\0".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        out.write(new byte[]{'K', 0, 0, 0, 12, 0, 0, 0, 1, 0, 0, 0, 0});
        out.write(new byte[]{'Z', 0, 0, 0, 5, 'I'});
        out.flush();
        latch.await();
      } catch (Exception ignored) {
      } finally {
        try {
          fakeServer.close();
        } catch (Exception ignored) {
        }
      }
    });
    serverThread.setDaemon(true);
    serverThread.start();

    // readIdleTimeout must be set via NetClientOptions on the builder —
    // PgConnection.connect() does not accept transport options directly.
    PgConnectOptions connectOptions = new PgConnectOptions()
      .setHost("localhost")
      .setPort(serverPort)
      .setUser("test")
      .setPassword("test")
      .setDatabase("test")
      .setSslMode(SslMode.DISABLE);

    Pool pool = PgBuilder.pool()
      .connectingTo(connectOptions)
      .with(new PoolOptions().setMaxSize(1))
      .with(new io.vertx.core.net.NetClientOptions()
        .setReadIdleTimeout(200)
        .setIdleTimeoutUnit(TimeUnit.MILLISECONDS))
      .using(vertx)
      .build();

    pool.getConnection().onComplete(ctx.asyncAssertSuccess(conn ->
      conn.closeHandler(v -> {
        latch.countDown();
        pool.close();
        async.complete();
      })
    ));
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
        .onComplete(ctx.asyncAssertFailure(t -> {
          t.printStackTrace();
        }));
    }));
  }
}
