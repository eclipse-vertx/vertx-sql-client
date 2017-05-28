package com.julienviet.pgclient;

import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPooledConnectionTest extends PgConnectionTestBase {

  public PgPooledConnectionTest() {
    super((client, handler) -> {
      PgConnectionPool pool = client.createPool(1);
      pool.getConnection(handler);
    });
  }

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgConnectionPool pool = client.createPool(4);
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD", ar -> {
          if (ar.succeeded()) {
            ResultSet result = ar.result();
            ctx.assertEquals(10000, result.getNumRows());
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
      PgClient client = PgClient.create(vertx, new PgClientOptions(options).setPort(8080).setHost("localhost"));
      PgConnectionPool pool = client.createPool(1);
      pool.getConnection(ctx.asyncAssertSuccess(conn1 -> {
        proxyConn.get().close();
        conn1.closeHandler(v2 -> {
          conn1.query("never-executer", ctx.asyncAssertFailure(err -> {
            pool.getConnection(ctx.asyncAssertSuccess(conn2 -> {
              conn2.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v3 -> {
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
      PgClient client = PgClient.create(vertx, new PgClientOptions(options).setPort(8080).setHost("localhost"));
      PgConnectionPool pool = client.createPool(1);
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      }));
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
  }

  @Override
  public void testPreparedQueryBindError(TestContext ctx) {
  }

  @Override
  public void testPreparedQueryParseError(TestContext ctx) {
  }

  @Override
  public void testPreparedPartialQuery(TestContext ctx) {
  }

  @Override
  public void testPreparedQuery(TestContext ctx) {
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
