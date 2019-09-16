package io.vertx.mysqlclient.impl.command;

import io.vertx.sqlclient.impl.command.CommandBase;

import java.util.Map;

public class AuthenticationCommandBase<R> extends CommandBase<R> {
  private final String username;
  private final String password;
  private final String database;
  private final String collation;
  private final String serverRsaPublicKey;
  private final Map<String, String> connectionAttributes;

  public AuthenticationCommandBase(String username, String password, String database, String collation, String serverRsaPublicKey, Map<String, String> connectionAttributes) {
    this.username = username;
    this.password = password;
    this.database = database;
    this.collation = collation;
    this.serverRsaPublicKey = serverRsaPublicKey;
    this.connectionAttributes = connectionAttributes;
  }

  public String username() {
    return username;
  }

  public String password() {
    return password;
  }

  public String database() {
    return database;
  }

  public String collation() {
    return collation;
  }

  public String serverRsaPublicKey() {
    return serverRsaPublicKey;
  }

  public Map<String, String> connectionAttributes() {
    return connectionAttributes;
  }
}
