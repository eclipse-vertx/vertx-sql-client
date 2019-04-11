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
  void encodePayload(MySQLEncoder encoder) {
    super.encodePayload(encoder);
    MySQLPreparedStatement statement = (MySQLPreparedStatement) cmd.statement();
    ByteBuf payload = allocateBuffer();
    payload.writeByte(CommandType.COM_STMT_CLOSE);
    payload.writeIntLE((int) statement.statementId);
    sendPacketWithBody(payload);

    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decodePayload(ByteBuf payload, MySQLEncoder encoder, int payloadLength, int sequenceId) {
    // no statement response
  }
}
