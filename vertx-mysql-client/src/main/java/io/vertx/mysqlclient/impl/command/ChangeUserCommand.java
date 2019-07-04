package io.vertx.mysqlclient.impl.command;

import io.vertx.sqlclient.impl.command.CommandBase;

import java.util.Map;

public class ChangeUserCommand extends CommandBase<Void> {
  //TODO support collation later
  private final String username;
  private final String password;
  private final String database;
  private final Map<String, String> connectionAttributes;

  public ChangeUserCommand(String username, String password, String database, Map<String, String> connectionAttributes) {
    this.username = username;
    this.password = password;
    this.database = database;
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

  public Map<String, String> connectionAttributes() {
    return connectionAttributes;
  }
}
