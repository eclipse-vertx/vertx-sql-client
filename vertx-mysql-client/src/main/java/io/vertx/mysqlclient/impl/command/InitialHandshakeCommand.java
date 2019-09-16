package io.vertx.mysqlclient.impl.command;

import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;

import java.util.Map;

public class InitialHandshakeCommand extends AuthenticationCommandBase<Connection> {
  private final SocketConnectionBase conn;
  private final SslMode sslMode;

  public InitialHandshakeCommand(SocketConnectionBase conn,
                                 String username,
                                 String password,
                                 String database,
                                 String collation,
                                 String serverRsaPublicKey,
                                 Map<String, String> connectionAttributes,
                                 SslMode sslMode) {
    super(username, password, database, collation, serverRsaPublicKey, connectionAttributes);
    this.conn = conn;
    this.sslMode = sslMode;
  }

  public SocketConnectionBase connection() {
    return conn;
  }

  public SslMode sslMode() {
    return sslMode;
  }
}
