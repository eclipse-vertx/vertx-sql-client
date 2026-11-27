/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.pgclient;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.spi.tracing.SpanKind;
import io.vertx.core.spi.tracing.TagExtractor;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingOptions;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgCopyOut;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * COPY statements must show up in traces like any other statement the application runs.
 */
@RunWith(VertxUnitRunner.class)
public class PgCopyTracingTest extends PgTestBase {

  private Vertx vertx;
  private final List<String> requests = new CopyOnWriteArrayList<>();
  private final List<String> responses = new CopyOnWriteArrayList<>();

  @Before
  public void setup() throws Exception {
    super.setup();
    requests.clear();
    responses.clear();
    VertxTracer<Object, Object> tracer = new VertxTracer<Object, Object>() {
      @Override
      public <R> Object sendRequest(Context context, SpanKind kind, TracingPolicy policy, R request,
                                    String operation, BiConsumer<String, String> headers, TagExtractor<R> extractor) {
        requests.add(operation);
        return request;
      }

      @Override
      public <R> void receiveResponse(Context context, R response, Object payload, Throwable failure,
                                      TagExtractor<R> extractor) {
        responses.add(failure == null ? "ok" : "failed");
      }
    };
    vertx = Vertx.builder()
      .with(new VertxOptions().setTracingOptions(new TracingOptions()))
      .withTracer(options -> tracer)
      .build();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCopyInIsTraced(TestContext ctx) {
    run(ctx, conn -> conn.query("CREATE TEMP TABLE copy_trace (id INT, val TEXT)").execute()
      .compose(v -> {
        requests.clear();
        responses.clear();
        return conn.copyIn("COPY copy_trace FROM STDIN WITH (FORMAT csv)");
      })
      .compose(in -> in.write(Buffer.buffer("1,alpha\n", StandardCharsets.UTF_8.name()))
        .compose(x -> in.end())
        .compose(x -> in.completion()))
      .map(rows -> {
        ctx.assertEquals(1, rows);
        ctx.assertEquals(1, requests.size(), "COPY IN produced no trace span, spans=" + requests);
        ctx.assertEquals("Query", requests.get(0));
        ctx.assertEquals(1, responses.size());
        ctx.assertEquals("ok", responses.get(0));
        return null;
      }));
  }

  @Test
  public void testCopyOutIsTraced(TestContext ctx) {
    run(ctx, conn -> {
      requests.clear();
      responses.clear();
      return conn.copyOut("COPY (SELECT generate_series(1, 3)) TO STDOUT")
        .compose(out -> drain(out).compose(text -> out.completion()))
        .map(rows -> {
          ctx.assertEquals(3, rows);
          ctx.assertEquals(1, requests.size(), "COPY OUT produced no trace span, spans=" + requests);
          ctx.assertEquals("Query", requests.get(0));
          ctx.assertEquals(1, responses.size());
          ctx.assertEquals("ok", responses.get(0));
          return null;
        });
    });
  }

  @Test
  public void testFailedCopyIsReportedAsAFailedSpan(TestContext ctx) {
    run(ctx, conn -> {
      requests.clear();
      responses.clear();
      return conn.copyOut("COPY (SELECT 1 / 0) TO STDOUT")
        .compose(out -> drain(out).mapEmpty(), err -> Future.succeededFuture())
        // the span closes at ReadyForQuery, which is also what lets the next statement run
        .compose(v -> conn.query("SELECT 1").execute())
        .map(v -> {
          // the trailing SELECT is traced too, so assert on the first span, which is the COPY
          ctx.assertFalse(requests.isEmpty(), "no span for the failed COPY");
          ctx.assertEquals("Query", requests.get(0));
          ctx.assertFalse(responses.isEmpty(), "the COPY span was never closed");
          ctx.assertEquals("failed", responses.get(0));
          return null;
        });
    });
  }

  private void run(TestContext ctx, Function<PgConnection, Future<?>> body) {
    PgConnection.connect(vertx, options)
      .compose(conn -> {
        Future<?> op;
        try {
          op = body.apply(conn);
        } catch (Throwable t) {
          op = Future.failedFuture(t);
        }
        return op.eventually(conn::close);
      })
      .onComplete(ctx.asyncAssertSuccess());
  }

  private Future<String> drain(PgCopyOut out) {
    Promise<Buffer> promise = Promise.promise();
    Buffer acc = Buffer.buffer();
    out.handler(acc::appendBuffer);
    out.endHandler(v -> promise.tryComplete(acc));
    out.exceptionHandler(promise::tryFail);
    return promise.future().map(b -> b.toString(StandardCharsets.UTF_8));
  }
}
