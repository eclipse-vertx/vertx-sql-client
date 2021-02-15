package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.sqlclient.impl.command.SimpleQueryCommand;

public class SimpleQueryCodec<T> extends ClickhouseNativeQueryCommandBaseCodec<T, SimpleQueryCommand<T>>{
  protected SimpleQueryCodec(SimpleQueryCommand<T> cmd) {
    super(cmd);
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}
