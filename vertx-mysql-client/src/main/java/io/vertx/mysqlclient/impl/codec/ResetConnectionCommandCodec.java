package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.ResetConnectionCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.codec.Packets.*;
import static io.vertx.mysqlclient.impl.protocol.backend.OkPacket.*;

class ResetConnectionCommandCodec extends CommandCodec<Void, ResetConnectionCommand> {
  ResetConnectionCommandCodec(ResetConnectionCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendResetConnectionCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    int header = payload.getUnsignedByte(payload.readerIndex());
    switch (header) {
      case OK_PACKET_HEADER:
        completionHandler.handle(CommandResponse.success(null));
        break;
      case ERROR_PACKET_HEADER:
        handleErrorPacketPayload(payload);
        break;
    }
  }

  private void sendResetConnectionCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    packet.writeMediumLE(1);
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_RESET_CONNECTION);

    sendPacket(packet, 1);
  }
}
