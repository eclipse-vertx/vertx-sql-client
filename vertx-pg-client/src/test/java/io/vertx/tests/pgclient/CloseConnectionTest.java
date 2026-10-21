package io.vertx.tests.pgclient;

import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelOption;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.net.NetSocketInternal;
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

import java.nio.charset.StandardCharsets;
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
  public void testReadIdleTimeoutClosesConnection(TestContext ctx) {
    Async async = ctx.async();
    vertx.createNetServer().connectHandler(socket -> {
      // Enable half-close so the server keeps the connection open after receiving FIN,
      // otherwise Vert.x auto-closes and sends FIN back, masking the bug
      ((NetSocketInternal) socket).channelHandlerContext().channel().config()
        .setOption(ChannelOption.ALLOW_HALF_CLOSURE, true);
      socket.handler(data -> {
        // Received startup message, send PG handshake response then stay silent
        Buffer response = Buffer.buffer()
          .appendByte((byte) 'R').appendInt(8).appendInt(0)
          .appendByte((byte) 'S').appendInt(25)
          .appendBytes("client_encoding\0UTF8\0".getBytes(StandardCharsets.UTF_8))
          .appendByte((byte) 'K').appendInt(12).appendInt(1).appendInt(0)
          .appendByte((byte) 'Z').appendInt(5).appendByte((byte) 'I');
        socket.write(response);
        socket.handler(null);
      });
    }).listen(0, "localhost").onComplete(ctx.asyncAssertSuccess(server -> {
      PgConnectOptions connectOptions = new PgConnectOptions()
        .setHost("localhost")
        .setPort(server.actualPort())
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
          pool.close();
          server.close();
          async.complete();
        })
      ));
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
        .onComplete(ctx.asyncAssertFailure(t -> {
          t.printStackTrace();
        }));
    }));
  }
}
