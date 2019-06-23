package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.ResetConnectionCommand;

class ResetConnectionCommandCodec extends CommandCodec<Void, ResetConnectionCommand> {
  private static final int PAYLOAD_LENGTH = 1;

  ResetConnectionCommandCodec(ResetConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendResetConnectionCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength, int sequenceId) {
    handleOkPacketOrErrorPacketPayload(payload);
  }

  private void sendResetConnectionCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_RESET_CONNECTION);

    sendNonSplitPacket(packet);
  }
}
