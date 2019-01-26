package io.reactiverse.mysqlclient.impl.codec.encoder;

import io.reactiverse.mysqlclient.impl.codec.MySQLCommandBase;
import io.reactiverse.mysqlclient.impl.codec.MySQLPacketEncoder;
import io.reactiverse.mysqlclient.impl.protocol.CommandType;

public class PingCommand extends MySQLCommandBase<Void> {
  public PingCommand() {
    super(CommandType.COM_PING);
  }

  @Override
  public void exec(MySQLPacketEncoder out) {
    out.writePingMessage();
  }
}
