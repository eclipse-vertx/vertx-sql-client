package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.CommandResponse;

abstract class ClickhouseNativeCommandCodec<R, C extends CommandBase<R>> {
  protected ClickhouseNativeEncoder encoder;
  protected Handler<? super CommandResponse<R>> completionHandler;
  protected final C cmd;

  protected ClickhouseNativeCommandCodec(C cmd) {
    this.cmd = cmd;
  }

  void encode(ClickhouseNativeEncoder encoder) {
    this.encoder = encoder;
  }

  abstract void decode(ChannelHandlerContext ctx, ByteBuf in);

  ByteBuf allocateBuffer() {
    return encoder.chctx().alloc().ioBuffer();
  }

  ByteBuf allocateBuffer(int capacity) {
    return encoder.chctx().alloc().ioBuffer(capacity);
  }
}
