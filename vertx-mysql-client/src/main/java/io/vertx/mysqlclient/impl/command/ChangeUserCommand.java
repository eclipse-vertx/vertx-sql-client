package io.vertx.mysqlclient.impl.command;

import io.vertx.sqlclient.impl.command.CommandBase;

public class ChangeUserCommand extends CommandBase<Void> {
  //TODO support charset and properties later
  private final String username;
  private final String password;
  private final String database;

  public ChangeUserCommand(String username, String password, String database) {
    this.username = username;
    this.password = password;
    this.database = database;
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
}
