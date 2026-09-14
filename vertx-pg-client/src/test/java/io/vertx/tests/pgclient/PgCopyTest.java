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

import io.netty.channel.ChannelConfig;
import io.netty.channel.WriteBufferWaterMark;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.PgCopyIn;
import io.vertx.pgclient.PgCopyInOptions;
import io.vertx.pgclient.PgCopyOut;
import io.vertx.pgclient.PgCopyOutOptions;
import io.vertx.pgclient.PgException;
import io.vertx.pgclient.impl.PgConnectionImpl;
import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;

public class PgCopyTest extends PgTestBase {

  private Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCopyInDefaultDirectImmediateTransport(TestContext ctx) {
    PgCopyInOptions options = new PgCopyInOptions();
    assertCopyInCsv(ctx, options, Arrays.asList("1:alpha", "2:beta", "3:gamma"));
  }

  @Test
  public void testCopyOutDefaultAggregationThreshold(TestContext ctx) {
    PgCopyOutOptions options = new PgCopyOutOptions();
    ctx.assertEquals(1, options.getAggregationThreshold());
  }

  @Test
  public void testCopyOptionsRejectInvalidValues(TestContext ctx) {
    assertThrows(ctx, IllegalArgumentException.class, () -> new PgCopyInOptions().setChunkSize(0));
    assertThrows(ctx, IllegalArgumentException.class, () -> new PgCopyInOptions().setChunkSize(-1));
    assertThrows(ctx, IllegalArgumentException.class, () -> new PgCopyOutOptions().setAggregationThreshold(0));
    assertThrows(ctx, IllegalArgumentException.class, () -> new PgCopyOutOptions().setAggregationThreshold(-1));
  }

  @Test
  public void testCopyInCoalescingTransportSmallChunks(TestContext ctx) {
    assertCopyInCsv(ctx, new PgCopyInOptions()
      .setChunkSize(8), Arrays.asList("1:alpha", "2:beta", "3:gamma"));
  }

  @Test
  public void testCopyInCoalescingTransportLargeBuffer(TestContext ctx) {
    assertCopyInCsvSingleLargeBuffer(ctx, new PgCopyInOptions()
      .setChunkSize(1024 * 1024));
  }

  @Test
  public void testCopyInCoalescingHandoffSmallChunks(TestContext ctx) {
    assertCopyInCsv(ctx, new PgCopyInOptions()
      .setChunkSize(8), Arrays.asList("1:alpha", "2:beta", "3:gamma"));
  }

  @Test
  public void testCopyInCoalescingHandoffLargeBuffer(TestContext ctx) {
    assertCopyInCsvSingleLargeBuffer(ctx, new PgCopyInOptions()
      .setChunkSize(1024 * 1024));
  }

  @Test
  public void testCopyInCoalescingSequentialComposedWrites(TestContext ctx) {
    assertCopyInSequentialComposedWrites(ctx);
  }

