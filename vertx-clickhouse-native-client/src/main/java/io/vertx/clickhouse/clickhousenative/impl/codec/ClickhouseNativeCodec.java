package io.vertx.clickhouse.clickhousenative.impl.codec;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.clickhouse.clickhousenative.impl.ClickhouseNativeSocketConnection;
import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.util.ArrayDeque;

public class ClickhouseNativeCodec extends CombinedChannelDuplexHandler<ClickhouseNativeDecoder, ClickhouseNativeEncoder> {
  private static final Logger LOG = LoggerFactory.getLogger(ClickhouseNativeCodec.class);

  private ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;

  public ClickhouseNativeCodec(ClickhouseNativeSocketConnection conn) {
    inflight = new ArrayDeque<>();
    ClickhouseNativeEncoder encoder = new ClickhouseNativeEncoder(inflight, conn);
    ClickhouseNativeDecoder decoder = new ClickhouseNativeDecoder(inflight, conn);
    init(decoder, encoder);
  }

  @Override
  public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
    //TODO smagellan: maybe remove method
    LOG.error("caught exception", cause);
    super.exceptionCaught(ctx, cause);
  }
}
