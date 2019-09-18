package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.ArrayDeque;
import java.util.List;

import static io.vertx.mysqlclient.impl.codec.Packets.*;

class MySQLDecoder extends ByteToMessageDecoder {

  private final ArrayDeque<CommandCodec<?, ?>> inflight;
  private final MySQLEncoder encoder;

  private CompositeByteBuf aggregatedPacketPayload = null;

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

      if (payloadLength >= PACKET_PAYLOAD_LENGTH_LIMIT && aggregatedPacketPayload == null) {
        aggregatedPacketPayload = ctx.alloc().compositeBuffer();
      }

      // payload
      if (in.readableBytes() >= payloadLength) {
        if (aggregatedPacketPayload != null) {
          // read a split packet
          aggregatedPacketPayload.addComponent(true, in.readRetainedSlice(payloadLength));
          sequenceId++;

          if (payloadLength < PACKET_PAYLOAD_LENGTH_LIMIT) {
            // we have just read the last split packet and there will be no more split packet
            decodePayload(aggregatedPacketPayload, aggregatedPacketPayload.readableBytes(), sequenceId);
            aggregatedPacketPayload.release();
            aggregatedPacketPayload = null;
          }
        } else {
          // read a non-split packet
          decodePayload(in.readSlice(payloadLength), payloadLength, sequenceId);
        }
      } else {
        in.readerIndex(packetStartIdx);
      }
    }
  }

  private void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    CommandCodec ctx = inflight.peek();
    ctx.sequenceId = sequenceId + 1;
    ctx.decodePayload(payload, payloadLength);
    payload.clear();
  }
}
