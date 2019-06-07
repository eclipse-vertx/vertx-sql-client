package io.vertx.mysqlclient.impl.command;

import io.vertx.mysqlclient.MySQLSetOption;
import io.vertx.sqlclient.impl.command.CommandBase;

public class SetOptionCommand extends CommandBase<Void> {
  private final MySQLSetOption mySQLSetOption;

  public SetOptionCommand(MySQLSetOption mySQLSetOption) {
    this.mySQLSetOption = mySQLSetOption;
  }

  public MySQLSetOption option() {
    return mySQLSetOption;
  }
}
