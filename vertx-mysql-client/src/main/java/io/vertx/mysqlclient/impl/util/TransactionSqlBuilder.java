package io.vertx.mysqlclient.impl.util;

import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;

public class TransactionSqlBuilder {
  private static final String START_TX_DEFAULT = "START TRANSACTION";
  private static final String SET_ISOLATION = "SET TRANSACTION";

  private static final String PREDEFINED_TX_REPEATABLE_READ = " ISOLATION LEVEL REPEATABLE READ";
  private static final String PREDEFINED_TX_SERIALIZABLE = " ISOLATION LEVEL SERIALIZABLE";
  private static final String PREDEFINED_TX_READ_COMMITTED = " ISOLATION LEVEL READ COMMITTED";
  private static final String PREDEFINED_TX_READ_UNCOMMITTED = " ISOLATION LEVEL READ UNCOMMITTED";


  private static final String PREDEFINED_TX_RW = " READ WRITE";
  private static final String PREDEFINED_TX_RO = " READ ONLY";


  public static String buildStartTxSql(TransactionAccessMode accessMode) {
    if (accessMode == TransactionAccessMode.READ_ONLY) {
      return START_TX_DEFAULT + PREDEFINED_TX_RO;
    } else if (accessMode == TransactionAccessMode.READ_WRITE) {
      return START_TX_DEFAULT + PREDEFINED_TX_RW;
    } else {
      return START_TX_DEFAULT;
    }
  }

  public static String buildSetTxIsolationLevelSql(TransactionIsolationLevel isolationLevel) {
    if (isolationLevel == TransactionIsolationLevel.READ_UNCOMMITTED) {
      return SET_ISOLATION + PREDEFINED_TX_READ_UNCOMMITTED;
    } else if (isolationLevel == TransactionIsolationLevel.READ_COMMITTED) {
      return SET_ISOLATION + PREDEFINED_TX_READ_COMMITTED;
    } else if (isolationLevel == TransactionIsolationLevel.REPEATABLE_READ) {
      return SET_ISOLATION + PREDEFINED_TX_REPEATABLE_READ;
    } else {
      return SET_ISOLATION + PREDEFINED_TX_SERIALIZABLE;
    }
  }
}
