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

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.PoolOptions;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  @Override
  protected PgPool createPool(PgConnectOptions options, PoolOptions poolOpts) {
    return PgPool.pool(vertx, options, poolOpts);
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
      PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), poolOptions.setMaxSize(1));
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
    PgPool pool = createPool(new PgConnectOptions(options).setPassword("wrong"), poolOptions.setMaxSize(1));
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
    PgPool pool = PgPool.pool(vertx, new PgConnectOptions(options).setPort(8080).setHost("localhost"),
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
    PgPool pool = PgPool.pool(options, new PoolOptions());
    try {
      pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
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
    PgPool pool = PgPool.pool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
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

  @Test
  public void testUseAvailableResources(TestContext ctx) {
    int poolSize = 10;
    Async async = ctx.async(poolSize + 1);
    PgPool pool = PgPool.pool(options, new PoolOptions().setMaxSize(poolSize));
    AtomicReference<PgConnection> ctrlConnRef = new AtomicReference<>();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(ctrlConn -> {
      ctrlConnRef.set(ctrlConn);
      for (int i = 0; i < poolSize; i++) {
        vertx.setTimer(10 * (i + 1), l -> {
          pool.query("select pg_sleep(5)").execute(ctx.asyncAssertSuccess(res -> async.countDown()));
        });
      }
      vertx.setTimer(10 * poolSize + 50, event -> {
        ctrlConn.query("select count(*) as cnt from pg_stat_activity where application_name like '%vertx%'").execute(ctx.asyncAssertSuccess(rows -> {
          Integer count = rows.iterator().next().getInteger("cnt");
          ctx.assertEquals(poolSize + 1, count);
          async.countDown();
        }));
      });
    }));
    try {
      async.await();
    } finally {
      PgConnection ctrlConn = ctrlConnRef.get();
      if (ctrlConn != null) {
        ctrlConn.close();
      }
      pool.close();
    }
  }
}

