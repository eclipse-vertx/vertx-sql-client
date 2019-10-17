package io.vertx.mssqlclient.impl.codec;

import io.vertx.mssqlclient.impl.protocol.TdsMessage;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.command.CommandResponse;
import io.vertx.sqlclient.impl.command.PrepareStatementCommand;

class PrepareStatementCodec extends MSSQLCommandCodec<PreparedStatement, PrepareStatementCommand> {
  PrepareStatementCodec(PrepareStatementCommand cmd) {
    super(cmd);
  }

  @Override
  void encode(TdsMessageEncoder encoder) {
    super.encode(encoder);
    // we use sp_prepexec instead of sp_prepare + sp_exec
    PreparedStatement preparedStatement = new MSSQLPreparedStatement(cmd.sql(), null);
    completionHandler.handle(CommandResponse.success(preparedStatement));

  }

  @Override
  void decodeMessage(TdsMessage message, TdsMessageEncoder encoder) {

  }
}
