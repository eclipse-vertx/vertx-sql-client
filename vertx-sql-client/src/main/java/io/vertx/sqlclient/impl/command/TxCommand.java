package io.vertx.sqlclient.impl.command;

public class TxCommand extends CommandBase<Void> {

  public static final TxCommand BEGIN = new TxCommand("BEGIN");
  public static final TxCommand ROLLBACK = new TxCommand("ROLLBACK");
  public static final TxCommand COMMIT = new TxCommand("COMMIT");

  public final String sql;

  private TxCommand(String sql) {
    this.sql = sql;
  }
}
