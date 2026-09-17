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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.core.json.JsonObject;
import io.vertx.core.streams.ReadStream;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgCopyInOptions;
import io.vertx.pgclient.PgCopyOutOptions;
import io.vertx.pgclient.impl.CopyInStreamImpl;
import io.vertx.pgclient.impl.CopyInStreamInternal;
import io.vertx.pgclient.impl.CopyOutStreamImpl;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.fail;

/**
 * Unit tests for the COPY streams and their options, with no database involved.
 */
@RunWith(VertxUnitRunner.class)
public class CopyStreamTest {


  private Vertx vertx;

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testSequentialComposedWritesProgress(TestContext ctx) {
    assertSequentialComposedWrites(ctx);
  }

  @Test
  public void testSameTurnWritesCoalesceAtChunkBoundaryWithoutLosingOversizedWrite(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(4);
      TestSink sink = new TestSink();
      completeFromDone(stream, sink, 3);

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Future<Void> w1 = stream.write(buffer("ab"));
        Future<Void> w2 = stream.write(buffer("cd"));
        Future<Void> w3 = stream.write(buffer("efghij"));
        Future<Void> end = stream.end();

        Future.all(w1, w2, w3).compose(x -> end).onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(Arrays.asList("data:abcd", "data:efghij", "done"), sink.events);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testEmptyCoalescingWriteIsNoOpAndDoesNotRetainBuffer(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      completeFromDone(stream, sink, 0);

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Buffer empty = Buffer.buffer();
        ByteBuf byteBuf = ((BufferInternal) empty).getByteBuf();
        int refCnt = byteBuf.refCnt();
        Future<Void> write = stream.write(empty);
        Future<Void> end = stream.end();

        write.compose(x -> end).onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(refCnt, byteBuf.refCnt());
          ctx.assertEquals(Arrays.asList("done"), sink.events);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testUnwritableSinkKeepsDataBeforeCopyDoneAndFiresSingleDrain(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(64);
      TestSink sink = new TestSink();
      sink.writable = false;
      AtomicInteger drains = new AtomicInteger();
      completeFromDone(stream, sink, 1);

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        stream.setWriteQueueMaxSize(1);
        stream.drainHandler(x -> drains.incrementAndGet());
        Future<Void> write = stream.write(buffer("x"));
        Future<Void> end = stream.end();

        vertx.runOnContext(next -> {
          ctx.assertTrue(stream.writeQueueFull());
          ctx.assertFalse(write.isComplete());
          ctx.assertFalse(end.isComplete());
          ctx.assertTrue(sink.events.isEmpty());

          sink.makeWritable();
          end.onComplete(ctx.asyncAssertSuccess(x -> {
            ctx.assertEquals(Arrays.asList("data:x", "done"), sink.events);
            ctx.assertEquals(1, drains.get());
            ctx.assertFalse(stream.writeQueueFull());
            ctx.assertTrue(write.succeeded());
            async.complete();
          }));
        });
      }));
    });
  }

  @Test
  public void testAbortReleasesBufferedDataAndRejectsFurtherOperations(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      sink.writable = false;
      stream.exceptionHandler(t -> { });

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Buffer data = buffer("buffered");
        ByteBuf byteBuf = ((BufferInternal) data).getByteBuf();
        int refCnt = byteBuf.refCnt();
        Future<Void> write = stream.write(data);

        // PostgreSQL answers CopyFail with an error, and that error is the outcome of the COPY
        IllegalStateException serverError = new IllegalStateException("COPY from stdin failed: rollback");

        stream.abort("rollback").compose(x -> {
          // aborting stops the stream, but the outcome is still the server's to report
          ctx.assertFalse(stream.completion().isComplete());
          stream.failFromServer(serverError);
          return Future.all(
            expectFailure(write),
            expectFailure(stream.completion()),
            expectFailure(stream.write(buffer("late"))),
            expectFailure(stream.abort("again")));
        }).onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(serverError, stream.completion().cause());
          ctx.assertEquals(refCnt, byteBuf.refCnt());
          ctx.assertEquals(Arrays.asList("fail:rollback"), sink.events);
          ctx.assertEquals(1, sink.copyFailCalls);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testServerFailureReleasesUnwrittenDataWithoutSendingCopyFail(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      sink.writable = false;
      stream.exceptionHandler(t -> { });

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Buffer data = buffer("buffered");
        ByteBuf byteBuf = ((BufferInternal) data).getByteBuf();
        int refCnt = byteBuf.refCnt();
        Future<Void> write = stream.write(data);
        stream.failFromServer(new IllegalStateException("server rejected COPY"));

        Future.all(expectFailure(write), expectFailure(stream.completion())).onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(refCnt, byteBuf.refCnt());
          ctx.assertTrue(sink.events.isEmpty());
          ctx.assertEquals(0, sink.copyFailCalls);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testEndIsIdempotentAndCopyDoneIsLast(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      completeFromDone(stream, sink, 0);

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Future<Void> end1 = stream.end();
        Future<Void> end2 = stream.end();

        Future.all(end1, end2).compose(x -> Future.all(
          expectFailure(stream.write(buffer("late"))),
          expectFailure(stream.abort("late")))
        ).onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(Arrays.asList("done"), sink.events);
          ctx.assertEquals(1, sink.copyDoneCalls);
          ctx.assertEquals(0, sink.copyFailCalls);
          async.complete();
        }));
      }));
    });
  }

  @Test
  public void testSynchronousSinkFailureReleasesFrame(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      sink.throwOnWrite = new IllegalStateException("write threw");
      stream.exceptionHandler(t -> { });

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> {
        Buffer data = buffer("payload");
        ByteBuf byteBuf = ((BufferInternal) data).getByteBuf();
        int refCnt = byteBuf.refCnt();

        Future.all(expectFailure(stream.write(data)), expectFailure(stream.completion()))
          .onComplete(ctx.asyncAssertSuccess(x -> {
            ctx.assertEquals(refCnt, byteBuf.refCnt());
            ctx.assertEquals(Arrays.asList("fail:write threw"), sink.events);
            ctx.assertEquals(1, sink.copyFailCalls);
            async.complete();
          }));
      }));
    });
  }

  private void assertSequentialComposedWrites(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(ignored -> {
      CopyInStreamImpl stream = coalescing(1024);
      TestSink sink = new TestSink();
      completeFromDone(stream, sink, 3);

      attach(stream, sink).onComplete(ctx.asyncAssertSuccess(v -> stream.write(buffer("a"))
        .compose(x -> stream.write(buffer("b")))
        .compose(x -> stream.write(buffer("c")))
        .compose(x -> stream.end())
        .onComplete(ctx.asyncAssertSuccess(x -> {
          ctx.assertEquals(Arrays.asList("data:a", "data:b", "data:c", "done"), sink.events);
          async.complete();
        }))));
    });
  }

  private CopyInStreamImpl coalescing(int chunkSize) {
    return new CopyInStreamImpl(currentContext(), new PgCopyInOptions()
      .setChunkSize(chunkSize));
  }

  private static ContextInternal currentContext() {
    return (ContextInternal) Vertx.currentContext();
  }

  private static Future<?> attach(CopyInStreamInternal stream, TestSink sink) {
    stream.attachSink(sink);
    return stream.readyFuture();
  }

  private static void completeFromDone(CopyInStreamInternal stream, TestSink sink, int rows) {
    sink.doneHandler = () -> stream.completeFromServer(rows);
  }

  private static Future<Void> expectFailure(Future<?> future) {
    return future.compose(v -> Future.failedFuture("expected failure"), err -> Future.succeededFuture());
  }

  private static Buffer buffer(String value) {
    return Buffer.buffer(value, StandardCharsets.UTF_8.name());
  }

  private static final class TestSink implements CopyInStreamInternal.Sink {

    final List<String> events = new ArrayList<>();

    boolean writable = true;
    RuntimeException throwOnWrite;
    Runnable writableHandler;
    Runnable doneHandler;
    int copyDoneCalls;
    int copyFailCalls;
    int detachCalls;
    int watermark;
    Integer lastFrameComponents;
    String lastCopyFailMessage;

    @Override
    public boolean isWritable() {
      return writable;
    }

    @Override
    public void onWritable(Runnable cb) {
      writableHandler = cb;
    }

    @Override
    public void setWatermarks(int maxBytes) {
      watermark = maxBytes;
    }

    @Override
    public void writeCopyData(ByteBuf buf) {
      if (throwOnWrite != null) {
        throw throwOnWrite;
      }
      lastFrameComponents = buf instanceof CompositeByteBuf ? ((CompositeByteBuf) buf).numComponents() : 1;
      record(buf);
    }

    @Override
    public void writeCopyDone() {
      copyDoneCalls++;
      events.add("done");
      if (doneHandler != null) {
        doneHandler.run();
      }
    }

    @Override
    public void writeCopyFail(String message) {
      copyFailCalls++;
      lastCopyFailMessage = message;
      events.add("fail:" + message);
    }

    @Override
    public void detach() {
      detachCalls++;
    }

    void makeWritable() {
      writable = true;
      if (writableHandler != null) {
        writableHandler.run();
      }
    }

    private void record(ByteBuf buf) {
      byte[] bytes = new byte[buf.readableBytes()];
      buf.getBytes(buf.readerIndex(), bytes);
      events.add("data:" + new String(bytes, StandardCharsets.UTF_8));
      buf.release();
    }
  }

  // ---------------------------------------------------------------- COPY OUT

  @Test
  public void testReentrantFetchDrainsIteratively(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);

      out.pause();
      ctx.assertEquals(1, upstream.pauseCalls);
      for (int i = 0; i < 16; i++) {
        out.emit(Buffer.buffer(Integer.toString(i)));
      }

      ctx.assertEquals(1, upstream.pauseCalls);
      ctx.assertEquals(0, upstream.resumeCalls);

      AtomicInteger delivered = new AtomicInteger();
      AtomicInteger ended = new AtomicInteger();
      AtomicInteger handlerDepth = new AtomicInteger();
      AtomicInteger maxHandlerDepth = new AtomicInteger();
      out.handler(buffer -> {
        int depth = handlerDepth.incrementAndGet();
        maxHandlerDepth.accumulateAndGet(depth, Math::max);
        if (delivered.incrementAndGet() < 16) {
          out.fetch(1);
        }
        handlerDepth.decrementAndGet();
      });
      out.endHandler(ignored -> ended.incrementAndGet());
      out.end(16);
      // CommandComplete ends the stream, ReadyForQuery resolves completion()
      ctx.assertFalse(out.completion().succeeded());
      out.commandCompleted();
      ctx.assertEquals(0, ended.get());
      out.fetch(1);

      vertx.runOnContext(ignored -> {
        ctx.assertTrue(out.completion().succeeded());
        ctx.assertEquals(16, delivered.get());
        ctx.assertEquals(1, ended.get());
        ctx.assertEquals(1, maxHandlerDepth.get());
        ctx.assertEquals(1, upstream.pauseCalls);
        ctx.assertEquals(1, upstream.resumeCalls);
        async.complete();
      });
    });
  }

  @Test
  public void testDiscardReleasesPausedUpstream(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);

      out.emit(Buffer.buffer("data"));
      ctx.assertEquals(1, upstream.pauseCalls);

      out.discard(new IllegalStateException("discarded"));

      ctx.assertEquals(1, upstream.resumeCalls);
      ctx.assertTrue(out.completion().failed());
      async.complete();
    });
  }

  @Test
  public void testInitiallyFlowingHandlerDoesNotTouchUpstream(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);
      AtomicInteger delivered = new AtomicInteger();

      out.handler(buffer -> delivered.incrementAndGet());
      out.emit(Buffer.buffer("a"));
      out.emit(Buffer.buffer("b"));

      ctx.assertEquals(2, delivered.get());
      ctx.assertEquals(0, upstream.pauseCalls);
      ctx.assertEquals(0, upstream.resumeCalls);
      async.complete();
    });
  }

  @Test
  public void testQueuedDataPausesUpstreamImmediately(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);
      AtomicInteger delivered = new AtomicInteger();

      out.emit(Buffer.buffer("a"));
      out.emit(Buffer.buffer("b")); // Simulates an event already decoded when pause took effect.

      ctx.assertEquals(1, upstream.pauseCalls);
      out.handler(buffer -> delivered.incrementAndGet());
      ctx.assertEquals(2, delivered.get());
      ctx.assertEquals(1, upstream.resumeCalls);
      async.complete();
    });
  }

  @Test
  public void testExhaustedDemandPausesUpstreamImmediately(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);
      AtomicInteger delivered = new AtomicInteger();

      out.handler(buffer -> delivered.incrementAndGet());
      out.pause();
      out.fetch(1);

      ctx.assertEquals(1, upstream.pauseCalls);
      ctx.assertEquals(1, upstream.resumeCalls);
      out.emit(Buffer.buffer("a"));
      ctx.assertEquals(1, delivered.get());
      ctx.assertEquals(2, upstream.pauseCalls);
      async.complete();
    });
  }

  @Test
  public void testEndWaitsForQueuedDataAndIsEmittedOnce(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);
      AtomicInteger delivered = new AtomicInteger();
      AtomicInteger ended = new AtomicInteger();

      out.pause();
      out.handler(buffer -> delivered.incrementAndGet());
      out.endHandler(ignored -> ended.incrementAndGet());
      out.emit(Buffer.buffer("a"));
      out.emit(Buffer.buffer("b"));
      out.end(2);
      out.commandCompleted();

      ctx.assertEquals(0, ended.get());
      out.fetch(1);
      ctx.assertEquals(1, delivered.get());
      ctx.assertEquals(0, ended.get());
      out.fetch(1);
      out.resume();
      out.fetch(1);
      out.endHandler(ignored -> ended.incrementAndGet());

      vertx.runOnContext(ignored -> {
        ctx.assertTrue(out.completion().succeeded());
        ctx.assertEquals(2, delivered.get());
        ctx.assertEquals(1, ended.get());
        async.complete();
      });
    });
  }

  @Test
  public void testHandlerCanClearItselfDuringDrain(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl((ContextInternal) Vertx.currentContext(), upstream);
      AtomicInteger delivered = new AtomicInteger();

      out.emit(Buffer.buffer("a"));
      out.emit(Buffer.buffer("b"));
      out.emit(Buffer.buffer("c"));
      out.handler(buffer -> {
        delivered.incrementAndGet();
        out.handler(null);
      });

      ctx.assertEquals(1, delivered.get());
      out.handler(buffer -> delivered.incrementAndGet());
      ctx.assertEquals(3, delivered.get());
      async.complete();
    });
  }

  // ---------------------------------------------------------------- edge cases

  @Test
  public void testExceptionHandlerRegisteredAfterFailureIsStillNotified(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      RuntimeException boom = new RuntimeException("boom");
      out.fail(boom);

      // handler installed after the failure already happened
      out.exceptionHandler(err -> {
        ctx.assertEquals(boom, err);
        async.complete();
      });
    });
  }

  @Test
  public void testFetchRejectsNegativeAmount(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      try {
        out.fetch(-1);
        ctx.fail("expected an IllegalArgumentException");
      } catch (IllegalArgumentException expected) {
      }
      // zero is a legal no-op
      out.fetch(0);
      async.complete();
    });
  }

  @Test
  public void testEmitAfterEndIsIgnored(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      AtomicInteger delivered = new AtomicInteger();
      out.handler(b -> delivered.incrementAndGet());
      out.end(0);
      out.emit(Buffer.buffer("late"));

      vertx.runOnContext(x -> {
        ctx.assertEquals(0, delivered.get());
        async.complete();
      });
    });
  }

  @Test
  public void testEmitOfNullIsIgnored(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      AtomicInteger delivered = new AtomicInteger();
      out.handler(b -> delivered.incrementAndGet());
      out.emit(null);

      vertx.runOnContext(x -> {
        ctx.assertEquals(0, delivered.get());
        async.complete();
      });
    });
  }

  @Test
  public void testFirstFailureWins(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      RuntimeException first = new RuntimeException("first");
      out.fail(first);
      out.fail(new RuntimeException("second"));

      out.completion().onComplete(ar -> {
        ctx.assertTrue(ar.failed());
        ctx.assertEquals(first, ar.cause());
        async.complete();
      });
    });
  }

  @Test
  public void testFailureAfterTheDataDrainedStillSettlesCompletion(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      out.handler(b -> {
      });
      // CommandComplete ends the stream, the connection can still die before ReadyForQuery
      out.end(3);
      ctx.assertFalse(out.completion().isComplete());

      RuntimeException boom = new RuntimeException("connection closed");
      out.fail(boom);

      out.completion().onComplete(ar -> {
        ctx.assertTrue(ar.failed());
        ctx.assertEquals(boom, ar.cause());
        async.complete();
      });
    });
  }

  @Test
  public void testDiscardIsIdempotentAndFailsCompletion(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl(currentContext(), upstream);
      out.emit(Buffer.buffer("queued"));

      out.discard(new IllegalStateException("first discard"));
      out.discard(new IllegalStateException("second discard"));

      ctx.assertTrue(out.isDiscarding());
      ctx.assertTrue(out.completion().failed());
      ctx.assertEquals("first discard", out.completion().cause().getMessage());
      // the upstream is released exactly once
      ctx.assertEquals(1, upstream.resumeCalls);
      async.complete();
    });
  }

  @Test
  public void testReadyFutureCompletesWhenServerEntersCopyMode(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      ctx.assertFalse(out.readyFuture().isComplete());
      out.readyFromServer();
      out.readyFuture().onComplete(ctx.asyncAssertSuccess(stream -> {
        ctx.assertEquals(out, stream);
        async.complete();
      }));
    });
  }

  @Test
  public void testCompletionCarriesTheRowCountFromEnd(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyOutStreamImpl out = copyOut();
      out.handler(b -> {
      });
      out.end(7);
      ctx.assertFalse(out.completion().isComplete());
      out.commandCompleted();
      ctx.assertTrue(out.completion().succeeded());
      ctx.assertEquals(7, out.completion().result());
      async.complete();
    });
  }

  @Test
  public void testEndedStreamNeverPausesUpstreamAgain(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      TestReadStream upstream = new TestReadStream();
      CopyOutStreamImpl out = new CopyOutStreamImpl(currentContext(), upstream);
      out.handler(b -> {
      });
      out.end(0);

      int pausesAfterEnd = upstream.pauseCalls;
      out.pause();
      out.fetch(0);
      ctx.assertEquals(pausesAfterEnd, upstream.pauseCalls);
      async.complete();
    });
  }

  @Test
  public void testAbortBeforeTheSinkIsAttachedSendsCopyFailOnAttach(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      TestSink sink = new TestSink();

      in.abort("early abort").onComplete(ctx.asyncAssertSuccess(x -> {
        in.attachSink(sink);
        vertx.runOnContext(y -> {
          ctx.assertEquals(1, sink.copyFailCalls);
          ctx.assertEquals("early abort", sink.lastCopyFailMessage);
          // the sink is released again straight away, the copy never starts
          ctx.assertEquals(1, sink.detachCalls);
          ctx.assertTrue(in.readyFuture().failed());
          async.complete();
        });
      }));
    });
  }

  @Test
  public void testAbortAfterEndIsRejected(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      TestSink sink = new TestSink();
      in.attachSink(sink);

      in.readyFuture().onComplete(ctx.asyncAssertSuccess(x -> {
        in.end();
        in.abort("too late").onComplete(ar -> {
          ctx.assertTrue(ar.failed());
          ctx.assertTrue(ar.cause() instanceof IllegalStateException, "was " + ar.cause());
          // no CopyFail was sent, the copy is already finishing
          ctx.assertEquals(0, sink.copyFailCalls);
          async.complete();
        });
      }));
    });
  }

  @Test
  public void testWriteQueueMaxSizeIsClampedAndReachesTheSink(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      TestSink sink = new TestSink();
      in.attachSink(sink);

      in.readyFuture().onComplete(ctx.asyncAssertSuccess(x -> {
        in.setWriteQueueMaxSize(4096);
        ctx.assertEquals(4096, sink.watermark);

        // anything below one byte is clamped rather than rejected
        in.setWriteQueueMaxSize(0);
        ctx.assertEquals(1, sink.watermark);
        in.setWriteQueueMaxSize(-99);
        ctx.assertEquals(1, sink.watermark);
        async.complete();
      }));
    });
  }

  @Test
  public void testDetachSinkWithoutASinkIsANoOp(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      in.detachSinkIfAny();
      vertx.runOnContext(x -> {
        ctx.assertFalse(in.completion().isComplete());
        async.complete();
      });
    });
  }

  @Test
  public void testCompleteFromServerResolvesCompletion(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      in.completeFromServer(12);
      in.completion().onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(12, rows);
        async.complete();
      }));
    });
  }

  @Test
  public void testWriteOfNullIsRejected(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      TestSink sink = new TestSink();
      in.attachSink(sink);

      in.readyFuture().onComplete(ctx.asyncAssertSuccess(x -> in.write(null).onComplete(ar -> {
        ctx.assertTrue(ar.failed());
        ctx.assertTrue(ar.cause() instanceof NullPointerException, "was " + ar.cause());
        async.complete();
      })));
    });
  }

  @Test
  public void testServerFailureAfterAbortReplacesTheCompletionCause(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = copyIn();
      TestSink sink = new TestSink();
      in.attachSink(sink);

      in.readyFuture().onComplete(ctx.asyncAssertSuccess(x -> in.abort("client abort").onComplete(ar -> {
        // abort alone does not decide the outcome
        ctx.assertFalse(in.completion().isComplete());

        RuntimeException serverError = new RuntimeException("server said no");
        in.failFromServer(serverError);

        in.completion().onComplete(done -> {
          ctx.assertTrue(done.failed());
          ctx.assertEquals(serverError, done.cause());
          async.complete();
        });
      })));
    });
  }

  @Test
  public void testCoalescingKeepsSmallWritesUncopied(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      CopyInStreamImpl in = new CopyInStreamImpl(currentContext(), new PgCopyInOptions()
        .setChunkSize(1024 * 1024));
      TestSink sink = new TestSink();
      in.attachSink(sink);

      in.readyFuture().onComplete(ctx.asyncAssertSuccess(x -> {
        int writes = 200;
        for (int i = 0; i < writes; i++) {
          in.write(Buffer.buffer("row-" + i + "\n"));
        }

        vertx.runOnContext(y -> {
          // Netty consolidates a CompositeByteBuf once it grows past its component limit, and
          // consolidating copies every byte accumulated so far. The default limit is 16, which
          // would turn coalescing many small writes into repeated whole chunk copies. Seeing one
          // component per write proves the payload was assembled without copying.
          ctx.assertNotNull(sink.lastFrameComponents, "no CopyData frame reached the sink");
          ctx.assertEquals(writes, sink.lastFrameComponents,
            "the chunk was consolidated, so small writes are being copied repeatedly");
          in.abort("done").onComplete(z -> async.complete());
        });
      }));
    });
  }

  // ---------------------------------------------------------------- options

  @Test
  public void testCopyInDefaults() {
    PgCopyInOptions options = new PgCopyInOptions();
    assertEquals(PgCopyInOptions.DEFAULT_CHUNK_SIZE, options.getChunkSize());
    assertEquals(256 * 1024, options.getChunkSize());
  }

  @Test
  public void testCopyInSettersAreFluentAndStick() {
    PgCopyInOptions options = new PgCopyInOptions();
    assertEquals(options, options.setChunkSize(4096));

    assertEquals(4096, options.getChunkSize());
  }

  @Test
  public void testCopyInCopyConstructor() {
    PgCopyInOptions original = new PgCopyInOptions()
      .setChunkSize(1234);

    PgCopyInOptions copy = new PgCopyInOptions(original);
    assertNotSame(original, copy);
    assertEquals(original.getChunkSize(), copy.getChunkSize());

    // the copy is independent
    copy.setChunkSize(9999);
    assertEquals(1234, original.getChunkSize());
  }

  @Test
  public void testCopyInJsonRoundTrip() {
    PgCopyInOptions original = new PgCopyInOptions()
      .setChunkSize(8192);

    JsonObject json = original.toJson();
    assertEquals(8192, (int) json.getInteger("chunkSize"));

    PgCopyInOptions restored = new PgCopyInOptions(json);
    assertEquals(original.getChunkSize(), restored.getChunkSize());
  }

  @Test
  public void testCopyInFromEmptyJsonKeepsDefaults() {
    PgCopyInOptions options = new PgCopyInOptions(new JsonObject());
    assertEquals(PgCopyInOptions.DEFAULT_CHUNK_SIZE, options.getChunkSize());
  }

  @Test
  public void testCopyInFromJsonWithOnlyChunkSize() {
    PgCopyInOptions options = new PgCopyInOptions(new JsonObject().put("chunkSize", 4096));
    assertEquals(4096, options.getChunkSize());
  }

  @Test
  public void testCopyInRejectsInvalidChunkSize() {
    expectIllegalArgument(() -> new PgCopyInOptions().setChunkSize(0));
    expectIllegalArgument(() -> new PgCopyInOptions().setChunkSize(-1));
    expectIllegalArgument(() -> new PgCopyInOptions().setChunkSize(Integer.MIN_VALUE));
    // one byte is degenerate but legal
    assertEquals(1, new PgCopyInOptions().setChunkSize(1).getChunkSize());
  }

  @Test
  public void testCopyOutDefaults() {
    PgCopyOutOptions options = new PgCopyOutOptions();
    assertEquals(PgCopyOutOptions.DEFAULT_AGGREGATION_THRESHOLD, options.getAggregationThreshold());
    assertEquals(1, options.getAggregationThreshold());
  }

  @Test
  public void testCopyOutSetterIsFluentAndSticks() {
    PgCopyOutOptions options = new PgCopyOutOptions();
    assertEquals(options, options.setAggregationThreshold(64 * 1024));
    assertEquals(64 * 1024, options.getAggregationThreshold());
  }

  @Test
  public void testCopyOutCopyConstructor() {
    PgCopyOutOptions original = new PgCopyOutOptions().setAggregationThreshold(4096);
    PgCopyOutOptions copy = new PgCopyOutOptions(original);
    assertNotSame(original, copy);
    assertEquals(4096, copy.getAggregationThreshold());

    copy.setAggregationThreshold(1);
    assertEquals(4096, original.getAggregationThreshold());
  }

  @Test
  public void testCopyOutJsonRoundTrip() {
    PgCopyOutOptions original = new PgCopyOutOptions().setAggregationThreshold(2048);
    JsonObject json = original.toJson();
    assertEquals(2048, (int) json.getInteger("aggregationThreshold"));

    PgCopyOutOptions restored = new PgCopyOutOptions(json);
    assertEquals(original.getAggregationThreshold(), restored.getAggregationThreshold());
  }

  @Test
  public void testCopyOutFromEmptyJsonKeepsDefaults() {
    assertEquals(PgCopyOutOptions.DEFAULT_AGGREGATION_THRESHOLD,
      new PgCopyOutOptions(new JsonObject()).getAggregationThreshold());
  }

  @Test
  public void testCopyOutRejectsInvalidThreshold() {
    expectIllegalArgument(() -> new PgCopyOutOptions().setAggregationThreshold(0));
    expectIllegalArgument(() -> new PgCopyOutOptions().setAggregationThreshold(-1));
    assertEquals(1, new PgCopyOutOptions().setAggregationThreshold(1).getAggregationThreshold());
  }

  private static void expectIllegalArgument(Runnable action) {
    try {
      action.run();
      fail("expected an IllegalArgumentException");
    } catch (IllegalArgumentException expected) {
    }
  }

  private static void expectNullPointer(Runnable action) {
    try {
      action.run();
      fail("expected a NullPointerException");
    } catch (NullPointerException expected) {
    }
  }

  private static CopyOutStreamImpl copyOut() {
    return new CopyOutStreamImpl(currentContext(), new TestReadStream());
  }

  private static CopyInStreamImpl copyIn() {
    return new CopyInStreamImpl(currentContext(), new PgCopyInOptions());
  }


  private static final class TestReadStream implements ReadStream<Object> {

    int pauseCalls;
    int resumeCalls;

    @Override
    public ReadStream<Object> exceptionHandler(Handler<Throwable> handler) {
      return this;
    }

    @Override
    public ReadStream<Object> handler(Handler<Object> handler) {
      return this;
    }

    @Override
    public ReadStream<Object> pause() {
      pauseCalls++;
      return this;
    }

    @Override
    public ReadStream<Object> resume() {
      resumeCalls++;
      return this;
    }

    @Override
    public ReadStream<Object> fetch(long amount) {
      return this;
    }

    @Override
    public ReadStream<Object> endHandler(Handler<Void> endHandler) {
      return this;
    }
  }
}
