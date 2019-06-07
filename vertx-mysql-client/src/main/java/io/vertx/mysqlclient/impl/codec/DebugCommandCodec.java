package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.DebugCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.codec.Packets.*;
import static io.vertx.mysqlclient.impl.protocol.backend.EofPacket.*;

class DebugCommandCodec extends CommandCodec<Void, DebugCommand> {
  DebugCommandCodec(DebugCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendDebugCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case EOF_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  private void sendDebugCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    packet.writeMediumLE(1);
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_DEBUG);

    sendPacket(packet, 1);
  }
}
