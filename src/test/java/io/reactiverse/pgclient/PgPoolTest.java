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

package io.reactiverse.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  @Override
  protected PgPool createPool(PgConnectOptions options, int size) {
    return PgClient.pool(vertx, new PgPoolOptions(options).setMaxSize(size));
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
        conn.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
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
    PgPool pool = PgClient.pool(vertx, new PgPoolOptions(options)
      .setPort(8080)
      .setHost("localhost")
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
        PgClient.pool(new PgPoolOptions());
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = PgClient.pool(new PgPoolOptions(options));
    try {
      pool.query("SELECT id, randomnumber from WORLD", ctx.asyncAssertSuccess(v -> {
        async.complete();
      }));
      async.await(4000);
    } finally {
      pool.close();
    }
  }

  @Test
  public void testMaxWaitQueueSize(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = PgClient.pool(new PgPoolOptions(options).setMaxSize(1).setMaxWaitQueueSize(0));
    try {
      pool.getConnection(ctx.asyncAssertSuccess(v -> {
        pool.getConnection(ctx.asyncAssertFailure(err -> {
          async.complete();
        }));
      }));
      async.await(4000000);
    } finally {
      pool.close();
    }
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    PgPoolOptions options = new PgPoolOptions(new PgConnectOptions(PgTestBase.options).setCachePreparedStatements(true)).setMaxSize(2);
    PgPool pool = PgClient.pool(vertx, options);
    int numRequests = 2;
    Async async = ctx.async(numRequests);
    for (int i = 0;i < numRequests;i++) {
      pool.preparedQuery("SELECT * FROM Fortune WHERE id=$1", Tuple.of(1), ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(1, results.size());
        Tuple row = results.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        async.countDown();
      }));
    }
  }
}
