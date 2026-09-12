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

package io.vertx.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.buffer.Unpooled;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.buffer.BufferInternal;
import io.vertx.core.streams.WriteStream;
import io.vertx.pgclient.PgCopyIn;
import io.vertx.pgclient.PgCopyInOptions;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

public final class CopyInStreamImpl implements CopyInStreamInternal, WriteStream<Buffer> {

  private static final class PendingFrame {
    final ByteBuf buf;
    final int bytes;
    final List<Promise<Void>> writePromises;

    PendingFrame(ByteBuf buf, int bytes, List<Promise<Void>> writePromises) {
      this.buf = buf;
      this.bytes = bytes;
      this.writePromises = writePromises;
    }

    void completeWrites() {
      if (writePromises == null) return;
      for (Promise<Void> p : writePromises) {
        p.tryComplete();
      }
    }

    void failWrites(Throwable t) {
      if (writePromises == null) return;
      for (Promise<Void> p : writePromises) {
        p.tryFail(t);
      }
    }
  }

  /**
   * Components the accumulating buffer may hold before Netty consolidates it.
   * <p/>
   * {@link Unpooled#compositeBuffer()} allows only 16, and every add beyond that copies the whole
   * payload accumulated so far, which turns coalescing many small writes into quadratic copying.
   * The bound is kept well below the number of buffers a gathering write can carry so that the
   * frame still leaves as a single vectored write.
   */
  private static final int MAX_CHUNK_COMPONENTS = 1024;

  private final ContextInternal context;

  private final Promise<Integer> completion;

  private final Promise<PgCopyIn> ready;

  private final int chunkBytes;

  /** Frames built and waiting for the transport to accept them. */
  private final Deque<PendingFrame> pending = new ArrayDeque<>();

  private int pendingBytes;

  /** The frame currently being accumulated from application writes. */
  private CompositeByteBuf building;

  private int buildingBytes;

  private List<Promise<Void>> buildingWritePromises;

  private boolean draining;

  private boolean drainScheduled;

  private Sink sink;

  private int writeQueueMaxSize = 8 * 1024 * 1024;

  private Handler<Void> drainHandler;

  private Handler<Throwable> exceptionHandler;

  private boolean wasFull;

  private boolean ended;

  private Throwable failure;

  private boolean copyDoneSent;

  private boolean copyFailSent;

  private String pendingCopyFailMessage;

  public CopyInStreamImpl(ContextInternal context, PgCopyInOptions options) {
    this.context = context;
    this.completion = context.promise();
    this.ready = context.promise();
    this.chunkBytes = options.getChunkSize();
  }

  @Override
  public Future<Void> write(Buffer data) {
    // Not context.promise(): completed on the connection's event loop anyway, and a context bound
    // future costs an isRunningOnContext check plus beginDispatch/endDispatch on every write
    Promise<Void> promise = Promise.promise();

    context.emit(promise, p -> {
      if (failure != null) {
        p.fail(failure);
        return;
      }

      if (ended) {
        p.fail(new IllegalStateException("COPY IN already ended"));
        return;
      }

      if (data == null) {
        p.fail(new NullPointerException("data"));
        return;
      }

      ByteBuf part = retainByteBuf(data);

      int sz = part.readableBytes();
      if (sz == 0) {
        part.release();
        p.complete();
        return;
      }

      ensureBuilding();

      building.addComponent(true, part);

      buildingBytes += sz;

      buildingWritePromises.add(p);

      if (buildingBytes >= chunkBytes || writeQueueFull()) {
        enqueueBuilt();
      }

      scheduleDrain();
    });

    return promise.future();
  }

  @Override
  public boolean writeQueueFull() {
    return (pendingBytes + buildingBytes) >= writeQueueMaxSize;
  }

  @Override
  public Future<Void> end() {
    Promise<Void> ignored = context.promise();

    context.emit(ignored, p -> {
      if (ended) {
        p.complete();
        return;
      }

      ended = true;

      if (buildingBytes > 0) {
        enqueueBuilt();
      }

      scheduleDrain();
      p.complete();
    });

    return completion.future().mapEmpty();
  }

  @Override
  public Future<Integer> completion() {
    return completion.future();
  }

  @Override
  public Future<PgCopyIn> readyFuture() {
    return ready.future();
  }

  @Override
  public CopyInStreamImpl exceptionHandler(@Nullable Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    return this;
  }

  @Override
  public CopyInStreamImpl setWriteQueueMaxSize(int maxSize) {
    if (maxSize < 1) {
      maxSize = 1;
    }
    this.writeQueueMaxSize = maxSize;

    Sink s = this.sink;
    if (s != null) {
      s.setWatermarks(maxSize);
    }

    return this;
  }

  @Override
  public CopyInStreamImpl drainHandler(@Nullable Handler<Void> handler) {
    this.drainHandler = handler;
    return this;
  }

  @Override
  public Future<Void> abort(String message) {
    Promise<Void> promise = context.promise();

    context.emit(promise, p -> {
      if (failure != null) {
        p.fail(failure);
        return;
      }

      if (ended) {
        p.fail(new IllegalStateException("COPY IN already ended"));
        return;
      }

      String msg = message != null ? message : "COPY IN aborted";
      pendingCopyFailMessage = msg;

      // Stop the stream and send CopyFail, but leave completion() to the server: it reports the
      // real cause when the COPY had already failed for a reason of its own.
      failLocally(new IllegalStateException(msg), true);

      p.complete();
    });

    return promise.future();
  }

