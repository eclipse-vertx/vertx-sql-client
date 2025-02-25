/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package tests.oracleclient;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.OracleConnectOptions;
import tests.oracleclient.junit.OracleRule;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class OraclePoolTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  private OracleConnectOptions options;
  private Set<Pool> pools;

  @Before
  public void setUp() throws Exception {
    options = oracle.options();
    pools = Collections.synchronizedSet(new HashSet<>());
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    if (!pools.isEmpty()) {
      Async async = ctx.async(pools.size());
      for (Pool pool : pools) {
        pool
          .close()
          .onComplete(ar -> {
          async.countDown();
        });
      }
      async.await();
    }
  }

  private Pool createPool(OracleConnectOptions connectOptions, int size) {
    return createPool(connectOptions, new PoolOptions().setMaxSize(size));
  }

  private Pool createPool(OracleConnectOptions connectOptions, PoolOptions poolOptions) {
    return createPool(connectOptions, poolOptions, null);
  }

  private Pool createPool(OracleConnectOptions connectOptions, PoolOptions poolOptions, Handler<SqlConnection> connectHandler) {
    OracleConnectOptions co = new OracleConnectOptions(connectOptions);
    PoolOptions po = new PoolOptions(poolOptions);
    Pool pool = OracleBuilder.pool(builder -> builder
      .with(po)
      .withConnectHandler(connectHandler)
      .connectingTo(co)
      .using(vertx));
    pools.add(pool);
    return pool;
  }

  @Test
  public void testPool(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    Pool pool = createPool(options, 40);
    for (int i = 0; i < num; i++) {
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertSuccess(conn -> {
        conn
          .query("SELECT id, randomnumber FROM WORLD")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(100, rows.size());
          conn.close();
          async.countDown();
        }));
      }));
    }
  }

  @Test
  public void testQuery(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    Pool pool = createPool(options, 40);
    for (int i = 0; i < num; i++) {
      pool
        .query("SELECT id, randomnumber FROM WORLD")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(100, rows.size());
        async.countDown();
      }));
    }
  }

  @Test
  public void testQueryWithParams(TestContext ctx) {
    int num = 2;
    Async async = ctx.async(num);
    Pool pool = createPool(options, 1);
    for (int i = 0; i < num; i++) {
      PreparedQuery<RowSet<Row>> preparedQuery = pool.preparedQuery("SELECT id, randomnumber FROM WORLD WHERE id=?");
      preparedQuery
        .execute(Tuple.of(i + 1))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        async.countDown();
      }));
    }
  }

  @Test
  public void testUpdate(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    Pool pool = createPool(options, 4);
    for (int i = 0; i < num; i++) {
      pool
        .query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.rowCount());
        async.countDown();
      }));
    }
  }

  @Test
  public void testUpdateWithParams(TestContext ctx) {
    int num = 1000;
    Async async = ctx.async(num);
    Pool pool = createPool(options, 4);
    for (int i = 0; i < num; i++) {
      pool
        .preparedQuery("UPDATE Fortune SET message = 'Whatever' WHERE id = ?")
        .execute(Tuple.of(9))
        .onComplete(ar -> {
        if (ar.succeeded()) {
          RowSet<Row> result = ar.result();
          ctx.assertEquals(1, result.rowCount());
        } else {
          ctx.assertEquals("closed", ar.cause().getMessage());
        }
        async.countDown();
      });
    }
  }

  @Test
  public void testWithConnection(TestContext ctx) {
    Async async = ctx.async(10);
    Pool pool = createPool(options, 1);
    Function<SqlConnection, Future<RowSet<Row>>> success = conn -> conn.query("SELECT 1 FROM DUAL").execute();
    Function<SqlConnection, Future<RowSet<Row>>> failure = conn -> conn.query("SELECT 1 FROM does_not_exist").execute();
    for (int i = 0; i < 10; i++) {
      if (i % 2 == 0) {
        pool
          .withConnection(success)
          .onComplete(ctx.asyncAssertSuccess(v -> async.countDown()));
      } else {
        pool
          .withConnection(failure)
          .onComplete(ctx.asyncAssertFailure(v -> async.countDown()));
      }
    }
  }

  @Test
  public void testAuthFailure(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(new OracleConnectOptions(options).setPassword("wrong"), 1);
    pool
      .query("SELECT id, randomnumber FROM WORLD")
      .execute()
      .onComplete(ctx.asyncAssertFailure(v2 -> {
      async.complete();
    }));
  }

  @Test
  public void testRunWithExisting(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      try {
        Pool.pool(options, new PoolOptions());
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(new OracleConnectOptions(options), new PoolOptions());
    pool
      .query("SELECT id, randomnumber FROM WORLD")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(4000);
  }

  @Test
  public void testMaxWaitQueueSize(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(v -> {
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertFailure(err -> {
        async.complete();
      }));
    }));
    async.await(4000000);
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    Pool pool = createPool(new OracleConnectOptions(this.options).setCachePreparedStatements(true), 2);
    int numRequests = 2;
    Async async = ctx.async(numRequests);
    for (int i = 0; i < numRequests; i++) {
      pool
        .preparedQuery("SELECT * FROM Fortune WHERE id=?")
        .execute(Tuple.of(1))
        .onComplete(ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(1, results.size());
        Tuple row = results.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        async.countDown();
      }));
    }
  }

  @Test
  public void testConnectionHook(TestContext ctx) {
    AtomicInteger hookCount = new AtomicInteger();
    Handler<SqlConnection> hook = f -> {
      ctx.assertEquals(1, hookCount.incrementAndGet());
      vertx.setTimer(100, id -> {
        f.close();
      });
    };
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1), hook);
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      ctx.assertEquals(1, hookCount.get());
      conn
        .query("SELECT id, randomnumber FROM WORLD")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v2 -> {
        conn.close().onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test
  public void testDirectQueryFromDuplicatedContext(TestContext ctx) {
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1));
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      ContextInternal current = (ContextInternal) Vertx.currentContext();
      pool
        .query("SELECT 1 FROM DUAL")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertTrue(Vertx.currentContext() == current);
        ContextInternal duplicated = current.duplicate();
        duplicated.runOnContext(v2 -> {
          pool
            .query("SELECT 1 FROM DUAL")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertTrue(Vertx.currentContext() == duplicated);
            async.complete();
          }));
        });
      }));
    });
  }

  @Test
  public void testQueryFromDuplicatedContext(TestContext ctx) {
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1));
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      ContextInternal current = (ContextInternal) Vertx.currentContext();
      pool
        .query("SELECT 1 FROM DUAL")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertTrue(Vertx.currentContext() == current);
        ContextInternal duplicated = current.duplicate();
        duplicated.runOnContext(v2 -> {
          pool
            .getConnection()
            .onComplete(ctx.asyncAssertSuccess(conn -> {
            ctx.assertTrue(Vertx.currentContext() == duplicated);
            conn
              .query("SELECT 1 FROM DUAL")
              .execute()
              .onComplete(ctx.asyncAssertSuccess(res2 -> {
              ctx.assertTrue(Vertx.currentContext() == duplicated);
              conn.close().onComplete(ctx.asyncAssertSuccess(v -> {
                ctx.assertTrue(Vertx.currentContext() == duplicated);
                async.complete();
              }));
            }));
          }));
        });
      }));
    });
  }
}
