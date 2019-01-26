package io.reactiverse.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.reactiverse.mysqlclient.impl.MySQLSocketConnection;

import java.nio.charset.Charset;
import java.util.List;

public abstract class MySQLPacketDecoder extends ByteToMessageDecoder {
  protected final Charset charset;
  protected final MySQLSocketConnection socketConnection;

  public MySQLPacketDecoder(Charset charset, MySQLSocketConnection socketConnection) {
    this.charset = charset;
    this.socketConnection = socketConnection;
  }

  @Override
  protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
    if (in.readableBytes() > 4) {
      int packetStartIdx = in.readerIndex();
      int payloadLength = in.readUnsignedMediumLE();
      int sequenceId = in.readUnsignedByte();
      //TODO check how to handle sequence number correctly?
      socketConnection.modifySequenceId(sequenceId, sequenceId + 1);

      // payload
      if (in.readableBytes() >= payloadLength) {
        int payloadStartIdx = in.readerIndex();

        ByteBuf payload = in.slice(payloadStartIdx, payloadLength);
        in.readerIndex(payloadStartIdx + payloadLength);
        decodePayload(ctx, payload, payloadLength, sequenceId, out);
      } else {
        in.readerIndex(packetStartIdx);
      }
    }
  }

  protected abstract void decodePayload(ChannelHandlerContext ctx, ByteBuf payload, int payloadLength, int sequenceId, List<Object> out);
}
