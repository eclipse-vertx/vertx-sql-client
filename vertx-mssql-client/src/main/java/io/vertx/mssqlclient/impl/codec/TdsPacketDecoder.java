package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.MessageStatus;
import io.vertx.mssqlclient.impl.protocol.MessageType;
import io.vertx.mssqlclient.impl.protocol.TdsPacket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

class TdsPacketDecoder extends ByteToMessageDecoder {
  @Override
  protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
    // decoding a packet
    if (in.readableBytes() > TdsPacket.PACKET_HEADER_SIZE) {
      int packetStartIdx = in.readerIndex();
      int packetLen = in.getUnsignedShort(packetStartIdx + 2);

      if (in.readableBytes() >= packetLen) {
        MessageType type = MessageType.valueOf(in.readUnsignedByte());
        MessageStatus status = MessageStatus.valueOf(in.readUnsignedByte());
        in.skipBytes(2); // packet length
        int processId = in.readUnsignedShort();
        short packetId = in.readUnsignedByte();
        in.skipBytes(1); // unused window

        ByteBuf packetData = in.readRetainedSlice(packetLen - TdsPacket.PACKET_HEADER_SIZE);

        list.add(TdsPacket.newTdsPacket(type, status, packetLen, processId, packetId, packetData));
      }
    }
  }
}
