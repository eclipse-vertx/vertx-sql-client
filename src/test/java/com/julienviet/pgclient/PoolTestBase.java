package com.julienviet.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@RunWith(VertxUnitRunner.class)
public abstract class PoolTestBase extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected abstract PgConnectionPool createPool(PgClient client, int size);

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgClient client = PgClient.create(vertx, options);
    PgConnectionPool pool = createPool(client, 4);
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
      PgConnectionPool pool = createPool(client, 1);
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
}
