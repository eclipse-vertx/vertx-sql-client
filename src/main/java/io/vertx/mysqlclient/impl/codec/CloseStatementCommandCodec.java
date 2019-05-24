package io.vertx.mysqlclient.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.mysqlclient.impl.protocol.CommandType;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

class CloseStatementCommandCodec extends CommandCodec<Void, CloseStatementCommand> {
  CloseStatementCommandCodec(CloseStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(MySQLEncoder encoder) {
    super.encode(encoder);
    MySQLPreparedStatement statement = (MySQLPreparedStatement) cmd.statement();
    sendCloseStatementCommand(statement);

    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    // no statement response
  }

  private void sendCloseStatementCommand(MySQLPreparedStatement statement) {
    ByteBuf packet = allocateBuffer();
    // encode packet header
    int packetStartIdx = packet.writerIndex();
    packet.writeMediumLE(0); // will set payload length later by calculation
    packet.writeByte(sequenceId++);

    // encode packet payload
    packet.writeByte(CommandType.COM_STMT_CLOSE);
    packet.writeIntLE((int) statement.statementId);

    // set payload length
    int lenOfPayload = packet.writerIndex() - packetStartIdx - 4;
    packet.setMediumLE(packetStartIdx, lenOfPayload);

    encoder.chctx.writeAndFlush(packet);
  }
}
