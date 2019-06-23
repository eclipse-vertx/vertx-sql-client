package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.DebugCommand;

class DebugCommandCodec extends CommandCodec<Void, DebugCommand> {
  private static final int PAYLOAD_LENGTH = 1;

  DebugCommandCodec(DebugCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendDebugCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    handleOkPacketOrErrorPacketPayload(payload);
  }

  private void sendDebugCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_DEBUG);

    sendPacket(packet, 1);
  }
}
