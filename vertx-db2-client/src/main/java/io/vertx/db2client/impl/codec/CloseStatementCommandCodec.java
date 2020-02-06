package io.vertx.db2client.impl.codec;

import io.netty.buffer.ByteBuf;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;
import io.vertx.sqlclient.impl.command.CommandResponse;

class CloseStatementCommandCodec extends CommandCodec<Void, CloseStatementCommand> {

  CloseStatementCommandCodec(CloseStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(DB2Encoder encoder) {
    DB2PreparedStatement statement = (DB2PreparedStatement) cmd.statement();
    statement.section.release();
    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    // no statement response
  }
}
