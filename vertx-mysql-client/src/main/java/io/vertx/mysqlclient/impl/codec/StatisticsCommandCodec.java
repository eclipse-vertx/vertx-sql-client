package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.command.StatisticsCommand;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CommandResponse;

import java.nio.charset.StandardCharsets;

class StatisticsCommandCodec extends CommandCodec<String, StatisticsCommand> {
  StatisticsCommandCodec(StatisticsCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    sendStatisticsCommand();
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    completionHandler.handle(CommandResponse.success(payload.toString(StandardCharsets.UTF_8)));
  }

  private void sendStatisticsCommand() {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    packet.writeMediumLE(1);
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_STATISTICS);

    sendPacket(packet, 1);
  }
}
