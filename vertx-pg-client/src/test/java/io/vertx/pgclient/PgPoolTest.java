/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.pgclient;

import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  private Set<PgPool> pools = new HashSet<>();

  @Override
  public void tearDown(TestContext ctx) {
    int size = pools.size();
    if (size > 0) {
      Async async = ctx.async(size);
      Set<PgPool> pools = this.pools;
      this.pools = new HashSet<>();
      pools.forEach(pool -> {
        pool.close(ar -> {
          async.countDown();
        });
      });
      async.awaitSuccess(20_000);
    }
    super.tearDown(ctx);
  }

  @Override
  protected PgPool createPool(PgConnectOptions connectOptions, PoolOptions poolOptions) {
    PgPool pool = PgPool.pool(vertx, connectOptions, poolOptions);
    pools.add(pool);
    return pool;
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
      PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), 1);
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      }));
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testAuthFailure(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(options).setPassword("wrong"), 1);
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertFailure(v2 -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectionFailure(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"),
      new PoolOptions()
        .setMaxSize(1)
        .setMaxWaitQueueSize(0)
    );
    pool.getConnection(ctx.asyncAssertFailure(err -> {
      proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
        pool.getConnection(ctx.asyncAssertSuccess(conn -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testRunWithExisting(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      try {
        PgPool.pool(new PoolOptions());
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(options), new PoolOptions());
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testMaxWaitQueueSize(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool.getConnection(ctx.asyncAssertSuccess(v -> {
      pool.getConnection(ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
    async.await(4000000);
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    PgPool pool = createPool(new PgConnectOptions(this.options).setCachePreparedStatements(true), 2);
    int numRequests = 2;
    Async async = ctx.async(numRequests);
    for (int i = 0;i < numRequests;i++) {
      pool.preparedQuery("SELECT * FROM Fortune WHERE id=$1").execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(1, results.size());
        Tuple row = results.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        async.countDown();
      }));
    }
  }
}
