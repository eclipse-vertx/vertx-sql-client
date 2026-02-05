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

package io.vertx.tests.sqlclient.tck;

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
import io.vertx.tests.sqlclient.ProxyServer;
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

import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

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
    awaitUntilSizeEquals(ctx, closeCount, 1);
  }

  @Test
  public void testQueuing(TestContext ctx) throws Exception {
    testQueuing(ctx, false);
  }

  @Test
  public void testQueuingTimeout(TestContext ctx) throws Exception {
    testQueuing(ctx, true);
  }

  private void testQueuing(TestContext ctx, boolean timeout) throws Exception {
    AtomicInteger queueSize = new AtomicInteger();
    List<Object> enqueueMetrics = Collections.synchronizedList(new ArrayList<>());
    List<Object> dequeueMetrics = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger usageSize = new AtomicInteger();
    List<Object> beginMetrics = Collections.synchronizedList(new ArrayList<>());
    List<Object> endMetrics = Collections.synchronizedList(new ArrayList<>());
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

      @Override
      public Object begin() {
        Object metric = new Object();
        beginMetrics.add(metric);
        usageSize.incrementAndGet();
        return metric;
      }

      @Override
      public void end(Object usageMetric) {
        endMetrics.add(usageMetric);
        usageSize.decrementAndGet();
      }
    };
    PoolOptions poolOptions = new PoolOptions().setMaxSize(1).setName("the-pool");
    if (timeout) {
      poolOptions.setConnectionTimeout(2).setConnectionTimeoutUnit(SECONDS);
    }
    Pool pool = createPool(vertx, poolOptions);
    SqlConnection conn = pool.getConnection().await(20, SECONDS);
    int num = 16;
    List<Future<?>> futures = new ArrayList<>();
    for (int i = 0;i < num;i++) {
      futures.add(pool.withConnection(sqlConn -> sqlConn.query("SELECT * FROM immutable WHERE id=1").execute()));
    }
    awaitUntilSizeEquals(ctx, queueSize, timeout ? 0 : num);
    conn.close();
    Future.join(futures).otherwiseEmpty().await(20, SECONDS);
    ctx.assertEquals(0, queueSize.get());
    ctx.assertEquals(enqueueMetrics, dequeueMetrics);
    awaitUntilSizeEquals(ctx, usageSize, 0);
    ctx.assertEquals(timeout ? 1 : num + 1, beginMetrics.size());
    ctx.assertEquals(timeout ? 1 : num + 1, endMetrics.size());
    ctx.assertEquals(beginMetrics, endMetrics);
    ctx.assertEquals("sql", poolType);
    ctx.assertEquals("the-pool", poolName);
  }

  private void awaitUntilSizeEquals(TestContext ctx, AtomicInteger queueSize, int num) throws InterruptedException {
    long now = System.currentTimeMillis();
    for (; ; ) {
      if (queueSize.get() != num) {
        if (System.currentTimeMillis() - now >= 20_000) {
          ctx.fail("Timeout waiting for queue size " + queueSize.get() + " to be equal to " + num);
        } else {
          MILLISECONDS.sleep(500);
        }
      } else {
        break;
      }
    }
  }

  @Test
  public void testConnectionLost(TestContext ctx) throws Exception {
    SqlConnectOptions connectOptions = connectOptions();
    ProxyServer proxy = ProxyServer.create(vertx, connectOptions.getPort(), connectOptions.getHost());
    AtomicReference<ProxyServer.Connection> firstConnection = new AtomicReference<>();
    proxy.proxyHandler(proxiedConn -> {
      if (firstConnection.compareAndSet(null, proxiedConn)) {
        proxiedConn.connect();
      }
    });
    // Start proxy
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(res -> listenLatch.complete()));
    listenLatch.awaitSuccess(20_000);


    AtomicInteger queueSize = new AtomicInteger();
    poolMetrics = new PoolMetrics() {
      @Override
      public Object enqueue() {
        queueSize.incrementAndGet();
        return null;
      }

      @Override
      public void dequeue(Object taskMetric) {
        queueSize.decrementAndGet();
      }
    };
    PoolOptions poolOptions = new PoolOptions()
      .setConnectionTimeout(500)
      .setConnectionTimeoutUnit(MILLISECONDS)
      .setMaxSize(1)
      .setName("the-pool");
    Pool pool = poolBuilder()
      .with(poolOptions)
      .using(vertx)
      .connectingTo(connectOptions.setHost("localhost").setPort(8080))
      .build();
    SqlConnection conn = pool.getConnection().await(20, SECONDS);
    int num = 16;
    Async async = ctx.async(num + 1);
    for (int i = 0; i < num; i++) {
      pool.withConnection(sqlConn -> sqlConn.query("SELECT * FROM immutable WHERE id=1").execute())
        .onComplete(ctx.asyncAssertFailure(t -> async.countDown()));
    }
    conn.closeHandler(v -> async.countDown());
    awaitUntilSizeEquals(ctx, queueSize, 16);
    firstConnection.get().clientSocket().close();
    async.await(20_000);
    ctx.assertEquals(0, queueSize.get());
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
      public Object init() {
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
