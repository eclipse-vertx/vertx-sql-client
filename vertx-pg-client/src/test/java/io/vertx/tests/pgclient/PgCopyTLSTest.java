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

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.net.ClientSSLOptions;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgCopyInOptions;
import io.vertx.pgclient.PgCopyOut;
import io.vertx.pgclient.PgCopyOutOptions;
import io.vertx.pgclient.SslMode;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

/**
 * COPY over a TLS connection.
 * <p/>
 * This is worth covering on its own because the COPY IN sink drives backpressure from
 * {@code channel().isWritable()} and installs its handler relative to the codec, while an
 * {@code SslHandler} sits in the same pipeline doing its own buffering and re-framing.
 */
@RunWith(VertxUnitRunner.class)
public class PgCopyTLSTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule().ssl(true);

  private Vertx vertx;
  private PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new PgConnectOptions(rule.options())
      .setSslMode(SslMode.REQUIRE)
      .setSslOptions(new ClientSSLOptions().setTrustAll(true));
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCopyInOverTls(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(COPY_IN))
      .compose(in -> in.write(buf("1,alpha\n2,beta\n")).compose(x -> in.end()).compose(x -> in.completion()))
      .map(rows -> {
        ctx.assertEquals(2, rows);
        return null;
      }));
  }

  @Test
  public void testCopyOutOverTls(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, 5)) TO STDOUT")
      .compose(this::drain)
      .map(text -> {
        ctx.assertEquals("1\n2\n3\n4\n5\n", text);
        return null;
      }));
  }

  @Test
  public void testLargeCopyInOverTlsCrossesTlsRecordBoundaries(TestContext ctx) {
    // comfortably larger than the 16k TLS record size, so the payload spans many records
    int rows = 50_000;
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= rows; i++) {
      sb.append(i).append(",value-").append(i).append('\n');
    }
    String payload = sb.toString();

    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(COPY_IN, new PgCopyInOptions()))
      .compose(in -> in.write(buf(payload)).compose(x -> in.end()).compose(x -> in.completion()))
      .map(count -> {
        ctx.assertEquals(rows, count);
        return null;
      }));
  }

  @Test
  public void testLargeCopyOutOverTls(TestContext ctx) {
    int rows = 50_000;
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, " + rows + ")) TO STDOUT",
        new PgCopyOutOptions().setAggregationThreshold(32 * 1024))
      .compose(this::drain)
      .map(text -> {
        StringBuilder expected = new StringBuilder();
        for (int i = 1; i <= rows; i++) {
          expected.append(i).append('\n');
        }
        ctx.assertEquals(expected.toString(), text);
        return null;
      }));
  }

  @Test
  public void testCopyInBackpressureOverTls(TestContext ctx) {
    // a small write queue over TLS exercises the writability signalling through the SslHandler
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(COPY_IN, new PgCopyInOptions()
        .setChunkSize(1024)))
      .compose(in -> {
        in.setWriteQueueMaxSize(4096);
        Future<Void> writes = Future.succeededFuture();
        for (int i = 1; i <= 5000; i++) {
          int n = i;
          writes = writes.compose(v -> in.write(buf(n + ",value-" + n + "\n")));
        }
        return writes.compose(v -> in.end()).compose(v -> in.completion());
      })
      .map(rows -> {
        ctx.assertEquals(5000, rows);
        return null;
      }));
  }

  @Test
  public void testCopyOutPausedFetchOverTls(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, 200)) TO STDOUT")
      .compose(out -> {
        Promise<String> promise = Promise.promise();
        StringBuilder sb = new StringBuilder();
        AtomicInteger seen = new AtomicInteger();
        out.pause();
        out.handler(b -> {
          sb.append(b.toString(StandardCharsets.UTF_8));
          seen.incrementAndGet();
          out.fetch(1);
        });
        out.endHandler(v -> promise.complete(sb.toString()));
        out.exceptionHandler(promise::tryFail);
        out.fetch(1);
        return promise.future().compose(text -> out.completion().map(rows -> {
          ctx.assertEquals(200, rows);
          StringBuilder expected = new StringBuilder();
          for (int i = 1; i <= 200; i++) {
            expected.append(i).append('\n');
          }
          ctx.assertEquals(expected.toString(), text);
          return (Void) null;
        }));
      }));
  }

  @Test
  public void testAbortOverTlsRollsBackAndKeepsConnectionUsable(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(COPY_IN))
      .compose(in -> in.write(buf("1,alpha\n"))
        .compose(x -> in.abort("tls abort"))
        .compose(x -> in.completion().otherwise(-1)))
      .compose(v -> conn.query("SELECT count(*) AS c FROM copy_tls").execute())
      .map(rs -> {
        ctx.assertEquals(0, rs.iterator().next().getInteger("c"));
        return null;
      }));
  }

  @Test
  public void testCopyInRestoresWatermarksOverTls(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(COPY_IN))
      .compose(in -> in.write(buf("1,alpha\n")).compose(x -> in.end()).compose(x -> in.completion()))
      // a second COPY on the same TLS connection must still work after the sink detached
      .compose(v -> conn.copyIn(COPY_IN))
      .compose(in -> in.write(buf("2,beta\n")).compose(x -> in.end()).compose(x -> in.completion()))
      .compose(v -> conn.query("SELECT count(*) AS c FROM copy_tls").execute())
      .map(rs -> {
        ctx.assertEquals(2, rs.iterator().next().getInteger("c"));
        return null;
      }));
  }

  // ---------------------------------------------------------------- helpers

  private static final String COPY_IN = "COPY copy_tls (id, val) FROM STDIN WITH (FORMAT csv)";

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

  private static Future<Void> table(PgConnection conn) {
    return conn.query("CREATE TEMP TABLE copy_tls (id INT PRIMARY KEY, val TEXT NOT NULL)")
      .execute().mapEmpty();
  }

  private Future<String> drain(PgCopyOut out) {
    Promise<Buffer> promise = Promise.promise();
    Buffer acc = Buffer.buffer();
    out.handler(acc::appendBuffer);
    out.endHandler(v -> promise.tryComplete(acc));
    out.exceptionHandler(promise::tryFail);
    return promise.future().map(b -> b.toString(StandardCharsets.UTF_8));
  }

  private static Buffer buf(String value) {
    return Buffer.buffer(value, StandardCharsets.UTF_8.name());
  }
}
