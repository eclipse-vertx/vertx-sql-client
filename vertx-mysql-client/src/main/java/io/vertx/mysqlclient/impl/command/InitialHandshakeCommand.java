package io.vertx.mysqlclient.impl.command;

import io.vertx.core.buffer.Buffer;
import io.vertx.mysqlclient.SslMode;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SocketConnectionBase;

import java.util.Map;

public class InitialHandshakeCommand extends AuthenticationCommandBase<Connection> {
  private final SocketConnectionBase conn;
  private final SslMode sslMode;
  private final int initialCapabilitiesFlags;

  public InitialHandshakeCommand(SocketConnectionBase conn,
                                 String username,
                                 String password,
                                 String database,
                                 String collation,
                                 Buffer serverRsaPublicKey,
                                 Map<String, String> connectionAttributes,
                                 SslMode sslMode,
                                 int initialCapabilitiesFlags) {
    super(username, password, database, collation, serverRsaPublicKey, connectionAttributes);
    this.conn = conn;
    this.sslMode = sslMode;
    this.initialCapabilitiesFlags = initialCapabilitiesFlags;
  }

  public SocketConnectionBase connection() {
    return conn;
  }

  public SslMode sslMode() {
    return sslMode;
  }

  public int initialCapabilitiesFlags() {
    return initialCapabilitiesFlags;
  }
}
