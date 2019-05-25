package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayDeque;
import java.util.List;

class MySQLDecoder extends ByteToMessageDecoder {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  private final MySQLEncoder encoder;

  MySQLDecoder(ArrayDeque<CommandCodec<?, ?>> inflight, MySQLEncoder encoder) {
    this.inflight = inflight;
    this.encoder = encoder;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() > 4) {
      int packetStartIdx = in.readerIndex();
      int payloadLength = in.readUnsignedMediumLE();
      int sequenceId = in.readUnsignedByte();

      // payload
      if (in.readableBytes() >= payloadLength) {
        int payloadStartIdx = in.readerIndex();
        ByteBuf payload = in.slice(payloadStartIdx, payloadLength);
        in.readerIndex(payloadStartIdx + payloadLength);
        decodePayload(payload, encoder, payloadLength, sequenceId, out);
      } else {
        in.readerIndex(packetStartIdx);
      }
    }
  }

  private void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId, List<Object> out) {
    CommandCodec ctx = inflight.peek();
    ctx.sequenceId = sequenceId + 1;
    ctx.decodePayload(payload, encoder, payloadLength, sequenceId);
  }
}
