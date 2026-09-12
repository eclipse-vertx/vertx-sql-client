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

import io.vertx.core.internal.ContextInternal;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.streams.ReadStream;
import io.vertx.pgclient.PgCopyOut;

import java.util.ArrayDeque;
import java.util.Objects;
import java.util.Queue;

public final class CopyOutStreamImpl implements PgCopyOut {

  private final ContextInternal ctx;
  private final ReadStream<?> upstream;
  private final Promise<Integer> completion;
  private final Promise<PgCopyOut> ready;

  private Handler<Throwable> exceptionHandler;
  private Handler<Buffer> handler;
  private Handler<Void> endHandler;

  private boolean paused;
  private int rowCount;
  private long demand = Long.MAX_VALUE;
  private boolean ended;
  private boolean endReached;
  private boolean endHandlerNotified;
  private boolean discarding;
  private boolean draining;
  private boolean upstreamPaused;
  private Throwable failure;

  // Buffers events already dispatched when the upstream stream was paused.
  // The upstream Vert.x read queue owns the actual backpressure watermarks.
  private final Queue<Buffer> queue = new ArrayDeque<>();

  public CopyOutStreamImpl(ContextInternal ctx, ReadStream<?> upstream) {
    this.ctx = Objects.requireNonNull(ctx, "ctx");
    this.upstream = Objects.requireNonNull(upstream, "upstream");
    this.completion = ctx.promise();
    this.ready = ctx.promise();
  }

  public void readyFromServer() {
    // Called from the codec on the connection's event loop, so settle it now: deferring would let
    // ReadyForQuery be processed first and make the stream look like it never started.
    ready.tryComplete(this);
  }

  public Future<PgCopyOut> readyFuture() {
    return ready.future();
  }

  public void emit(Buffer buf) {
    if (ended) return;
    if (buf == null) return;

    if (handler == null || paused || demand == 0) {
      queue.add(buf);
      updateUpstream();
      return;
    }

    deliver(buf);
    updateUpstream();
  }

  /**
   * The server sent CommandComplete, no more data will be emitted. The COPY row count is held
   * until the server is ready for the next command, see {@link #commandCompleted()}.
   */
  public void end(int rowCount) {
    if (ended) return;
    ended = true;
    this.rowCount = rowCount;
    drain();
  }

  /**
   * The server sent ReadyForQuery, the COPY command is done and the connection is free again.
   */
  public void commandCompleted() {
    completion.tryComplete(rowCount);
  }

  public void discard(Throwable t) {
    discarding = true;
    queue.clear();
    resumeUpstream();
    fail(t);
  }

  public boolean isDiscarding() {
    return discarding;
  }

  public void fail(Throwable t) {
    // Always settle the futures the caller holds. A statement that completed without entering copy
    // mode ends the stream without ever handing one out, and the data can be drained before the
    // server acknowledges the command, leaving the completion for a ReadyForQuery that never comes.
    ready.tryFail(t);
    completion.tryFail(t);
    if (failure != null || endReached) return;
    failure = Objects.requireNonNull(t, "t");
    discarding = true;
    ended = true;
    queue.clear();
    resumeUpstream();
    Handler<Throwable> h = exceptionHandler;
    if (h != null) {
      ctx.runOnContext(v -> h.handle(t));
    }
  }

  @Override
  public CopyOutStreamImpl exceptionHandler(Handler<Throwable> handler) {
    this.exceptionHandler = handler;
    if (failure != null && handler != null) {
      ctx.runOnContext(v -> handler.handle(failure));
    }
    return this;
  }

  @Override
  public CopyOutStreamImpl handler(Handler<Buffer> handler) {
    this.handler = handler;
    drain();
    return this;
  }

  @Override
  public CopyOutStreamImpl pause() {
    paused = true;
    demand = 0;
    updateUpstream();
    return this;
  }

  @Override
  public CopyOutStreamImpl resume() {
    paused = false;
    demand = Long.MAX_VALUE;
    drain();
    return this;
  }

  @Override
  public CopyOutStreamImpl fetch(long amount) {
    if (amount < 0) throw new IllegalArgumentException("amount < 0");
    paused = false;

    if (demand != Long.MAX_VALUE) {
      long next = demand + amount;
      if (next < 0) {
        demand = Long.MAX_VALUE;
      } else {
        demand = next;
      }
    }

    drain();
    return this;
  }

  @Override
  public CopyOutStreamImpl endHandler(Handler<Void> endHandler) {
    this.endHandler = endHandler;
    notifyEndHandler();
    return this;
  }

  @Override
  public Future<Integer> completion() {
    return completion.future();
  }

  private void drain() {
    if (draining) {
      return;
    }
    if (handler == null || paused || demand == 0) {
      updateUpstream();
      signalEndIfDrained();
      return;
    }
    draining = true;
    try {
      while (!queue.isEmpty() && handler != null && !paused && demand != 0) {
        Buffer buf = queue.poll();
        deliver(buf);
      }
    } finally {
      draining = false;
      updateUpstream();
      signalEndIfDrained();
    }
  }

  private void deliver(Buffer buf) {
    Handler<Buffer> h = handler;
    if (h == null) {
      queue.add(buf);
      return;
    }

    if (demand != Long.MAX_VALUE) {
      demand--;
    }

    try {
      h.handle(buf);
    } catch (Throwable t) {
      fail(t);
    }
  }

  private void updateUpstream() {
    if (ended || (handler != null && !paused && demand != 0)) {
      resumeUpstream();
    } else {
      pauseUpstream();
    }
  }

  private void pauseUpstream() {
    if (!upstreamPaused) {
      upstreamPaused = true;
      upstream.pause();
    }
  }

  private void resumeUpstream() {
    if (upstreamPaused) {
      upstreamPaused = false;
      upstream.resume();
    }
  }

  private void signalEndIfDrained() {
    if (ended && queue.isEmpty() && failure == null) {
      endReached = true;
      notifyEndHandler();
    }
  }

  private void notifyEndHandler() {
    Handler<Void> h = endHandler;
    if (endReached && !endHandlerNotified && h != null) {
      endHandlerNotified = true;
      ctx.runOnContext(v -> h.handle(null));
    }
  }
}
