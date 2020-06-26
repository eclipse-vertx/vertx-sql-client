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
import io.vertx.core.spi.metrics.VertxMetrics;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public abstract class MetricsTestBase {

  Vertx vertx;
  ClientMetrics metrics;
  Pool pool;

  @Before
  public void setup() {
    vertx = Vertx.vertx(new VertxOptions().setMetricsOptions(
      new MetricsOptions().setEnabled(true).setFactory(tracingOptions -> new VertxMetrics() {
        @Override
        public ClientMetrics<?, ?, ?, ?> createClientMetrics(SocketAddress remoteAddress, String type, String namespace) {
          return metrics;
        }
      }))
    );
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected Pool getPool() {
    if (pool == null) {
      pool = createPool(vertx);
    }
    return pool;
  }

  protected abstract Pool createPool(Vertx vertx);

  protected abstract String statement(String... parts);

  @Test
  public void testClosePool(TestContext ctx) {
    AtomicInteger closeCount = new AtomicInteger();
    metrics = new ClientMetrics() {
      @Override
      public void close() {
        closeCount.incrementAndGet();
      }
    };
    Pool pool = createPool(vertx);
    ctx.assertEquals(0, closeCount.get());
    pool.close(ctx.asyncAssertSuccess(v -> {
      ctx.assertEquals(1, closeCount.get());
    }));
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
    Object queueMetric = new Object();
    AtomicReference<Object> responseMetric = new AtomicReference<>();
    AtomicReference<Object> failureMetric = new AtomicReference<>();
    AtomicInteger enqueueCount = new AtomicInteger();
    AtomicInteger dequeueCount = new AtomicInteger();
    metrics = new ClientMetrics() {
      @Override
      public Object enqueueRequest() {
        enqueueCount.incrementAndGet();
        return queueMetric;
      }
      @Override
      public void dequeueRequest(Object taskMetric) {
        dequeueCount.incrementAndGet();
      }
      @Override
      public Object requestBegin(String uri, Object request) {
        return metric;
      }
      @Override
      public void responseEnd(Object requestMetric, Object response) {
        responseMetric.set(requestMetric);
      }
      @Override
      public void requestReset(Object requestMetric) {
        failureMetric.set(requestMetric);
      }
    };
    Pool pool = getPool();
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        fn.apply(conn).onComplete(ar -> {
          ctx.assertEquals(!fail, ar.succeeded());
          conn.close(ctx.asyncAssertSuccess(v3 -> {
            vertx.runOnContext(v4 -> {
              if (fail) {
                ctx.assertNull(responseMetric.get());
                ctx.assertEquals(metric, failureMetric.get());
              } else {
                ctx.assertEquals(metric, responseMetric.get());
                ctx.assertNull(failureMetric.get());
              }
              ctx.assertEquals(1, enqueueCount.get());
              ctx.assertEquals(1, dequeueCount.get());
              async.complete();
            });
          }));
        });
      }));
    });
  }
}
