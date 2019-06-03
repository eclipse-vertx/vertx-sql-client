package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.codec.Packets.ERROR_PACKET_HEADER;

class ResetStatementCommandCodec extends CommandCodec<Void, CloseCursorCommand> {
  ResetStatementCommandCodec(CloseCursorCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    MySQLPreparedStatement statement = (MySQLPreparedStatement) cmd.statement();

    statement.isCursorOpen = false;

    sendStatementResetCommand(statement);
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    int first = payload.getUnsignedByte(payload.readerIndex());
    if (first == ERROR_PACKET_HEADER) {
      handleErrorPacketPayload(payload);
    } else if (first == OkPacket.OK_PACKET_HEADER) {
      completionHandler.handle(CommandResponse.success(null));
    }
  }

  private void sendStatementResetCommand(MySQLPreparedStatement statement) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_RESET);
    packet.writeIntLE((int) statement.statementId);

    // set payload length
    int payloadLength = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, payloadLength);

    sendPacket(packet, payloadLength);
  }
}
