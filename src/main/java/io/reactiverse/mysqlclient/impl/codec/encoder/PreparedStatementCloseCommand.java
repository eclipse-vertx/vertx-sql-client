package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;

public class PreparedStatementCloseCommand extends MySQLCommandBase<Void> {
  private final long statementId;

  public PreparedStatementCloseCommand(long statementId) {
    super(CommandType.COM_STMT_CLOSE);
    this.statementId = statementId;
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writeStatementCloseMessage(statementId);
  }
}
