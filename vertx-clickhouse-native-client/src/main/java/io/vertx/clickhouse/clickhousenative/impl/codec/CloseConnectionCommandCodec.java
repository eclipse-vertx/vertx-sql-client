package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.command.CloseConnectionCommand;

public class CloseConnectionCommandCodec extends ClickhouseNativeCommandCodec<Void, CloseConnectionCommand> {
  private static final Logger LOG = LoggerFactory.getLogger(CloseConnectionCommandCodec.class);

  protected CloseConnectionCommandCodec(CloseConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }

  @Override
  public void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);
    LOG.info("closing channel");
    encoder.chctx().channel().close();
  }
}
