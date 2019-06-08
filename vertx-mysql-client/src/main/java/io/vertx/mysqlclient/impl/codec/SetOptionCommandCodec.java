package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.SetOptionCommand;

class SetOptionCommandCodec extends CommandCodec<Void, SetOptionCommand> {
  private static final int PAYLOAD_LENGTH = 3;

  SetOptionCommandCodec(SetOptionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendSetOptionCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    handleOkPacketOrErrorPacketPayload(payload);
  }

  private void sendSetOptionCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_SET_OPTION);
    packet.writeShortLE(cmd.option().ordinal());

    sendNonSplitPacket(packet);
  }
}