  @Override
  public void attachSink(Sink sink) {
    context.runOnContext(v -> {

      Sink prev = this.sink;
      if (prev != null) {
        prev.detach();
      }

      if (failure != null) {
        if (!copyFailSent) {
          copyFailSent = true;
          try {
            sink.writeCopyFail(pendingCopyFailMessage != null ? pendingCopyFailMessage : safeMsg(failure));
          } catch (Throwable ignore) {
          }
        }
        sink.detach();
        this.sink = null;
        return;
      }

      this.sink = sink;

      sink.setWatermarks(writeQueueMaxSize);

      sink.onWritable(this::scheduleDrain);

      ready.tryComplete(this);

      syncFullState();
    });
  }

  @Override
  public void detachSinkIfAny() {
    context.runOnContext(v -> {
      Sink s = this.sink;
      this.sink = null;

      if (s != null) {
        s.detach();
      }
    });
  }

  @Override
  public void completeFromServer(int rowCount) {
    context.runOnContext(v -> completion.tryComplete(rowCount));
  }

  @Override
  public void failFromServer(Throwable t) {
    context.runOnContext(v -> {
      failLocally(t, false);
      // Settles completion() even when a local abort already stopped the stream
      completion.tryFail(t);
    });
  }

  private void notifyIfNoLongerFull() {
    boolean fullNow = writeQueueFull();

    if (wasFull && !fullNow) {
      Handler<Void> dh = drainHandler;
      if (dh != null) {

        context.runOnContext(v -> dh.handle(null));
      }
    }

    wasFull = fullNow;
  }

  private void syncFullState() {
    wasFull = writeQueueFull();
  }

  private void failNow(Throwable t, boolean sendCopyFail) {
    failLocally(t, sendCopyFail);
    completion.tryFail(t);
  }

  /**
   * Move the stream to its failed state and notify the application, without deciding the outcome of
   * the COPY command itself.
   */
  private void failLocally(Throwable t, boolean sendCopyFail) {
    if (failure != null) {
      return;
    }

    failure = t;

    ready.tryFail(t);

    if (sendCopyFail) {
      Sink s = sink;
      if (s != null && !copyFailSent) {
        copyFailSent = true;
        try {
          s.writeCopyFail(pendingCopyFailMessage != null ? pendingCopyFailMessage : safeMsg(t));
        } catch (Throwable ignore) {

        }
      }
    }

    failAllPendingPromises(t);
    releaseAllPending();

    Handler<Throwable> eh = exceptionHandler;
    if (eh != null) {
      context.runOnContext(v -> eh.handle(t));
    }
  }

  private static ByteBuf retainByteBuf(Buffer data) {
    ByteBuf bb = ((BufferInternal) data).getByteBuf();
    return bb.retainedDuplicate();
  }

  private static String safeMsg(Throwable t) {
    String m = t.getMessage();
    return m != null ? m : t.getClass().getName();
  }

  private void scheduleDrain() {
    if (drainScheduled) return;
    drainScheduled = true;

    context.runOnContext(v -> {
      drainScheduled = false;
      // Batch writes made in the same turn, but never strand a partial frame.
      if (failure == null && buildingBytes > 0) {
        enqueueBuilt();
      }
      doDrain();
    });
  }

  private void doDrain() {
    if (draining) return;

    draining = true;
    try {
      Sink s = sink;

      if (s == null) {
        return;
      }

      if (failure != null) {
        return;
      }

      while (!pending.isEmpty() && s.isWritable()) {
        PendingFrame frame = pending.pollFirst();
        pendingBytes -= frame.bytes;

        writeFrame(s, frame);

        if (failure != null) {
          return;
        }
      }

      notifyIfNoLongerFull();

      if (ended && !copyDoneSent && pending.isEmpty()) {
        copyDoneSent = true;
        s.writeCopyDone();
      }

    } catch (Throwable t) {
      failNow(t, true);
    } finally {
      draining = false;
    }
  }

  /**
   * Hand the frame to the transport and settle the writes it carries.
   * <p/>
   * The write futures resolve once the frame is accepted by the transport, which only happens
   * while the channel is writable, so back pressure is preserved. Whether the COPY itself
   * succeeded is reported by {@link #completion()}, the only answer that is meaningful for a
   * statement PostgreSQL applies atomically.
   */
  private void writeFrame(Sink s, PendingFrame frame) {
    try {
      s.writeCopyData(frame.buf);

      frame.completeWrites();
    } catch (Throwable t) {

      try {
        frame.buf.release();
      } catch (Throwable ignore) {
      }

      frame.failWrites(t);
      failNow(t, true);
    }
  }

  private void ensureBuilding() {
    if (building == null) {
      building = Unpooled.compositeBuffer(MAX_CHUNK_COMPONENTS);
      buildingBytes = 0;
      buildingWritePromises = new ArrayList<>(8);
    }
  }

  private void enqueueBuilt() {
    if (building == null || buildingBytes == 0) {
      return;
    }

    ByteBuf payload = building;
    int sz = buildingBytes;
    List<Promise<Void>> writePromises = buildingWritePromises;

    building = null;
    buildingBytes = 0;
    buildingWritePromises = null;

    pending.addLast(new PendingFrame(payload, sz, writePromises));
    pendingBytes += sz;

    wasFull = writeQueueFull();
  }

  private void failAllPendingPromises(Throwable t) {
    if (buildingWritePromises != null) {
      for (Promise<Void> p : buildingWritePromises) {
        p.tryFail(t);
      }
      buildingWritePromises = null;
    }

    for (PendingFrame f : pending) {
      f.failWrites(t);
    }
  }

  private void releaseAllPending() {
    if (building != null) {
      try {
        building.release();
      } catch (Throwable ignore) {
      }
      building = null;
      buildingBytes = 0;
      buildingWritePromises = null;
    }

    while (!pending.isEmpty()) {
      PendingFrame f = pending.pollFirst();
      try {
        f.buf.release();
      } catch (Throwable ignore) {
      }
    }

    pendingBytes = 0;
  }
}
