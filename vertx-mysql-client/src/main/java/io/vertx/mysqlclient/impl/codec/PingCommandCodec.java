package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.PingCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

class PingCommandCodec extends CommandCodec<Void, PingCommand> {
  private static final int PAYLOAD_LENGTH = 1;

  PingCommandCodec(PingCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendPingCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    // we don't care what the response payload is from the server
    completionHandler.handle(CommandResponse.success(null));
  }

  private void sendPingCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_PING);

    sendNonSplitPacket(packet);
  }
}
