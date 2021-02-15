package io.vertx.clickhouse.clikhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clikhousenative.impl.ClickhouseNativeDatabaseMetadata;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.InitCommand;

public class InitCommandCodec extends ClickhouseNativeCommandCodec<Connection, InitCommand> {
  private static final Logger LOG = LoggerFactory.getLogger(InitCommandCodec.class);

  InitCommandCodec(InitCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);

    ByteBuf buf = allocateBuffer();
    ByteBufUtils.writeULeb128(PacketTypes.HELLO, buf);
    ByteBufUtils.writeCString("ClickHouse " + "test", buf);
    ByteBufUtils.writeULeb128(20, buf);
    ByteBufUtils.writeULeb128(10, buf);
    ByteBufUtils.writeULeb128(54441, buf);
    ByteBufUtils.writeCString("default", buf);
    ByteBufUtils.writeCString("default", buf);
    ByteBufUtils.writeCString("clickhouse4man", buf);
    encoder.chctx().writeAndFlush(buf, encoder.chctx().voidPromise());
    LOG.info("sent hello packet");
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
    encoder.getConn().setDatabaseMetadata(new ClickhouseNativeDatabaseMetadata("", "", "", ""));
    LOG.info("decode");
    completionHandler.handle(CommandResponse.success(null));
  }
}
