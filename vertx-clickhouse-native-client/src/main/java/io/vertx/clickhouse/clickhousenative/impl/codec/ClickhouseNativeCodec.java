package io.vertx.clickhouse.clikhousenative.impl.codec;

import io.netty.channel.CombinedChannelDuplexHandler;
import io.vertx.clickhouse.clikhousenative.impl.ClickhouseNativeSocketConnection;

import java.util.ArrayDeque;

public class ClickhouseNativeCodec extends CombinedChannelDuplexHandler<ClickhouseNativeDecoder, ClickhouseNativeEncoder> {
  private ArrayDeque<ClickhouseNativeCommandCodec<?, ?>> inflight;

  public ClickhouseNativeCodec(ClickhouseNativeSocketConnection conn) {
    inflight = new ArrayDeque<>();
    ClickhouseNativeEncoder encoder = new ClickhouseNativeEncoder(inflight, conn);
    ClickhouseNativeDecoder decoder = new ClickhouseNativeDecoder(inflight, conn);
    init(decoder, encoder);
  }
}
