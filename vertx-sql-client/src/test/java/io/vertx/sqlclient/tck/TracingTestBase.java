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

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.impl.tracing.QueryRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class TracingTestBase {

  Vertx vertx;
  VertxTracer tracer;
  Pool pool;

  @Before
  public void setup() throws Exception {
    vertx = Vertx.builder()
      .withTracer(tracingOptions -> new VertxTracer() {
      @Override
      public Object sendRequest(Context context, SpanKind kind, TracingPolicy tracingPolicy, Object request, String operation, BiConsumer headers, TagExtractor tagExtractor) {
        return tracer.sendRequest(context, kind, tracingPolicy, request, operation, headers, tagExtractor);
      }
      @Override
      public void receiveResponse(Context context, Object response, Object payload, Throwable failure, TagExtractor tagExtractor) {
        tracer.receiveResponse(context, response, payload, failure, tagExtractor);
      }
    }).build();
    pool = createPool(vertx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected abstract Pool createPool(Vertx vertx);

  protected abstract String statement(String... parts);

  @Test
  public void testTraceSimpleQuery(TestContext ctx) {
    String sql = "SELECT * FROM immutable WHERE id=1";
    testTraceQuery(ctx, sql, Collections.emptyList(), pool -> pool.withConnection(conn -> conn.query(sql).execute()));
  }

  @Test
  public void testTracePooledSimpleQuery(TestContext ctx) {
    String sql = "SELECT * FROM immutable WHERE id=1";
    testTraceQuery(ctx, sql, Collections.emptyList(), pool -> pool.query(sql).execute());
  }

  @Test
  public void testTracePreparedQuery(TestContext ctx) {
    String sql = statement("SELECT * FROM immutable WHERE id = ", "");
    Tuple tuple = Tuple.of(1);
    testTraceQuery(ctx, sql, Collections.singletonList(tuple), pool -> pool.withConnection(conn -> conn.preparedQuery(sql).execute(tuple)));
  }

  @Test
  public void testTracePooledPreparedQuery(TestContext ctx) {
    String sql = statement("SELECT * FROM immutable WHERE id = ", "");
    Tuple tuple = Tuple.of(1);
    testTraceQuery(ctx, sql, Collections.singletonList(tuple), pool -> pool.preparedQuery(sql).execute(tuple));
  }

  @Test
  public void testTraceBatchQuery(TestContext ctx) {
    String sql = statement("SELECT * FROM immutable WHERE id = ", "");
    List<Tuple> tuples = Arrays.asList(Tuple.of(1), Tuple.of(2));
    testTraceQuery(ctx, sql, tuples, pool -> pool.withConnection(conn -> conn.preparedQuery(sql).executeBatch(tuples)));
  }

  @Test
  public void testTracePooledBatchQuery(TestContext ctx) {
    String sql = statement("SELECT * FROM immutable WHERE id = ", "");
    List<Tuple> tuples = Arrays.asList(Tuple.of(1), Tuple.of(2));
    testTraceQuery(ctx, sql, tuples, pool -> pool.preparedQuery(sql).executeBatch(tuples));
  }

  public void testTraceQuery(TestContext ctx, String expectedSql, List<Tuple> expectedTuples, Function<Pool, Future<?>> fn) {
    AtomicBoolean called = new AtomicBoolean();
    AtomicReference<Context> requestContext = new AtomicReference<>();
    AtomicReference<Context> responseContext = new AtomicReference<>();
    Async completed = ctx.async(2);
    Object expectedPayload = new Object();
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, SpanKind kind, TracingPolicy tracingPolicy, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        QueryRequest query = (QueryRequest) request;
        ctx.assertEquals(expectedSql, query.sql());
        ctx.assertEquals(expectedTuples, query.tuples());
        Map<String, String> tags = tagExtractor.extract(request);
        ctx.assertEquals("client", tags.get("span.kind"));
        ctx.assertEquals("sql", tags.get("db.type"));
        ctx.assertEquals(expectedSql, tags.get("db.statement"));
        String dbSystem = tags.get("db.system");
        ctx.assertNotNull(dbSystem);
        ctx.assertTrue(isValidDbSystem(dbSystem));
        requestContext.set(context);
        completed.countDown();
        return expectedPayload;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        RowSet rs = (RowSet) response;
        ctx.assertTrue(rs.iterator().hasNext());
        ctx.assertEquals(expectedPayload, payload);
        ctx.assertNull(failure);
        called.set(true);
        responseContext.set(context);
        completed.countDown();
      }
    };
    Async async = ctx.async();
    vertx.runOnContext(v1 -> {
      Context context = Vertx.currentContext();
      fn.apply(pool).onComplete(ctx.asyncAssertSuccess(v2 -> {
        vertx.runOnContext(v4 -> {
          completed.await(2000);
          ctx.assertEquals(context, requestContext.get());
          ctx.assertEquals(context, responseContext.get());
          ctx.assertTrue(called.get());
          async.complete();
        });
      }));
    });
  }

  protected abstract boolean isValidDbSystem(String dbSystem);

  @Test
  public void testTracingFailure(TestContext ctx) {
    Async completed = ctx.async();
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, SpanKind kind, TracingPolicy tracingPolicy, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        return null;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        ctx.assertNull(response);
        ctx.assertNotNull(failure);
        completed.complete();
      }
    };
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(statement("SELECT * FROM undefined_table WHERE id = ", ""))
        .execute(Tuple.of(0))
        .onComplete(ctx.asyncAssertFailure(err -> {
          conn.close();
        }));
    }));
  }

  @Test
  public void testMappingFailure(TestContext ctx) {
    RuntimeException failure = new RuntimeException();
    AtomicInteger called = new AtomicInteger();
    Async completed = ctx.async();
    String sql = statement("SELECT * FROM immutable WHERE id = ", "");
    tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, SpanKind kind, TracingPolicy tracingPolicy, R request, String operation, BiConsumer<String, String> headers, TagExtractor<R> tagExtractor) {
        return null;
      }
      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure, TagExtractor<R> tagExtractor) {
        ctx.assertEquals(1, called.incrementAndGet());
        completed.complete();
      }
    };
    Async async = ctx.async();
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(sql)
        .mapping(row -> {
          throw failure;
        })
        .execute(Tuple.of(1))
        .onComplete(ctx.asyncAssertFailure(err -> {
          conn.close().onComplete(ctx.asyncAssertSuccess(v1 -> {
            vertx.runOnContext(v2 -> {
              completed.await(2000);
              ctx.assertEquals(1, called.get());
              async.complete();
            });
          }));
        }));
    }));
  }
}
