package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

public class CloseCursorCommandCodec extends ClickhouseNativeCommandCodec<Void, CloseCursorCommand>{
  private final ClickhouseNativeSocketConnection conn;

  protected CloseCursorCommandCodec(CloseCursorCommand cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd);
    this.conn = conn;
  }

  void encode(ClickhouseNativeEncoder encoder) {
    conn.releaseCursor(((ClickhouseNativePreparedStatement)cmd.statement()).getPsId(), cmd.id());
    super.encode(encoder);
    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}

