package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.SetOptionCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.codec.Packets.*;
import static io.vertx.mysqlclient.impl.protocol.backend.EofPacket.*;

class SetOptionCommandCodec extends CommandCodec<Void, SetOptionCommand> {
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

  private void sendSetOptionCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    packet.writeMediumLE(3);
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_SET_OPTION);
    packet.writeShortLE(cmd.option().ordinal());

    sendPacket(packet, 3);
  }
}
