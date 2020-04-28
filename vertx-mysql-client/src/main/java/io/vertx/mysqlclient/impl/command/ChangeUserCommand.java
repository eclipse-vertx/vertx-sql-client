package io.vertx.mysqlclient.impl.command;

import io.vertx.core.buffer.Buffer;
import io.vertx.mysqlclient.impl.MySQLCollation;

import java.util.Map;

public class ChangeUserCommand extends AuthenticationCommandBase<Void> {
  public ChangeUserCommand(String username,
                           String password,
                           String database,
                           MySQLCollation collation,
                           Buffer serverRsaPublicKey,
                           Map<String, String> connectionAttributes) {
    super(username, password, database, collation, serverRsaPublicKey, connectionAttributes);
  }
}
