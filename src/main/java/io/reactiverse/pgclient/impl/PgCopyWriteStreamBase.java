package io.reactiverse.pgclient.impl;

import io.netty.buffer.ByteBuf;
import io.vertx.codegen.annotations.Nullable;
import io.vertx.core.Handler;
import io.vertx.core.streams.WriteStream;
import java.util.concurrent.atomic.AtomicBoolean;

abstract class PgCopyWriteStreamBase<T> implements WriteStream<T>{
  private final AtomicBoolean closed;
  protected final Connection conn;
  protected Handler<Throwable> expHandler;
  protected Handler<Integer> endHandler;

  PgCopyWriteStreamBase(Connection conn) {
    this.conn = conn;
    closed = new AtomicBoolean(false);
  }

  private void handleCopyDone(int copyCount) {
    if (endHandler != null) {
      endHandler.handle(copyCount);
    }
  }

  public PgCopyWriteStreamBase<T> endHandler(Handler<Integer> endHandler) {
    this.endHandler = endHandler;
    return this;
  }

  @Override
  public PgCopyWriteStreamBase<T> exceptionHandler(Handler<Throwable> handler) {
    expHandler = handler;
    return this;
  }

  @Override
  public PgCopyWriteStreamBase<T> write(T tuple) {
    if (closed.get()) {
      expHandler.handle(new IllegalStateException("Close message has already been sent"));
    } else {
      conn.schedule(new CopyDataCommand(b -> writeCopyData(tuple, b), r -> {
        if (r.failed()) {
          expHandler.handle(r.cause());
        }
      }));
    }
    return this;
  }

  protected abstract void writeCopyData(T data, ByteBuf buffer);

  @Override
  public void end() {
    if (!closed.getAndSet(true)) {
      conn.schedule(new CopyEndCommand(this::writeEnd, count -> {
        if (count.failed()) {
          expHandler.handle(count.cause());
        } else if (count.result() != null){
          handleCopyDone(count.result());
        }
      }));
    } else {
      expHandler.handle(new IllegalStateException("Close message has already been sent"));
    }
  }

  protected abstract void writeEnd(ByteBuf buffer);

  protected abstract void writeHeader();

  @Override
  public PgCopyWriteStreamBase setWriteQueueMaxSize(int i) {
    return this;
  }

  @Override
  public boolean writeQueueFull() {
    return false;
  }

  @Override
  public PgCopyWriteStreamBase drainHandler(@Nullable Handler<Void> handler) {
    return this;
  }
}
