package io.vertx.sqlclient.impl.command;

import io.vertx.sqlclient.TransactionOptions;

public class StartTxCommand<R> extends TxCommand<R> {
  public TransactionOptions transactionOptions;

  public StartTxCommand(Kind kind, R result, TransactionOptions transactionOptions) {
    super(kind, result);
    this.transactionOptions = transactionOptions;
  }
}
