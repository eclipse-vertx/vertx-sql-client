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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.SqlResult;
import io.vertx.sqlclient.Tuple;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public abstract class PgPoolTestBase extends PgTestBase {

  Vertx vertx;

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected abstract PgPool createPool(PgConnectOptions options, PoolOptions poolOpts);

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, poolOptions.setMaxSize(4));
    for (int i = 0;i < num;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD").execute(ar -> {
          if (ar.succeeded()) {
            SqlResult result = ar.result();
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
  public void testQuery(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, poolOptions.setMaxSize(4));
    for (int i = 0;i < num;i++) {
      pool.query("SELECT id, randomnumber from WORLD").execute(ar -> {
        if (ar.succeeded()) {
          SqlResult result = ar.result();
          ctx.assertEquals(10000, result.size());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testQueryWithParams(TestContext ctx) {
    testQueryWithParams(ctx, options);
  }

  @Test
  public void testCachedQueryWithParams(TestContext ctx) {
    testQueryWithParams(ctx, new PgConnectOptions(options).setCachePreparedStatements(true));
  }

  private void testQueryWithParams(TestContext ctx, PgConnectOptions options) {
    int num = 2;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, poolOptions.setMaxSize(1));
    for (int i = 0;i < num;i++) {
      pool.preparedQuery("SELECT id, randomnumber from WORLD where id=$1").execute(Tuple.of(i + 1), ar -> {
        if (ar.succeeded()) {
          SqlResult result = ar.result();
          ctx.assertEquals(1, result.size());
        } else {
          ar.cause().printStackTrace();
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdate(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, poolOptions.setMaxSize(4));
    for (int i = 0;i < num;i++) {
      pool.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute( ar -> {
        if (ar.succeeded()) {
          SqlResult result = ar.result();
          ctx.assertEquals(1, result.rowCount());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testUpdateWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    PgPool pool = createPool(options, poolOptions.setMaxSize(4));
    for (int i = 0;i < num;i++) {
      pool.preparedQuery("UPDATE Fortune SET message = 'Whatever' WHERE id = $1").execute(Tuple.of(9), ar -> {
        if (ar.succeeded()) {
          SqlResult result = ar.result();
          ctx.assertEquals(1, result.rowCount());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
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
      PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), poolOptions.setMaxSize(1));
      pool.getConnection(ctx.asyncAssertSuccess(conn1 -> {
        proxyConn.get().close();
        conn1.closeHandler(v2 -> {
          conn1.query("never-read").execute(ctx.asyncAssertFailure(err -> {
            pool.getConnection(ctx.asyncAssertSuccess(conn2 -> {
              conn2.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v3 -> {
                async.complete();
              }));
            }));
          }));
        });
      }));
    }));
  }

  @Test
  public void testCancelRequest(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(options, poolOptions.setMaxSize(4));
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT pg_sleep(10)").execute(ctx.asyncAssertFailure(error -> {
        ctx.assertEquals("canceling statement due to user request", error.getMessage());
        conn.close();
        async.complete();
      }));
      ((PgConnection)conn).cancelRequest(ctx.asyncAssertSuccess());
    }));
  }

  @Repeat(500)
  @Test
  public void checkBorderConditionBetweenIdleAndGetConnection(TestContext ctx) {
    final int concurrentRequestAmount = 100;
    final int idle = 1000;
    final int poolSize = 5;
    Async async = ctx.async(concurrentRequestAmount);

    options.setIdleTimeout(idle).setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    poolOptions.setMaxSize(poolSize).setIdleTimeout(idle).setIdleTimeUnit(TimeUnit.MILLISECONDS);

    PgPool pool = createPool(new PgConnectOptions(options), poolOptions);

    IntStream.range(0, concurrentRequestAmount).forEach(n -> CompletableFuture.runAsync(() ->
      pool.query("SELECT CURRENT_TIMESTAMP;").execute(ctx.asyncAssertSuccess(rowSet -> {
        pool.query("select count(*) as cnt from pg_stat_activity where application_name like '%vertx%' and state = 'active'").execute(ctx.asyncAssertSuccess(rows -> {
          Integer count = rows.iterator().next().getInteger("cnt");
          ctx.assertInRange(count , 1, poolSize, "Oops!...Connections exceed poolSize. Are you leaked connections?.");
          async.countDown();
        }));
      }))));
  }
}
