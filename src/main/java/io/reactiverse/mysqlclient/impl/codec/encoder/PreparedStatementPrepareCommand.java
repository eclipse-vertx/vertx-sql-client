package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.MySQLPreparedStatement;
import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;

public class PreparedStatementPrepareCommand extends MySQLCommandBase<MySQLPreparedStatement> {
  private final String sql;

  public PreparedStatementPrepareCommand(String sql) {
    super(CommandType.COM_STMT_PREPARE);
    this.sql = sql;
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writePrepareMessage(sql);
  }
}
