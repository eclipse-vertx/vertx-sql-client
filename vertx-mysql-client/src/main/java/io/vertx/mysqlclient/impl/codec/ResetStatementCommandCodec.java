package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.command.CloseCursorCommand;

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
    handleOkPacketOrErrorPacketPayload(payload);
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
