package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.mysqlclient.impl.protocol.backend.OkPacket;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

import static io.vertx.mysqlclient.impl.codec.Packets.ERROR_PACKET_HEADER;

class ResetStatementCommandCodec extends CommandCodec<Void, CloseCursorCommand> {
  private static final int PAYLOAD_LENGTH = 5;

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
    ByteBuf packet = allocateBuffer(PAYLOAD_LENGTH + 4);
    // encode packet header
    packet.writeMediumLE(PAYLOAD_LENGTH);
    packet.writeByte(sequenceId);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_RESET);
    packet.writeIntLE((int) statement.statementId);

    sendNonSplitPacket(packet);
  }
}
