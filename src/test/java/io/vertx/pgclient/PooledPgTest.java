package io.vertx.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

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

  @Test
  public void testReconnect(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options).setPort(8080).setHost("localhost"));
      PostgresConnectionPool pool = client.createPool(1);
      pool.getConnection(ctx.asyncAssertSuccess(conn1 -> {
        proxyConn.get().close();
        conn1.closeHandler(v2 -> {
          conn1.execute("never-executer", ctx.asyncAssertFailure(err -> {
            pool.getConnection(ctx.asyncAssertSuccess(conn2 -> {
              conn2.execute("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v3 -> {
                async.complete();
              }));
            }));
          }));
        });
      }));
    }));
  }

  @Test
  public void testReconnectQueued(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PostgresClient client = PostgresClient.create(vertx, new PostgresClientOptions(options).setPort(8080).setHost("localhost"));
      PostgresConnectionPool pool = client.createPool(1);
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      }));
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.execute("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
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
