package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

public class CloseStatementCommandCodec extends ClickhouseNativeCommandCodec<Void, CloseStatementCommand> {
  public CloseStatementCommandCodec(CloseStatementCommand cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd);
  }

  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);
    completionHandler.handle(CommandResponse.success(null));
  }


  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}
