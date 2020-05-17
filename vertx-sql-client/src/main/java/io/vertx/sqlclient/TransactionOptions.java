package io.vertx.sqlclient;

import io.vertx.codegen.annotations.DataObject;

//TODO codegen
public class TransactionOptions {

  public static final TransactionOptions DEFAULT_TX_OPTIONS = new TransactionOptions();

  private TransactionIsolationLevel transactionIsolationLevel;
  private TransactionAccessMode transactionAccessMode;

  public TransactionOptions() {
  }

  public TransactionOptions(TransactionIsolationLevel transactionIsolationLevel, TransactionAccessMode transactionAccessMode) {
    this.transactionIsolationLevel = transactionIsolationLevel;
    this.transactionAccessMode = transactionAccessMode;
  }

  public TransactionOptions setTransactionAccessMode(TransactionAccessMode transactionAccessMode) {
    this.transactionAccessMode = transactionAccessMode;
    return this;
  }

  public TransactionAccessMode getTransactionAccessMode() {
    return transactionAccessMode;
  }

  public TransactionOptions setTransactionIsolationLevel(TransactionIsolationLevel transactionIsolationLevel) {
    this.transactionIsolationLevel = transactionIsolationLevel;
    return this;
  }

  public TransactionIsolationLevel getTransactionIsolationLevel() {
    return transactionIsolationLevel;
  }
}
