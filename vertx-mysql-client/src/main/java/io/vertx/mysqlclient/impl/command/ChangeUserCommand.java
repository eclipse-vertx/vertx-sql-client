package io.vertx.mysqlclient.impl.command;

import io.vertx.core.json.JsonObject;
import io.vertx.sqlclient.impl.command.CommandBase;

public class ChangeUserCommand extends CommandBase<Void> {
  //TODO support collation later
  private final String username;
  private final String password;
  private final String database;
  private final JsonObject connectionAttributes;

  public ChangeUserCommand(String username, String password, String database, JsonObject connectionAttributes) {
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

  public JsonObject connectionAttributes() {
    return connectionAttributes;
  }
}
