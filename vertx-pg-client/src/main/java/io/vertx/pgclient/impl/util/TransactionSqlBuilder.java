package io.vertx.pgclient.impl.util;

import io.vertx.sqlclient.TransactionOptions;

public class TransactionSqlBuilder {
  public static String buildStartTxSql(TransactionOptions txOptions) {
    boolean isTransactionModeExisted = false;

    StringBuilder txStartSqlBuilder = new StringBuilder("START TRANSACTION");
    if (txOptions.getTransactionAccessMode() != null) {
      txStartSqlBuilder.append(" ");
      txStartSqlBuilder.append(txOptions.getTransactionAccessMode().literal());
      isTransactionModeExisted = true;
    }
    if (txOptions.getTransactionIsolationLevel() != null) {
      if (isTransactionModeExisted) {
        txStartSqlBuilder.append(", ISOLATION LEVEL ");
      } else {
        txStartSqlBuilder.append(" ISOLATION LEVEL ");
        isTransactionModeExisted = true;
      }
      txStartSqlBuilder.append(txOptions.getTransactionIsolationLevel().literal());
    }
    return txStartSqlBuilder.toString();
  }
}
