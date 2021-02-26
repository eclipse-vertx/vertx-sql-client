package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeRowDesc;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

import java.util.Collections;

public class PrepareStatementCodec extends ClickhouseNativeCommandCodec<PreparedStatement, PrepareStatementCommand>{
  private static final Logger LOG = LoggerFactory.getLogger(PrepareStatementCodec.class);

  protected PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(ClickhouseNativeEncoder encoder) {
    super.encode(encoder);
    LOG.info("handle ready for query");
    completionHandler.handle(CommandResponse.success(new ClickhouseNativePreparedStatement(cmd.sql(), new ClickhouseNativeParamDesc(Collections.emptyList()),
      new ClickhouseNativeRowDesc(Collections.emptyList()))));
  }

  @Override
  void decode(ChannelHandlerContext ctx, ByteBuf in) {
  }
}
