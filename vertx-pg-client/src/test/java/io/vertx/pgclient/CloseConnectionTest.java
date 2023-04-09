package io.vertx.pgclient;

import io.netty.buffer.ByteBufUtil;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.spi.PgDriver;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.ProxyServer;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.spi.ConnectionFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.function.Function;

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
      PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(1));
      pool.getConnection().onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
          pool.close().onComplete(ctx.asyncAssertSuccess());
        }));
      }));
    });
  }

  @Test
  public void testCloseNetSocket(TestContext ctx) {
    testCloseConnection(ctx, () -> {
      PgPool pool = PgPool.pool(vertx, options, new PoolOptions().setMaxSize(1));
      ConnectionFactory factory = PgDriver.INSTANCE.createConnectionFactory(vertx);
      pool.connectionProvider(new Function<Context, Future<SqlConnection>>() {
        @Override
        public Future<SqlConnection> apply(Context context) {
          return factory.connect(context, options);
        }
      });
      pool.getConnection().onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
          factory.close(Promise.promise());
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
}
