package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;
import io.vertx.sqlclient.transaction.TransactionOptions;

public class StartTxCommand<R> extends TxCommand<R> {
  public final TransactionIsolationLevel isolationLevel;
  public final TransactionAccessMode accessMode;

  public StartTxCommand(Kind kind, R result, TransactionOptions transactionOptions) {
    super(kind, result);
    this.isolationLevel = transactionOptions.getIsolationLevel();
    this.accessMode = transactionOptions.getAccessMode();
  }
}
