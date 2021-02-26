package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.ExtendedQueryCommand;

import java.util.Collections;

public class ExtendedQueryCodec<T> extends ClickhouseNativeQueryCommandBaseCodec<T, ExtendedQueryCommand<T>> {
  private final ClickhouseNativeSocketConnection conn;

  public ExtendedQueryCodec(ExtendedQueryCommand<T> cmd, ClickhouseNativeSocketConnection conn) {
    super(cmd);
    this.conn = conn;
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    conn.throwExceptionIfBusy();
    conn.setPendingCursorId(cmd.cursorId());
    super.encode(encoder);
    ClickhouseNativeRowDesc rowDesc = new ClickhouseNativeRowDesc(Collections.emptyList());
    RowResultDecoder<?, T> rowResultDecoder = new RowResultDecoder<>(cmd.collector(), rowDesc);
    Throwable t = rowResultDecoder.complete();
    cmd.resultHandler().handleResult(0, 0, rowDesc, rowResultDecoder.result(), t);
    completionHandler.handle(CommandResponse.success(false));
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}
