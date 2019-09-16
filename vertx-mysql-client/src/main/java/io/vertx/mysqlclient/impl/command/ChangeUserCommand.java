package io.vertx.mysqlclient.impl.command;

import java.util.Map;

public class ChangeUserCommand extends AuthenticationCommandBase<Void> {
  public ChangeUserCommand(String username,
                           String password,
                           String database,
                           String collation,
                           String serverRsaPublicKey,
                           Map<String, String> connectionAttributes) {
    super(username, password, database, collation, serverRsaPublicKey, connectionAttributes);
  }
}
