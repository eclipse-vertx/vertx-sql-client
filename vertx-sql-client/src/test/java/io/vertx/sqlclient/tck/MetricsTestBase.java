/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.metrics.MetricsOptions;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public abstract class MetricsTestBase {

  Vertx vertx;
  ClientMetrics clientMetrics;
  PoolMetrics poolMetrics;
  Pool pool;
  String clientType;
  String clientNamespace;
  String poolType;
  String poolName;

  @Before
  public void setup() {
    vertx = Vertx
      .builder()
      .with(new VertxOptions().setMetricsOptions(
        new MetricsOptions().setEnabled(true)))
      .withMetrics(tracingOptions -> new VertxMetrics() {
        @Override
        public ClientMetrics<?, ?, ?> createClientMetrics(SocketAddress remoteAddress, String type, String namespace) {
          clientType = type;
          clientNamespace = namespace;
          return clientMetrics;
        }
        @Override
        public PoolMetrics<?, ?> createPoolMetrics(String type, String name, int maxSize) {
          poolType = type;
          poolName = name;
          return poolMetrics;
        }
      })
      .build();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected Pool getPool() {
    if (pool == null) {
      pool = createPool(vertx);
    }
    return pool;
  }

  protected abstract SqlConnectOptions connectOptions();

  protected abstract ClientBuilder<Pool> poolBuilder();

  protected Pool createPool(Vertx vertx) {
    return createPool(vertx, new PoolOptions());
  }

  protected Pool createPool(Vertx vertx, PoolOptions options) {
    return poolBuilder().with(options).using(vertx).connectingTo(connectOptions()).build();
  }

  protected abstract String statement(String... parts);

  @Test
  public void testClosePool(TestContext ctx) throws Exception {
    AtomicInteger closeCount = new AtomicInteger();
    clientMetrics = new ClientMetrics() {
      @Override
      public void close() {
        closeCount.incrementAndGet();
      }
    };
    Pool pool = createPool(vertx);
    pool.query("SELECT * FROM immutable WHERE id=1").execute().toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    ctx.assertEquals(0, closeCount.get());
    pool.close();
    long now = System.currentTimeMillis();
    while (closeCount.get() != 1) {
      ctx.assertTrue(System.currentTimeMillis() - now < 20_000);
      Thread.sleep(100);
    }
  }

  @Test
  public void testQueuing(TestContext ctx) throws Exception {
    AtomicInteger queueSize = new AtomicInteger();
    List<Object> enqueueMetrics = Collections.synchronizedList(new ArrayList<>());
    List<Object> dequeueMetrics = Collections.synchronizedList(new ArrayList<>());
    poolMetrics = new PoolMetrics() {
      @Override
      public Object enqueue() {
        Object metric = new Object();
        enqueueMetrics.add(metric);
        queueSize.incrementAndGet();
        return metric;
      }
      @Override
      public void dequeue(Object taskMetric) {
        dequeueMetrics.add(taskMetric);
        queueSize.decrementAndGet();
      }
    };
    Pool pool = createPool(vertx, new PoolOptions().setMaxSize(1).setName("the-pool"));
    SqlConnection conn = pool.getConnection().toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    int num = 16;
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0;i < num;i++) {
      futures.add(pool.query("SELECT * FROM immutable WHERE id=1").execute());
    }
    long now = System.currentTimeMillis();
    while (queueSize.get() != num) {
      ctx.assertTrue(System.currentTimeMillis() - now < 20_000);
      Thread.sleep(100);
    }
    conn.close();
    Future.join(futures).toCompletionStage().toCompletableFuture().get(20, TimeUnit.SECONDS);
    ctx.assertEquals(0, queueSize.get());
    ctx.assertEquals(enqueueMetrics, dequeueMetrics);
    ctx.assertEquals("sql", poolType);
    ctx.assertEquals("the-pool", poolName);
  }

  @Test
  public void testSimpleQuery(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn.query("SELECT * FROM immutable WHERE id=1").execute();
    testMetrics(ctx, false, fn);
  }

  @Test
  public void testPreparedQuery(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn.preparedQuery("SELECT * FROM immutable WHERE id=1").execute();
    testMetrics(ctx, false, fn);
  }

  @Test
  public void testPreparedBatchQuery(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn.preparedQuery("SELECT * FROM immutable WHERE id=1").executeBatch(Collections.singletonList(Tuple.tuple()));
    testMetrics(ctx, false, fn);
  }

  @Test
  public void testPrepareAndQuery(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn
      .prepare("SELECT * FROM immutable WHERE id=1")
      .compose(ps -> ps.query().execute());
    testMetrics(ctx, false, fn);
  }

  @Test
  public void testPrepareAndBatchQuery(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn
      .prepare("SELECT * FROM immutable WHERE id=1")
      .compose(ps -> ps.query().executeBatch(Collections.singletonList(Tuple.tuple())));
    testMetrics(ctx, false, fn);
  }

  @Test
  public void testFailure(TestContext ctx) {
    Function<SqlConnection, Future<?>> fn = conn -> conn.query("SELECT * FROM undefined_table WHERE id = 1").execute();
    testMetrics(ctx, true, fn);
  }

  private void testMetrics(TestContext ctx, boolean fail, Function<SqlConnection, Future<?>> fn) {
    Object metric = new Object();
    AtomicReference<Object> responseMetric = new AtomicReference<>();
    AtomicReference<Object> failureMetric = new AtomicReference<>();
    clientMetrics = new ClientMetrics() {
      @Override
      public Object requestBegin(String uri, Object request) {
        return metric;
      }
      @Override
      public void requestEnd(Object requestMetric) {
        ctx.assertEquals(metric, requestMetric);
      }
      @Override
      public void responseEnd(Object requestMetric) {
        responseMetric.set(requestMetric);
      }
      @Override
      public void requestReset(Object requestMetric) {
        failureMetric.set(requestMetric);
      }
    };
    pool = poolBuilder().using(vertx).connectingTo(connectOptions().setMetricsName("the-client-metrics")).build();
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertSuccess(conn -> {
        fn.apply(conn).onComplete(ar -> {
          ctx.assertEquals(!fail, ar.succeeded());
          conn.close().onComplete(ctx.asyncAssertSuccess(v3 -> {
            vertx.runOnContext(v4 -> {
              if (fail) {
                ctx.assertNull(responseMetric.get());
                ctx.assertEquals(metric, failureMetric.get());
              } else {
                ctx.assertEquals(metric, responseMetric.get());
                ctx.assertNull(failureMetric.get());
              }
              ctx.assertEquals("sql", clientType);
              ctx.assertEquals("the-client-metrics", clientNamespace);
              async.complete();
            });
          }));
        });
      }));
    });
  }
}
