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
    statement.close();
    // Currently all cursors are implicitly closed when complete on the server side
    // TODO: Flow closes for all remaining cursor that may not have been completed
    completionHandler.handle(CommandResponse.success(null));
  }

  @Override
  void decodePayload(ByteBuf payload, int payloadLength) {
    // no statement response
  }
}
