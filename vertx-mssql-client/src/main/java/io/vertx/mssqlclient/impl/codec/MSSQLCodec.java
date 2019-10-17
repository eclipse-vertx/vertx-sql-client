package io.vertx.mssqlclient.impl.codec;

import io.netty.channel.ChannelPipeline;

import java.util.ArrayDeque;

public class MSSQLCodec {
  public static void initPipeLine(ChannelPipeline pipeline) {
    final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight = new ArrayDeque<>();

    TdsMessageEncoder encoder = new TdsMessageEncoder(inflight);
    TdsMessageDecoder messageDecoder = new TdsMessageDecoder(inflight, encoder);
    TdsPacketDecoder packetDecoder = new TdsPacketDecoder();
    pipeline.addBefore("handler", "encoder", encoder);
    pipeline.addBefore("encoder", "messageDecoder", messageDecoder);
    pipeline.addBefore("messageDecoder", "packetDecoder", packetDecoder);
  }
}
