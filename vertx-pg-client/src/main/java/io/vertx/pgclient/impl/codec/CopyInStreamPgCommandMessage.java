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

package io.vertx.pgclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.WriteBufferWaterMark;
import io.vertx.pgclient.impl.CopyInStreamCommand;
import io.vertx.pgclient.impl.CopyInStreamInternal;

final class CopyInStreamPgCommandMessage
  extends PgCommandMessage<Void, CopyInStreamCommand>
  implements CopyInHandler {

  private PgEncoder encoder;

  private final CopyInStreamInternal in;

  private int rowCount = -1;

  CopyInStreamPgCommandMessage(CopyInStreamCommand cmd) {
    super(cmd);
    this.in = cmd.in();
  }

  @Override
  void encode(PgEncoder encoder) {
    this.encoder = encoder;

    encoder.writeQuery(new QueryMessage(cmd.sql()));

    encoder.suspendCommandPipeline();
  }

  @Override
  public void handleCopyInResponse(int overall, short[] colFmts) {
    in.attachSink(new NettyCopyInSink(encoder));
  }

  @Override
  public void handleCommandComplete(int updated) {
    in.detachSinkIfAny();
    rowCount = updated;
    result = null;
  }

  @Override
  public void handleErrorResponse(ErrorResponse err) {
    in.detachSinkIfAny();
    // Recorded, not reported: the command completion fails the stream, after ReadyForQuery
    failure = err.toException();
  }

  @Override
  void handleReadyForQuery() {
    encoder.resumeCommandPipeline();
    super.handleReadyForQuery();
    if (rowCount >= 0) {
      // Held back until now so the connection is free when the application hears
      in.completeFromServer(rowCount);
    }
  }

  private static final class NettyCopyInSink implements CopyInStreamInternal.Sink {

    private final PgEncoder encoder;

    private volatile ChannelHandlerContext ctx;

    private final String handlerName;

    private volatile Runnable writableCb;

    private volatile boolean detached;

    private boolean flushScheduled;

    private final WriteBufferWaterMark originalWatermark;

    private WriteBufferWaterMark copyWatermark;

    private final ChannelInboundHandlerAdapter writabilityHandler = new ChannelInboundHandlerAdapter() {

      @Override
      public void handlerAdded(ChannelHandlerContext c) throws Exception {
        ctx = c;
        super.handlerAdded(c);
      }

      @Override
      public void channelWritabilityChanged(ChannelHandlerContext c) throws Exception {
        if (!detached && c.channel().isWritable()) {
          Runnable cb = writableCb;
          if (cb != null) {
            c.executor().execute(cb);
          }
        }
        super.channelWritabilityChanged(c);
      }

      @Override
      public void channelReadComplete(ChannelHandlerContext c) throws Exception {
        if (!detached && flushScheduled) {
          doFlush();
        }
        super.channelReadComplete(c);
      }
    };

    NettyCopyInSink(PgEncoder encoder) {
      this.encoder = encoder;

      ChannelHandlerContext encoderCtx = encoder.channelHandlerContext();
      if (encoderCtx == null) {
        throw new IllegalStateException("PgEncoder.channelHandlerContext() returned null");
      }

      this.handlerName = "pg-copyin-writability@" + System.identityHashCode(this);

      ChannelPipeline p = encoderCtx.pipeline();
      if (p.get(handlerName) == null) {
        p.addAfter("codec", handlerName, writabilityHandler);
      }

      if (ctx == null) {
        throw new IllegalStateException("COPY IN sink ctx was not initialized");
      }

      this.originalWatermark = ctx.channel().config().getWriteBufferWaterMark();
    }

    @Override
    public boolean isWritable() {
      return ctx.channel().isWritable();
    }

    @Override
    public void onWritable(Runnable cb) {
      this.writableCb = cb;
    }

    @Override
    public synchronized void setWatermarks(int maxBytes) {
      if (detached) {
        return;
      }
      try {
        int hi = Math.max(64 * 1024, maxBytes);
        int lo = Math.max(32 * 1024, hi / 2);
        WriteBufferWaterMark watermark = new WriteBufferWaterMark(lo, hi);
        ctx.channel().config().setWriteBufferWaterMark(watermark);
        copyWatermark = watermark;
      } catch (Throwable ignore) {

      }
    }

    @Override
    public void writeCopyData(ByteBuf buf) {
      encoder.writeCopyData(buf);
      flushAfterCopyDataWrite();
    }

    private void flushAfterCopyDataWrite() {
      ChannelHandlerContext c = ctx;
      if (!c.channel().isWritable() || c.channel().bytesBeforeUnwritable() == 0) {
        doFlush();
      } else {
        scheduleFlush();
      }
    }

    @Override
    public void writeCopyDone() {
      encoder.writeCopyDone();
      doFlush();
    }

    @Override
    public void writeCopyFail(String message) {
      encoder.writeCopyFail(message);
      doFlush();
    }

    @Override
    public synchronized void detach() {
      if (detached) {
        return;
      }
      detached = true;

      try {
        // Do not overwrite a watermark installed by another owner during COPY.
        WriteBufferWaterMark currentWatermark = ctx.channel().config().getWriteBufferWaterMark();
        if (copyWatermark != null && currentWatermark == copyWatermark) {
          ctx.channel().config().setWriteBufferWaterMark(originalWatermark);
        }
      } catch (Throwable ignore) {
      }

      try {
        ChannelPipeline p = ctx.pipeline();
        if (p.get(handlerName) != null) {
          p.remove(handlerName);
        }
      } catch (Throwable ignore) {
      }
    }

    private void scheduleFlush() {
      if (flushScheduled || detached) return;
      flushScheduled = true;

      ChannelHandlerContext c = ctx;
      c.executor().execute(() -> {
        if (detached || !flushScheduled) {
          return;
        }
        doFlush();
      });
    }

    private void doFlush() {
      if (detached) return;

      flushScheduled = false;
      ctx.flush();
    }
  }
}
