package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.StatisticsCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

class StatisticsCommandCodec extends CommandCodec<String, StatisticsCommand> {
  private static final int PAYLOAD_LENGTH = 1;

  StatisticsCommandCodec(StatisticsCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendStatisticsCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    completionHandler.handle(CommandResponse.success(payload.toString()));
  }

  private void sendStatisticsCommand() {
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STATISTICS);

    sendNonSplitPacket(packet);
  }
}
