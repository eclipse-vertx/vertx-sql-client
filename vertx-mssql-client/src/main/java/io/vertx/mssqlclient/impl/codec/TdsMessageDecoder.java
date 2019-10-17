package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.mssqlclient.impl.protocol.TdsPacket;
import io.netty.buffer.CompositeByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.ArrayDeque;
import java.util.List;

class TdsMessageDecoder extends MessageToMessageDecoder<TdsPacket> {
  private final ArrayDeque<MSSQLCommandCodec<?, ?>> inflight;
  private final TdsMessageEncoder encoder;

  private TdsMessage message;

  TdsMessageDecoder(ArrayDeque<MSSQLCommandCodec<?, ?>> inflight, TdsMessageEncoder encoder) {
    this.inflight = inflight;
    this.encoder = encoder;
  }

  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, TdsPacket tdsPacket, List<Object> list) throws Exception {
    // assemble packets
    if (tdsPacket.status() == MessageStatus.END_OF_MESSAGE) {
      if (message == null) {
        message = TdsMessage.newTdsMessageFromSinglePacket(tdsPacket);
        decodeMessage();
      } else {
        // last packet of this message
        CompositeByteBuf messageData = (CompositeByteBuf) message.content();
        messageData.addComponent(true, tdsPacket.content());
        decodeMessage();
      }
    } else {
      if (message == null) {
        // first packet of this message and there will be more packets
        CompositeByteBuf messageData = channelHandlerContext.alloc().compositeBuffer();
        messageData.addComponent(true, tdsPacket.content());
        message = TdsMessage.newTdsMessage(tdsPacket.type(), tdsPacket.status(), tdsPacket.processId(), messageData);
      } else {
        CompositeByteBuf messageData = (CompositeByteBuf) message.content();
        messageData.addComponent(true, tdsPacket.content());
      }
    }
  }

  private void decodeMessage() {
    inflight.peek().decodeMessage(message, encoder);
    this.message = null;
  }
}
