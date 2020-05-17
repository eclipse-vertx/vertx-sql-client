package io.vertx.mysqlclient.impl.util;

import io.vertx.sqlclient.TransactionOptions;

public class TransactionSqlBuilder {
  public static String buildStartTxSql(TransactionOptions txOptions) {
    if (txOptions.getTransactionAccessMode() != null) {
      return "START TRANSACTION " + txOptions.getTransactionAccessMode().literal();
    } else {
      return "START TRANSACTION";
    }
  }
}
