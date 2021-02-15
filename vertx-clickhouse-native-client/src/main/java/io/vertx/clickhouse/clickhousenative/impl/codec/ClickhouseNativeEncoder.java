package io.vertx.clickhouse.clikhousenative.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOutboundHandlerAdapter;
import io.netty.channel.ChannelPromise;
import io.vertx.clickhouse.clikhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.InitCommand;

import java.util.ArrayDeque;

public class ClickhouseNativeEncoder extends ChannelOutboundHandlerAdapter {

  private final ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;
  private final ClickhouseNativeSocketConnection conn;

  private ChannelHandlerContext chctx;

  public ClickhouseNativeEncoder(ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight, ClickhouseNativeSocketConnection conn) {
      this.inflight = inflight;
      this.conn = conn;
  }

  ClickhouseNativeSocketConnection getConn() {
    return conn;
  }

  @Override
  public void handlerAdded(ChannelHandlerContext ctx) {
    chctx = ctx;
  }

  ChannelHandlerContext chctx() {
    return chctx;
  }

  @Override
  public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
    if (msg instanceof CommandBase<?>) {
      CommandBase<?> cmd = (CommandBase<?>) msg;
      write(cmd);
    } else {
      super.write(ctx, msg, promise);
    }
  }

  void write(CommandBase<?> cmd) {
    ClickhouseNativeCommandCodec<?, ?> codec = wrap(cmd);
    codec.completionHandler = resp -> {
      ClickhouseNativeCommandCodec<?, ?> c = inflight.poll();
      resp.cmd = (CommandBase) c.cmd;
      chctx.fireChannelRead(resp);
    };
    inflight.add(codec);
    codec.encode(this);
  }

  private ClickhouseNativeCommandCodec<?, ?> wrap(CommandBase<?> cmd) {
    if (cmd instanceof InitCommand) {
      return new InitCommandCodec((InitCommand) cmd);
    }
    throw new UnsupportedOperationException(cmd.getClass().getName());
  }
}