  @Test
  public void testCopyInCoalescingEmptyWriteThenEnd(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn(
        "COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)",
        new PgCopyInOptions()
          .setChunkSize(1024 * 1024)))
      .compose(in -> in.write(Buffer.buffer()).compose(v -> in.end()))
      .compose(v -> fetchCount(conn, "copy_test"))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInRestoresCustomChannelWatermarkAfterSuccess(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test").compose(v -> {
      ChannelConfig config = channelConfig(conn);
      WriteBufferWaterMark original = new WriteBufferWaterMark(123456, 234567);
      config.setWriteBufferWaterMark(original);

      return conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)")
        .compose(in -> {
          assertWatermark(ctx, config.getWriteBufferWaterMark(), 4 * 1024 * 1024, 8 * 1024 * 1024);
          in.setWriteQueueMaxSize(512 * 1024);
          assertWatermark(ctx, config.getWriteBufferWaterMark(), 256 * 1024, 512 * 1024);
          return in.write(buf("1,alpha\n")).compose(x -> in.end());
        })
        .map(x -> {
          ctx.assertTrue(config.getWriteBufferWaterMark() == original);
          return null;
        });
    })).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInRestoresCustomChannelWatermarkAfterAbort(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test").compose(v -> {
      ChannelConfig config = channelConfig(conn);
      WriteBufferWaterMark original = new WriteBufferWaterMark(123457, 234568);
      config.setWriteBufferWaterMark(original);

      return conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)")
        .compose(in -> in.abort("rollback")
          .compose(x -> expectFailure(in.completion(), "abort should fail completion")))
        .compose(x -> conn.query("SELECT 1").execute())
        .map(x -> {
          ctx.assertTrue(config.getWriteBufferWaterMark() == original);
          return null;
        });
    })).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInRestoresCustomChannelWatermarkAfterServerError(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test").compose(v -> {
      ChannelConfig config = channelConfig(conn);
      WriteBufferWaterMark original = new WriteBufferWaterMark(123458, 234569);
      config.setWriteBufferWaterMark(original);

      return conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)")
        .compose(in -> in.write(buf("not-an-int,alpha\n"))
          .compose(x -> expectFailure(in.end(), "server-side COPY error should fail end")))
        .compose(x -> conn.query("SELECT 1").execute())
        .map(x -> {
          ctx.assertTrue(config.getWriteBufferWaterMark() == original);
          return null;
        });
    })).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInDirectDelayedFlush(TestContext ctx) {
    assertCopyInCsv(ctx, new PgCopyInOptions(), Arrays.asList("1:alpha", "2:beta", "3:gamma"));
  }

  @Test
  public void testCopyInDirectImmediateFlush(TestContext ctx) {
    assertCopyInCsv(ctx, new PgCopyInOptions(), Arrays.asList("1:alpha", "2:beta", "3:gamma"));
  }

  @Test
  public void testCopyInFragmentedCsvLineAcrossManyWrites(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)", new PgCopyInOptions().setChunkSize(4)))
      .compose(in -> {
        List<Future<Void>> writes = new ArrayList<>();
        writes.add(in.write(buf("1,")));
        writes.add(in.write(buf("al")));
        writes.add(in.write(buf("pha\n2")));
        writes.add(in.write(buf(",be")));
        writes.add(in.write(buf("ta\n")));
        writes.add(in.write(buf("3,gamma\n")));

        Future<Void> completion = in.end();
        return Future.all(writes).compose(v -> completion);
      })
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta", "3:gamma"), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInCsvHeaderAndEscaping(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv, HEADER true)"))
      .compose(in -> {
        Future<Void> write = in.write(buf(
          "id,val\n" +
            "1,alpha\n" +
            "2,\"hello, world\"\n" +
            "3,\"he said \"\"yo\"\"\"\n"));
        Future<Void> completion = in.end();
        return write.compose(v -> completion);
      })
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:hello, world", "3:he said \"yo\""), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInTextFormat(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT text)"))
      .compose(in -> {
        Future<Void> write = in.write(buf("1\talpha\n2\tbeta\n3\tgamma\n"));
        Future<Void> completion = in.end();
        return write.compose(v -> completion);
      })
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta", "3:gamma"), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInEmptyEnd(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
      .compose(PgCopyIn::end)
      .compose(v -> fetchCount(conn, "copy_test"))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInWriteAfterEndFailsAndDoesNotInsert(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
      .compose(in -> {
        Future<Void> completion = in.end();
        return expectFailure(in.write(buf("1,late\n")), "write after end should fail")
          .compose(v -> completion);
      })
      .compose(v -> fetchCount(conn, "copy_test"))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInAbortAfterEndFailsAndDoesNotSendCopyFail(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
      .compose(in -> {
        Future<Void> completion = in.end();
        return expectFailure(in.abort("too late"), "abort after end should fail")
          .compose(v -> completion);
      })
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInWriteAfterAbortFailsAndConnectionIsReusable(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
      .compose(in -> in.abort("stop")
        .compose(v -> expectFailure(in.write(buf("1,late\n")), "write after abort should fail"))
        .compose(v -> expectFailure(in.completion(), "abort should fail completion")))
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInAbortAfterBufferedCoalescingDataRollsBackCopy(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn(
        "COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)",
        new PgCopyInOptions()
          .setChunkSize(1024 * 1024)))
      .compose(in -> {
        Future<Void> write = in.write(buf("1,alpha\n2,beta\n"));
        Future<Void> abort = in.abort("rollback copy");

        return expectFailure(write, "buffered coalescing write should fail after abort")
          .compose(v -> abort)
          .compose(v -> expectFailure(in.completion(), "abort should fail completion"));
      })
      .compose(v -> fetchCount(conn, "copy_test"))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInServerErrorFailsCompletionAndConnectionIsReusable(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
      .compose(in -> {
        Future<Void> write = in.write(buf("not-an-int,alpha\n"));
        Future<Void> completion = in.end();

        return write.otherwiseEmpty()
          .compose(v -> expectFailure(completion, "server-side COPY error should fail completion"));
      })
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInFollowingQueryWaitsForCopyCompletion(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)", new PgCopyInOptions().setChunkSize(1024 * 1024)))
      .compose(in -> {
        Future<RowSet<Row>> select = conn.query("SELECT count(*) AS c FROM copy_test").execute();
        ctx.assertFalse(select.isComplete());

        Future<Void> write = in.write(buf("1,alpha\n2,beta\n"));
        Future<Void> completion = in.end();

        return Future.all(write, completion).compose(v -> select);
      })
      .map(rows -> {
        ctx.assertEquals(2, rows.iterator().next().getInteger("c"));
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInRejectsInvalidSql(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> Future.all(
        // rejected as a failed future, never thrown at the caller
        assertFailsWith(ctx, NullPointerException.class, conn.copyIn(null)),
        assertFailsWith(ctx, IllegalArgumentException.class, conn.copyIn("SELECT 1")),
        assertFailsWith(ctx, IllegalArgumentException.class, conn.copyIn("COPY (SELECT 1) TO STDOUT")))
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutDefaultUnpaused(TestContext ctx) {
    Async async = ctx.async();

    PgCopyOutOptions options = new PgCopyOutOptions();
    ctx.assertEquals(1, options.getAggregationThreshold());

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> insertTextRows(conn, "copy_test"))
      .compose(v -> conn.copyOut("COPY (SELECT id, val FROM copy_test ORDER BY id) TO STDOUT WITH (FORMAT csv)"))
      .compose(out -> collectFlowing(out)
        .compose(csv -> out.completion().map(rowCount -> {
          ctx.assertEquals("1,alpha\n2,beta\n3,gamma\n", normalize(csv));
          ctx.assertEquals(3, rowCount);
          return null;
        })))
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutExplicitAggregationThresholdOneCsvHeader(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> insertTextRows(conn, "copy_test"))
      .compose(v -> conn.copyOut(
        "COPY (SELECT id, val FROM copy_test ORDER BY id) TO STDOUT WITH (FORMAT csv, HEADER true)",
        new PgCopyOutOptions().setAggregationThreshold(1)))
      .compose(out -> collect(out).map(csv -> {
        ctx.assertEquals("id,val\n1,alpha\n2,beta\n3,gamma\n", normalize(csv));
        return null;
      }))
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutLargeAggregationThresholdUnpaused(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> insertTextRows(conn, "copy_test"))
      .compose(v -> conn.copyOut(
        "COPY (SELECT id, val FROM copy_test ORDER BY id) TO STDOUT WITH (FORMAT csv)",
        new PgCopyOutOptions().setAggregationThreshold(1024 * 1024)))
      .compose(out -> collectFlowing(out).map(csv -> {
        ctx.assertEquals("1,alpha\n2,beta\n3,gamma\n", normalize(csv));
        return null;
      }))
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutTextFormat(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> insertTextRows(conn, "copy_test"))
      .compose(v -> conn.copyOut("COPY (SELECT id, val FROM copy_test ORDER BY id) TO STDOUT WITH (FORMAT text)"))
      .compose(out -> collect(out).map(text -> {
        ctx.assertEquals("1\talpha\n2\tbeta\n3\tgamma\n", normalize(text));
        return null;
      }))
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutPausedFetchOneByOne(TestContext ctx) {
    Async async = ctx.async();

    PgCopyOutOptions options = new PgCopyOutOptions();
    ctx.assertEquals(1, options.getAggregationThreshold());

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> insertTextRows(conn, "copy_test"))
      .compose(v -> conn.copyOut(
        "COPY (SELECT id, val FROM copy_test ORDER BY id) TO STDOUT WITH (FORMAT csv)"))
      .compose(out -> {
        StringBuilder sb = new StringBuilder();
        Promise<String> drained = Promise.promise();

        out.pause();
        out.handler(buffer -> {
          sb.append(buffer.toString(StandardCharsets.UTF_8));
          out.fetch(1);
        });
        out.exceptionHandler(drained::tryFail);
        out.endHandler(v -> drained.tryComplete(sb.toString()));
        out.completion().onFailure(drained::tryFail);
        out.fetch(1);

        return drained.future();
      })
      .map(csv -> {
        ctx.assertEquals("1,alpha\n2,beta\n3,gamma\n", normalize(csv));
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutFetchOneByOneAcrossTransportWatermarks(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> conn.copyOut(
        "COPY (SELECT generate_series(1, 256)) TO STDOUT WITH (FORMAT text)")
      .compose(out -> {
        AtomicInteger chunks = new AtomicInteger();
        Promise<Integer> drained = Promise.promise();

        out.handler(buffer -> {
          chunks.incrementAndGet();
          out.fetch(1);
        });
        out.exceptionHandler(drained::tryFail);
        out.endHandler(v -> drained.tryComplete(chunks.get()));
        out.completion().onFailure(drained::tryFail);
        out.fetch(1);

        return drained.future().compose(count -> out.completion().map(rowCount -> {
          ctx.assertEquals(256, count);
          ctx.assertEquals(256, rowCount);
          return null;
        }));
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutServerErrorFailsReadyFutureAndConnectionIsReusable(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> conn.copyOut("COPY (SELECT * FROM missing_copy_table) TO STDOUT WITH (FORMAT csv)")
      .compose(v -> Future.failedFuture("COPY OUT should have failed"), err -> Future.succeededFuture())
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutRejectsInvalidSql(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> Future.all(
        // rejected as a failed future, never thrown at the caller
        assertFailsWith(ctx, NullPointerException.class, conn.copyOut(null)),
        assertFailsWith(ctx, IllegalArgumentException.class, conn.copyOut("SELECT 1")),
        assertFailsWith(ctx, IllegalArgumentException.class, conn.copyOut("COPY copy_test FROM STDIN"))))
      .compose(v -> conn.query("SELECT 1").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutUnexpectedResponseFromPlainQueryClosesConnection(TestContext ctx) {
    assertUnexpectedCopyQueryClosesConnection(ctx, "COPY (SELECT 1) TO STDOUT WITH (FORMAT csv)");
  }

  @Test
  public void testCopyInUnexpectedResponseFromPlainQueryClosesConnection(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> unexpectedCopyQuery(conn, "COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyInAbortOnPooledConnectionRecycle(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)).using(vertx));

    pool.getConnection()
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return createTextTable(pg, "copy_test")
          .compose(v -> pg.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)"))
          .compose(copy -> copy.write(buf("1,alpha\n")).compose(v -> pg.close()));
      })
      .compose(v -> pool.getConnection())
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return fetchCount(pg, "copy_test")
          .map(count -> {
            ctx.assertEquals(0, count);
            return null;
          })
          .eventually(pg::close);
      })
      .eventually(pool::close)
      .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testPendingCopyInAbortOnPooledConnectionRecycle(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)).using(vertx));

    pool.getConnection()
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return createTextTable(pg, "copy_test")
          .compose(v -> {
            pg.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)");
            return pg.close();
          });
      })
      .compose(v -> pool.getConnection())
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return fetchCount(pg, "copy_test")
          .map(count -> {
            ctx.assertEquals(0, count);
            return null;
          })
          .eventually(pg::close);
      })
      .eventually(pool::close)
      .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutCompletesBeforePooledConnectionRecycle(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)).using(vertx));

    pool.getConnection()
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return pg.copyOut("COPY (SELECT generate_series(1, 5000)) TO STDOUT WITH (FORMAT csv)")
          .compose(copy -> pg.close());
      })
      .compose(v -> pool.getConnection())
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        return pg.query("SELECT 1").execute().mapEmpty().eventually(pg::close);
      })
      .eventually(pool::close)
      .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutPipeToCopyInCsvAcrossConnections(TestContext ctx) {
    Async async = ctx.async();

    withTwoConnections((src, dst) -> createTextTable(src, "copy_src")
      .compose(v -> createTextTable(dst, "copy_dst"))
      .compose(v -> insertTextRows(src, "copy_src"))
      .compose(v -> src.copyOut("COPY (SELECT id, val FROM copy_src ORDER BY id) TO STDOUT WITH (FORMAT csv)"))
      .compose(out -> dst.copyIn("COPY copy_dst (id, val) FROM STDIN WITH (FORMAT csv)")
        .compose(in -> out.pipeTo(in)
          .compose(v -> out.completion())
          .compose(v -> in.completion())))
      .compose(v -> fetchTextRows(dst, "copy_dst"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta", "3:gamma"), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testCopyOutPipeToCopyInBinaryAcrossConnections(TestContext ctx) {
    Async async = ctx.async();

    withTwoConnections((src, dst) -> createTextTable(src, "copy_src")
      .compose(v -> createTextTable(dst, "copy_dst"))
      .compose(v -> insertTextRows(src, "copy_src"))
      .compose(v -> src.copyOut("COPY (SELECT id, val FROM copy_src ORDER BY id) TO STDOUT WITH (FORMAT binary)"))
      .compose(out -> dst.copyIn("COPY copy_dst (id, val) FROM STDIN WITH (FORMAT binary)")
        .compose(in -> out.pipeTo(in)
          .compose(v -> out.completion())
          .compose(v -> in.completion())))
      .compose(v -> fetchTextRows(dst, "copy_dst"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta", "3:gamma"), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }


  // ================================================================
  // Contract: how the futures resolve, how the connection is guarded,
  // how bad input is reported.
  // ================================================================

  // ---------------------------------------------------------------- completion semantics

  @Test
  public void testCopyInCompletionMeansConnectionIsReady(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.write(buf("1,alpha\n")).compose(x -> in.end()).map(x -> in))
      .compose(in -> in.completion())
      .compose(rows -> {
        ctx.assertEquals(1, rows);
        // completion() resolves at ReadyForQuery, so the connection takes a new command right away
        return conn.query("SELECT 1 AS n").execute();
      })
      .map(rs -> {
        ctx.assertEquals(1, rs.iterator().next().getInteger("n"));
        return null;
      }));
  }

  @Test
  public void testCopyOutCompletionMeansConnectionIsReady(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, 3)) TO STDOUT")
      .compose(out -> drain(out).compose(text -> {
        ctx.assertEquals("1\n2\n3\n", text);
        return out.completion();
      }))
      .compose(rows -> {
        ctx.assertEquals(3, rows);
        return conn.query("SELECT 1 AS n").execute();
      })
      .map(rs -> {
        ctx.assertEquals(1, rs.iterator().next().getInteger("n"));
        return null;
      }));
  }

  @Test
  public void testCopyInEndFutureResolvesWhenCopyCompletes(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.write(buf("1,alpha\n2,beta\n")).compose(x -> in.end()))
      .compose(v -> rows(conn))
      .map(rows -> {
        ctx.assertEquals(2, rows.size());
        return null;
      }));
  }

  // ---------------------------------------------------------------- abort

  @Test
  public void testAbortSurfacesTheServerErrorInCompletion(TestContext ctx) {
    AtomicReference<Throwable> cause = new AtomicReference<>();
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.write(buf("1,alpha\n"))
        .compose(x -> in.abort("stop right there"))
        .compose(x -> in.completion().otherwise(err -> {
          cause.set(err);
          return -1;
        })))
      .map(v -> {
        Throwable err = cause.get();
        ctx.assertNotNull(err, "completion() should have failed");
        // the outcome is reported by PostgreSQL, not synthesised by the client
        ctx.assertTrue(err instanceof PgException, "expected a PgException but got " + err);
        ctx.assertTrue(err.getMessage().contains("stop right there"),
          "server error should quote the abort message, was: " + err.getMessage());
        return null;
      }));
  }

  @Test
  public void testAbortRollsBackEverythingWritten(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.write(buf("1,alpha\n2,beta\n"))
        .compose(x -> in.abort("rollback"))
        .compose(x -> in.completion().otherwise(-1)))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      }));
  }

  @Test
  public void testAbortItselfSucceeds(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.abort("done")
        .compose(x -> in.completion().otherwise(-1)))
      .mapEmpty());
  }

  // ---------------------------------------------------------------- one COPY per connection

  @Test
  public void testSecondCopyInWhileFirstIsActiveIsRejected(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> conn.copyIn(copyIn())
        .compose(second -> Future.<Void>failedFuture("a second COPY IN should have been rejected"),
          err -> {
            ctx.assertTrue(err instanceof IllegalStateException, "was " + err);
            return Future.<Void>succeededFuture();
          })
        .compose(x -> in.abort("cleanup"))
        .compose(x -> in.completion().otherwise(-1)))
      .mapEmpty());
  }

  @Test
  public void testCopyOutWhileCopyInIsActiveIsRejected(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> conn.copyOut("COPY (SELECT 1) TO STDOUT")
        .compose(out -> Future.<Void>failedFuture("COPY OUT should have been rejected"),
          err -> {
            ctx.assertTrue(err instanceof IllegalStateException, "was " + err);
            return Future.<Void>succeededFuture();
          })
        .compose(x -> in.abort("cleanup"))
        .compose(x -> in.completion().otherwise(-1)))
      .mapEmpty());
  }

  @Test
  public void testCopyInAfterPreviousOneCompletedSucceeds(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, "1,alpha\n"))
      .compose(v -> copyRow(conn, "2,beta\n"))
      .compose(v -> copyRow(conn, "3,gamma\n"))
      .compose(v -> rows(conn))
      .map(rows -> {
        ctx.assertEquals(3, rows.size());
        return null;
      }));
  }

  @Test
  public void testCopyOutAfterCopyInOnTheSameConnection(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, "1,alpha\n"))
      .compose(v -> conn.copyOut("COPY copy_contract TO STDOUT WITH (FORMAT csv)"))
      .compose(this::drain)
      .map(text -> {
        ctx.assertEquals("1,alpha\n", text);
        return null;
      }));
  }

  // ---------------------------------------------------------------- rejected SQL

  @Test
  public void testInvalidSqlFailsTheFutureInsteadOfThrowing(TestContext ctx) {
    run(ctx, conn -> {
      // must not throw at call time
      Future<PgCopyIn> in = conn.copyIn("SELECT 1");
      return assertFailsWithType(ctx, in, IllegalArgumentException.class)
        .compose(v -> assertFailsWithType(ctx, conn.copyOut("SELECT 1"), IllegalArgumentException.class));
    });
  }

  @Test
  public void testNullSqlFailsTheFuture(TestContext ctx) {
    run(ctx, conn -> Future.all(
      assertFailsWithType(ctx, conn.copyIn(null), NullPointerException.class),
      assertFailsWithType(ctx, conn.copyOut(null), NullPointerException.class)
    ).mapEmpty());
  }

  @Test
  public void testWrongDirectionIsRejected(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> assertFailsWithType(ctx, conn.copyIn("COPY copy_contract TO STDOUT"), IllegalArgumentException.class))
      .compose(v -> assertFailsWithType(ctx, conn.copyOut("COPY copy_contract FROM STDIN"), IllegalArgumentException.class))
      // the connection survives both mismatches
      .compose(v -> copyRow(conn, "1,alpha\n"))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(1, count);
        return null;
      }));
  }

  @Test
  public void testMultipleStatementsAreRejected(TestContext ctx) {
    run(ctx, conn -> assertFailsWithType(ctx,
      conn.copyIn("COPY copy_contract FROM STDIN; DROP TABLE copy_contract"),
      IllegalArgumentException.class).mapEmpty());
  }

  @Test
  public void testSemicolonInsideAnOptionIsNotAStatementSeparator(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn("COPY copy_contract (id, val) FROM STDIN WITH (FORMAT csv, DELIMITER ';')"))
      .compose(in -> in.write(buf("1;alpha\n2;beta\n")).compose(v -> in.end()))
      .compose(v -> rows(conn))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta"), rows);
        return null;
      }));
  }

  @Test
  public void testSemicolonInsideAQuotedIdentifierIsNotAStatementSeparator(TestContext ctx) {
    run(ctx, conn -> exec(conn, "CREATE TEMP TABLE \"odd;name\" (id INT)")
      .compose(v -> conn.copyIn("COPY \"odd;name\" FROM STDIN WITH (FORMAT csv)"))
      .compose(in -> in.write(buf("1\n")).compose(v -> in.end()))
      .mapEmpty());
  }

  @Test
  public void testATrailingStatementAfterAQuotedSemicolonIsStillRejected(TestContext ctx) {
    run(ctx, conn -> assertFailsWithType(ctx,
      conn.copyIn("COPY copy_contract FROM STDIN (DELIMITER ';'); DROP TABLE copy_contract"),
      IllegalArgumentException.class).mapEmpty());
  }

  @Test
  public void testKeywordsMayBeWrappedOverSeveralLines(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn("COPY copy_contract (id, val)\n  FROM\n  STDIN\n  WITH (FORMAT csv)"))
      .compose(in -> in.write(buf("1,alpha\n")).compose(v -> in.end()))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(1, count);
        return null;
      }));
  }

  @Test
  public void testCopyOutKeywordsMayBeWrappedOverSeveralLines(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, "1,alpha\n"))
      .compose(v -> conn.copyOut("COPY copy_contract\n  TO\n  STDOUT\n  WITH (FORMAT csv)"))
      .compose(out -> drain(out).compose(data -> out.completion().map(rowCount -> {
        ctx.assertEquals(1, rowCount);
        ctx.assertEquals("1,alpha\n", data);
        return null;
      }))));
  }

  @Test
  public void testRejectedSqlLeavesTheConnectionUsable(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> assertFailsWithType(ctx, conn.copyIn("SELECT 1"), IllegalArgumentException.class))
      // the guard must not have been taken, a real COPY still works
      .compose(v -> copyRow(conn, "1,alpha\n"))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(1, count);
        return null;
      }));
  }

  // ---------------------------------------------------------------- data fidelity

  @Test
  public void testRoundTripPreservesSpecialCharacters(TestContext ctx) {
    String tricky = "a \"quoted\" value, with comma\nand newline";
    String csv = "1,\"" + tricky.replace("\"", "\"\"") + "\"\n";
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, csv))
      .compose(v -> conn.query("SELECT val FROM copy_contract WHERE id = 1").execute())
      .map(rs -> {
        ctx.assertEquals(tricky, rs.iterator().next().getString("val"));
        return null;
      }));
  }

  @Test
  public void testRoundTripPreservesUnicode(TestContext ctx) {
    String unicode = "héllo — Ω 日本語 🐘";
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, "1,\"" + unicode + "\"\n"))
      .compose(v -> conn.query("SELECT val FROM copy_contract WHERE id = 1").execute())
      .map(rs -> {
        ctx.assertEquals(unicode, rs.iterator().next().getString("val"));
        return null;
      }));
  }

  @Test
  public void testTextFormatNullMarker(TestContext ctx) {
    run(ctx, conn -> exec(conn, "CREATE TEMP TABLE copy_nullable (id INT, val TEXT)")
      .compose(v -> conn.copyIn("COPY copy_nullable FROM STDIN"))
      .compose(in -> in.write(buf("1\t\\N\n")).compose(x -> in.end()))
      .compose(v -> conn.query("SELECT val FROM copy_nullable WHERE id = 1").execute())
      .map(rs -> {
        ctx.assertNull(rs.iterator().next().getString("val"));
        return null;
      }));
  }

  @Test
  public void testCopyOutOfAnEmptyTable(TestContext ctx) {
    AtomicBoolean ended = new AtomicBoolean();
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyOut("COPY copy_contract TO STDOUT WITH (FORMAT csv)"))
      .compose(out -> drain(out, ended).compose(text -> {
        ctx.assertEquals("", text);
        return out.completion();
      }))
      .map(rows -> {
        ctx.assertEquals(0, rows);
        ctx.assertTrue(ended.get(), "endHandler should have fired");
        return null;
      }));
  }

  @Test
  public void testCopyInWithNoRows(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.end().compose(x -> in.completion()))
      .map(rows -> {
        ctx.assertEquals(0, rows);
        return null;
      }));
  }

  @Test
  public void testLargeCopyInAndOutRoundTrip(TestContext ctx) {
    int count = 20_000;
    StringBuilder sb = new StringBuilder();
    for (int i = 1; i <= count; i++) {
      sb.append(i).append(",value-").append(i).append('\n');
    }
    String payload = sb.toString();

    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn(), new PgCopyInOptions()))
      .compose(in -> in.write(buf(payload)).compose(x -> in.end()).compose(x -> in.completion()))
      .compose(rows -> {
        ctx.assertEquals(count, rows);
        return conn.copyOut("COPY copy_contract TO STDOUT WITH (FORMAT csv)");
      })
      .compose(this::drain)
      .map(text -> {
        ctx.assertEquals(payload.length(), text.length());
        ctx.assertEquals(payload, text);
        return null;
      }));
  }

  @Test
  public void testCopyOutQueryForm(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT i, 'v' || i FROM generate_series(1, 5) AS i) TO STDOUT WITH (FORMAT csv)")
      .compose(this::drain)
      .map(text -> {
        ctx.assertEquals("1,v1\n2,v2\n3,v3\n4,v4\n5,v5\n", text);
        return null;
      }));
  }

  @Test
  public void testBinaryFormatRoundTripOnOneConnection(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> copyRow(conn, "1,alpha\n2,beta\n"))
      .compose(v -> conn.copyOut("COPY copy_contract TO STDOUT WITH (FORMAT binary)"))
      .compose(out -> collectBytes(out))
      .compose(binary -> exec(conn, "DELETE FROM copy_contract")
        .compose(v -> conn.copyIn("COPY copy_contract FROM STDIN WITH (FORMAT binary)"))
        .compose(in -> in.write(binary).compose(x -> in.end()).compose(x -> in.completion())))
      .compose(rows -> {
        ctx.assertEquals(2, rows);
        return rows(conn);
      })
      .map(all -> {
        ctx.assertEquals(2, all.size());
        ctx.assertEquals("1:alpha", all.get(0));
        return null;
      }));
  }

  // ---------------------------------------------------------------- stream surface

  @Test
  public void testEndWithBufferWritesTheData(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> in.end(buf("1,alpha\n")).compose(x -> in.completion()))
      .map(rows -> {
        ctx.assertEquals(1, rows);
        return null;
      }));
  }

  @Test
  public void testWriteQueueFullAndDrainHandler(TestContext ctx) {
    AtomicInteger drains = new AtomicInteger();
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn(), new PgCopyInOptions()
        .setChunkSize(64)))
      .compose(in -> {
        in.setWriteQueueMaxSize(128);
        in.drainHandler(x -> drains.incrementAndGet());

        List<Future<Void>> writes = new ArrayList<>();
        for (int i = 1; i <= 2000; i++) {
          writes.add(in.write(buf(i + ",value-" + i + "\n")));
        }
        return Future.all(new ArrayList<>(writes))
          .compose(x -> in.end())
          .compose(x -> in.completion());
      })
      .map(rows -> {
        ctx.assertEquals(2000, rows);
        return null;
      }));
  }

  @Test
  public void testSetWriteQueueMaxSizeIsClampedToAtLeastOne(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      .compose(in -> {
        in.setWriteQueueMaxSize(0);
        in.setWriteQueueMaxSize(-100);
        return in.write(buf("1,alpha\n")).compose(x -> in.end()).compose(x -> in.completion());
      })
      .map(rows -> {
        ctx.assertEquals(1, rows);
        return null;
      }));
  }

  @Test
  public void testCopyOutPauseFetchResume(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, 50)) TO STDOUT")
      .compose(out -> {
        Promise<String> promise = Promise.promise();
        StringBuilder sb = new StringBuilder();
        AtomicInteger chunks = new AtomicInteger();

        out.pause();
        out.handler(b -> {
          sb.append(b.toString(StandardCharsets.UTF_8));
          if (chunks.incrementAndGet() < 10) {
            out.fetch(1);
          } else {
            out.resume();
          }
        });
        out.endHandler(v -> promise.complete(sb.toString()));
        out.exceptionHandler(promise::tryFail);
        out.fetch(1);

        return promise.future().compose(text -> out.completion().map(rows -> {
          ctx.assertEquals(50, rows);
          StringBuilder expected = new StringBuilder();
          for (int i = 1; i <= 50; i++) {
            expected.append(i).append('\n');
          }
          ctx.assertEquals(expected.toString(), text);
          return (Void) null;
        }));
      }));
  }

  @Test
  public void testCopyOutAggregationThresholdProducesFewerChunks(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT generate_series(1, 500)) TO STDOUT",
        new PgCopyOutOptions().setAggregationThreshold(4096))
      .compose(out -> {
        AtomicInteger chunks = new AtomicInteger();
        Promise<String> promise = Promise.promise();
        StringBuilder sb = new StringBuilder();
        out.handler(b -> {
          chunks.incrementAndGet();
          sb.append(b.toString(StandardCharsets.UTF_8));
        });
        out.endHandler(v -> promise.complete(sb.toString()));
        out.exceptionHandler(promise::tryFail);
        return promise.future().map(text -> {
          StringBuilder expected = new StringBuilder();
          for (int i = 1; i <= 500; i++) {
            expected.append(i).append('\n');
          }
          ctx.assertEquals(expected.toString(), text);
          // 500 CopyData messages aggregated into a handful of buffers
          ctx.assertTrue(chunks.get() < 100, "expected aggregation, got " + chunks.get() + " chunks");
          return null;
        });
      }));
  }

  @Test
  public void testCopyOutExceptionHandlerSeesServerError(TestContext ctx) {
    run(ctx, conn -> conn.copyOut("COPY (SELECT 1 / (5 - i) FROM generate_series(1, 10) AS i) TO STDOUT")
      .compose(out -> {
        Promise<Throwable> promise = Promise.promise();
        out.handler(b -> {
        });
        out.exceptionHandler(promise::tryComplete);
        out.endHandler(v -> promise.tryFail("expected a division by zero"));
        return promise.future();
      }, err -> Future.succeededFuture(err))
      .map(err -> {
        ctx.assertTrue(err instanceof PgException, "was " + err);
        return null;
      })
      // the connection stays usable after a failed COPY OUT
      .compose(v -> conn.query("SELECT 1").execute())
      .mapEmpty());
  }

  @Test
  public void testCopyInServerErrorIsReportedAndConnectionSurvives(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.copyIn(copyIn()))
      // val is NOT NULL, so an empty value violates the constraint
      .compose(in -> in.write(buf("1,alpha\nnot-an-int,beta\n"))
        .compose(x -> in.end(), err -> Future.succeededFuture())
        .transform(ar -> in.completion())
        .otherwise(err -> {
          ctx.assertTrue(err instanceof PgException, "was " + err);
          return -1;
        }))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      }));
  }

  // ---------------------------------------------------------------- transactions

  @Test
  public void testCopyInsideCommittedTransaction(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.begin())
      .compose(tx -> conn.copyIn(copyIn())
        .compose(in -> in.write(buf("1,alpha\n")).compose(x -> in.end()))
        .compose(x -> tx.commit()))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(1, count);
        return null;
      }));
  }

  @Test
  public void testCopyInsideRolledBackTransaction(TestContext ctx) {
    run(ctx, conn -> table(conn)
      .compose(v -> conn.begin())
      .compose(tx -> conn.copyIn(copyIn())
        .compose(in -> in.write(buf("1,alpha\n")).compose(x -> in.end()))
        .compose(x -> tx.rollback()))
      .compose(v -> count(conn))
      .map(count -> {
        ctx.assertEquals(0, count);
        return null;
      }));
  }

  // ---------------------------------------------------------------- pooling

  @Test
  public void testPooledConnectionsRunCopiesConcurrently(TestContext ctx) {
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(4)).using(vertx));

    List<Future<String>> results = new ArrayList<>();
    for (int i = 1; i <= 8; i++) {
      int n = i;
      results.add(pool.withConnection(conn -> ((PgConnection) conn)
        .copyOut("COPY (SELECT generate_series(1, " + n + ")) TO STDOUT")
        .compose(this::drain)));
    }

    Future.all(new ArrayList<>(results))
      .map(composite -> {
        for (int i = 1; i <= 8; i++) {
          StringBuilder expected = new StringBuilder();
          for (int j = 1; j <= i; j++) {
            expected.append(j).append('\n');
          }
          ctx.assertEquals(expected.toString(), composite.<String>resultAt(i - 1));
        }
        return null;
      })
      .eventually(pool::close)
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testCopyOutAbandonedOnRecycleDoesNotWedgeThePool(TestContext ctx) {
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)).using(vertx));

    pool.getConnection()
      .compose(conn -> {
        PgConnection pg = (PgConnection) conn;
        // take the stream, consume nothing, hand the connection straight back
        return pg.copyOut("COPY (SELECT generate_series(1, 2000)) TO STDOUT")
          .compose(out -> {
            out.pause();
            return pg.close();
          });
      })
      // the single pooled connection must be usable again
      .compose(v -> pool.withConnection(conn -> conn.query("SELECT 42 AS n").execute()))
      .map(rs -> {
        ctx.assertEquals(42, rs.iterator().next().getInteger("n"));
        return null;
      })
      .eventually(pool::close)
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testDefaultThresholdEmitsOneBufferPerCopyDataMessage(TestContext ctx) {
    int rows = 500;
    run(ctx, conn -> conn.copyOut(
        "COPY (SELECT i, repeat('x', 40) FROM generate_series(1, " + rows + ") AS i) TO STDOUT WITH (FORMAT csv)",
        new PgCopyOutOptions().setAggregationThreshold(1))
      .compose(out -> {
        AtomicInteger chunks = new AtomicInteger();
        Promise<Void> promise = Promise.promise();
        out.handler(b -> chunks.incrementAndGet());
        out.endHandler(v -> promise.complete());
        out.exceptionHandler(promise::tryFail);
        return promise.future().map(v -> chunks.get());
      })
      .map(chunks -> {
        // PostgreSQL frames one CopyData message per row, and the default threshold of 1 hands
        // each of them to the application as its own buffer
        ctx.assertEquals(rows, chunks);
        return null;
      }));
  }

  @Test
  public void testConnectionLossDuringCopyInFailsCompletion(TestContext ctx) {
    // The guarantee that matters: whatever happens to an individual write, a COPY that did not
    // reach the server must be reported as failed by completion().
    Async async = ctx.async();
    PgConnection.connect(vertx, options).compose(victim ->
        victim.query("SELECT pg_backend_pid() AS pid").execute().compose(rs -> {
          int pid = rs.iterator().next().getInteger("pid");
          return victim.query("CREATE TEMP TABLE copy_loss (id INT, val TEXT)").execute()
            .compose(v -> victim.copyIn("COPY copy_loss FROM STDIN WITH (FORMAT csv)",
              new PgCopyInOptions()))
            .compose(in -> in.write(buf("1,alpha\n"))
              // kill the backend from another connection, then keep pushing data at a dead socket
              .compose(v -> PgConnection.connect(vertx, options)
                .compose(killer -> killer.query("SELECT pg_terminate_backend(" + pid + ")").execute()
                  .eventually(killer::close)))
              .compose(v -> {
                Future<Void> writes = Future.succeededFuture();
                for (int i = 0; i < 200; i++) {
                  int n = i;
                  writes = writes.transform(ar -> in.write(buf(n + ",value-" + n + "\n")));
                }
                return writes.transform(ar -> in.end()).transform(ar -> in.completion());
              })
              .transform(ar -> {
                ctx.assertTrue(ar.failed(), "completion() must fail when the connection dies");
                ctx.assertNotNull(ar.cause());
                return Future.succeededFuture();
              }));
        }).eventually(victim::close)
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  @Test
  public void testStatementThatNeverEntersCopyModeFailsRatherThanHangs(TestContext ctx) {
    // The direction keyword sits inside a literal, so the client lets it through, but the server
    // runs a COPY TO PROGRAM and never enters copy mode. There is no stream to hand out, and the
    // caller must be told instead of waiting forever.
    run(ctx, conn -> table(conn)
      .compose(v -> assertFailsWithType(ctx,
conn.copyOut("COPY copy_contract TO PROGRAM 'cat > /dev/null # TO STDOUT'"),
IllegalStateException.class))
      // and the connection is still usable afterwards
      .compose(v -> conn.query("SELECT 1").execute())
      .mapEmpty());
  }

  private static final String COPY_IN_SQL = "COPY copy_contract (id, val) FROM STDIN WITH (FORMAT csv)";

  private static String copyIn() {
    return COPY_IN_SQL;
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

  private static Future<Void> table(PgConnection conn) {
    return exec(conn, "CREATE TEMP TABLE copy_contract (id INT PRIMARY KEY, val TEXT NOT NULL)");
  }

  private static Future<Void> copyRow(PgConnection conn, String csv) {
    return conn.copyIn(COPY_IN_SQL)
      .compose(in -> in.write(buf(csv)).compose(v -> in.end()))
      .mapEmpty();
  }

  private static Future<Integer> count(PgConnection conn) {
    return conn.query("SELECT count(*) AS c FROM copy_contract").execute()
      .map(rs -> rs.iterator().next().getInteger("c"));
  }

  private static Future<List<String>> rows(PgConnection conn) {
    return conn.query("SELECT id, val FROM copy_contract ORDER BY id").execute()
      .map(rs -> {
        List<String> out = new ArrayList<>();
        for (Row row : rs) {
          out.add(row.getInteger("id") + ":" + row.getString("val"));
        }
        return out;
      });
  }

  private Future<String> drain(PgCopyOut out) {
    return collectBytes(out).map(b -> b.toString(StandardCharsets.UTF_8));
  }

  private Future<String> drain(PgCopyOut out, AtomicBoolean endHandlerFired) {
    Promise<Buffer> promise = Promise.promise();
    Buffer acc = Buffer.buffer();
    out.handler(acc::appendBuffer);
    out.endHandler(v -> {
      endHandlerFired.set(true);
      promise.tryComplete(acc);
    });
    out.exceptionHandler(promise::tryFail);
    return promise.future().map(b -> b.toString(StandardCharsets.UTF_8));
  }

  private Future<Buffer> collectBytes(PgCopyOut out) {
    Promise<Buffer> promise = Promise.promise();
    Buffer acc = Buffer.buffer();
    out.handler(acc::appendBuffer);
    out.endHandler(v -> promise.tryComplete(acc));
    out.exceptionHandler(promise::tryFail);
    return promise.future();
  }

  private static <T> Future<Void> assertFailsWithType(TestContext ctx, Future<T> future, Class<? extends Throwable> type) {
    return future.compose(
      v -> Future.failedFuture("expected a " + type.getSimpleName()),
      err -> {
        ctx.assertTrue(type.isInstance(err), "expected " + type.getSimpleName() + " but got " + err);
        return Future.succeededFuture();
      });
  }

  private void assertCopyInCsv(TestContext ctx, PgCopyInOptions options, List<String> expected) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)", options))
      .compose(in -> {
        Future<Void> w1 = in.write(buf("1,alpha\n"));
        Future<Void> w2 = in.write(buf("2,beta\n"));
        Future<Void> w3 = in.write(buf("3,gamma\n"));

        Future<Void> completion = in.end();
        return Future.all(w1, w2, w3)
          .compose(v -> completion)
          .compose(v -> in.completion())
          .map(rowCount -> {
            ctx.assertEquals(expected.size(), rowCount);
            return null;
          });
      })
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(expected, rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  private void assertCopyInSequentialComposedWrites(TestContext ctx) {
    Async async = ctx.async();

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn(
        "COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)",
        new PgCopyInOptions()
          .setChunkSize(1024 * 1024)))
      .compose(in -> in.write(buf("1,alpha\n"))
        .compose(v -> in.write(buf("2,beta\n")))
        .compose(v -> in.write(buf("3,gamma\n")))
        .compose(v -> in.end()))
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(Arrays.asList("1:alpha", "2:beta", "3:gamma"), rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  private void assertCopyInCsvSingleLargeBuffer(TestContext ctx, PgCopyInOptions options) {
    Async async = ctx.async();

    StringBuilder payload = new StringBuilder();
    List<String> expected = new ArrayList<>();
    for (int i = 1; i <= 128; i++) {
      payload.append(i).append(",value-").append(i).append('\n');
      expected.add(i + ":value-" + i);
    }

    withConnection(conn -> createTextTable(conn, "copy_test")
      .compose(v -> conn.copyIn("COPY copy_test (id, val) FROM STDIN WITH (FORMAT csv)", options))
      .compose(in -> {
        Future<Void> write = in.write(buf(payload.toString()));
        Future<Void> completion = in.end();
        return write.compose(v -> completion)
          .compose(v -> in.completion())
          .map(rowCount -> {
            ctx.assertEquals(expected.size(), rowCount);
            return null;
          });
      })
      .compose(v -> fetchTextRows(conn, "copy_test"))
      .map(rows -> {
        ctx.assertEquals(expected, rows);
        return null;
      })
    ).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  private void assertUnexpectedCopyQueryClosesConnection(TestContext ctx, String sql) {
    Async async = ctx.async();

    PgConnection.connect(vertx, options).compose(conn -> {
      Promise<Void> closed = Promise.promise();
      AtomicReference<Throwable> exception = new AtomicReference<>();
      conn.exceptionHandler(exception::set);
      conn.closeHandler(v -> closed.tryComplete());

      return conn.query(sql).execute()
        .compose(v -> Future.failedFuture("plain query unexpectedly handled COPY protocol"), err -> Future.succeededFuture())
        .compose(v -> closed.future())
        .map(v -> {
          ctx.assertNotNull(exception.get());
          return null;
        });
    }).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
  }

  private Future<Void> unexpectedCopyQuery(PgConnection conn, String sql) {
    Promise<Void> closed = Promise.promise();
    AtomicReference<Throwable> exception = new AtomicReference<>();
    conn.exceptionHandler(exception::set);
    conn.closeHandler(v -> closed.tryComplete());

    return conn.query(sql).execute()
      .compose(v -> Future.failedFuture("plain query unexpectedly handled COPY protocol"), err -> Future.succeededFuture())
      .compose(v -> closed.future())
      .map(v -> {
        if (exception.get() == null) {
          throw new AssertionError("Expected protocol exception");
        }
        return null;
      });
  }

  private <T> Future<T> withConnection(Function<PgConnection, Future<T>> fn) {
    Promise<T> promise = Promise.promise();
    PgConnection.connect(vertx, options).onComplete(ar -> {
      if (ar.failed()) {
        promise.fail(ar.cause());
        return;
      }
      PgConnection conn = ar.result();
      Future<T> op;
      try {
        op = fn.apply(conn);
      } catch (Throwable t) {
        op = Future.failedFuture(t);
      }
      op.eventually(conn::close).onComplete(promise);
    });
    return promise.future();
  }

  private <T> Future<T> withTwoConnections(BiFunction<PgConnection, PgConnection, Future<T>> fn) {
    Promise<T> promise = Promise.promise();

    PgConnection.connect(vertx, options).onComplete(srcAr -> {
      if (srcAr.failed()) {
        promise.fail(srcAr.cause());
        return;
      }

      PgConnection src = srcAr.result();
      PgConnection.connect(vertx, options).onComplete(dstAr -> {
        if (dstAr.failed()) {
          src.close().onComplete(v -> promise.fail(dstAr.cause()));
          return;
        }

        PgConnection dst = dstAr.result();
        Future<T> op;
        try {
          op = fn.apply(src, dst);
        } catch (Throwable t) {
          op = Future.failedFuture(t);
        }
        op.eventually(() -> src.close().eventually(dst::close)).onComplete(promise);
      });
    });

    return promise.future();
  }

  private static Future<Void> exec(PgConnection conn, String sql) {
    return conn.query(sql).execute().mapEmpty();
  }

  private static Buffer buf(String value) {
    return Buffer.buffer(value, StandardCharsets.UTF_8.name());
  }

  private static Future<Void> expectFailure(Future<?> future, String message) {
    return future.compose(v -> Future.failedFuture(message), err -> Future.succeededFuture());
  }

  private static <T> Future<Void> assertFailsWith(TestContext ctx, Class<? extends Throwable> expectedType, Future<T> future) {
    return future.compose(
      v -> Future.failedFuture("Expected " + expectedType.getName()),
      err -> {
        ctx.assertTrue(expectedType.isInstance(err), "Expected " + expectedType.getName() + " but got " + err);
        return Future.succeededFuture();
      });
  }

  private static void assertThrows(TestContext ctx, Class<? extends Throwable> expectedType, Runnable action) {
    try {
      action.run();
      ctx.fail("Expected " + expectedType.getName());
    } catch (Throwable t) {
      ctx.assertTrue(expectedType.isInstance(t));
    }
  }

  private static Future<Void> createTextTable(PgConnection conn, String table) {
    return exec(conn, "CREATE TEMP TABLE " + table + " (id INT PRIMARY KEY, val TEXT NOT NULL)");
  }

  private static Future<Void> insertTextRows(PgConnection conn, String table) {
    return exec(conn, "INSERT INTO " + table + " (id, val) VALUES (1, 'alpha'), (2, 'beta'), (3, 'gamma')");
  }

  private static Future<Integer> fetchCount(PgConnection conn, String table) {
    return conn.query("SELECT count(*) AS c FROM " + table)
      .execute()
      .map(rows -> rows.iterator().next().getInteger("c"));
  }

  private static ChannelConfig channelConfig(PgConnection conn) {
    PgConnectionImpl connection = (PgConnectionImpl) conn;
    PgSocketConnection socket = (PgSocketConnection) connection.unwrap().unwrap();
    return socket.socket().channelHandlerContext().channel().config();
  }

  private static void assertWatermark(TestContext ctx, WriteBufferWaterMark watermark, int low, int high) {
    ctx.assertEquals(low, watermark.low());
    ctx.assertEquals(high, watermark.high());
  }

  private static Future<List<String>> fetchTextRows(PgConnection conn, String table) {
    return conn.query("SELECT id, val FROM " + table + " ORDER BY id")
      .execute()
      .map(rows -> {
        List<String> got = new ArrayList<>();
        for (Row row : rows) {
          got.add(row.getInteger("id") + ":" + row.getString("val"));
        }
        return got;
      });
  }

  private static Future<String> collect(PgCopyOut out) {
    Promise<String> promise = Promise.promise();
    StringBuilder sb = new StringBuilder();

    out.pause();
    out.handler(buffer -> sb.append(buffer.toString(StandardCharsets.UTF_8)));
    out.exceptionHandler(promise::tryFail);
    out.endHandler(v -> promise.tryComplete(sb.toString()));
    out.completion().onFailure(promise::tryFail);
    out.resume();

    return promise.future();
  }

  private static Future<String> collectFlowing(PgCopyOut out) {
    Promise<String> promise = Promise.promise();
    StringBuilder sb = new StringBuilder();

    out.handler(buffer -> sb.append(buffer.toString(StandardCharsets.UTF_8)));
    out.exceptionHandler(promise::tryFail);
    out.endHandler(v -> promise.tryComplete(sb.toString()));
    out.completion().onFailure(promise::tryFail);

    return promise.future();
  }

  private static String normalize(String value) {
    return value.replace("\r\n", "\n");
  }


}
