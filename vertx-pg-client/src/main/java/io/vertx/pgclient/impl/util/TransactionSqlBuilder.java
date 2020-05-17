package io.vertx.pgclient.impl.util;

import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;

public class TransactionSqlBuilder {
  private static final String START_TX_DEFAULT = "START TRANSACTION";

  private static final String PREDEFINED_TX_REPEATABLE_READ = " ISOLATION LEVEL REPEATABLE READ";
  private static final String PREDEFINED_TX_SERIALIZABLE = " ISOLATION LEVEL SERIALIZABLE";
  private static final String PREDEFINED_TX_READ_COMMITTED = " ISOLATION LEVEL READ COMMITTED";
  private static final String PREDEFINED_TX_READ_UNCOMMITTED = " ISOLATION LEVEL READ UNCOMMITTED";


  private static final String PREDEFINED_TX_RW = " READ WRITE";
  private static final String PREDEFINED_TX_RO = " READ ONLY";

  public static String buildStartTxSql(TransactionIsolationLevel isolationLevel, TransactionAccessMode accessMode) {
    boolean isCharacteristicExisted = false;
    StringBuilder sqlBuilder = new StringBuilder(START_TX_DEFAULT);

    if (isolationLevel != null) {
      switch (isolationLevel) {
        case READ_UNCOMMITTED:
          sqlBuilder.append(PREDEFINED_TX_READ_UNCOMMITTED);
          break;
        case READ_COMMITTED:
          sqlBuilder.append(PREDEFINED_TX_READ_COMMITTED);
          break;
        case REPEATABLE_READ:
          sqlBuilder.append(PREDEFINED_TX_REPEATABLE_READ);
          break;
        case SERIALIZABLE:
          sqlBuilder.append(PREDEFINED_TX_SERIALIZABLE);
          break;
      }
      isCharacteristicExisted = true;
    }

    if (accessMode != null) {
      if (isCharacteristicExisted) {
        sqlBuilder.append(',');
      } else {
        isCharacteristicExisted = true;
      }
      switch (accessMode) {
        case READ_ONLY:
          sqlBuilder.append(PREDEFINED_TX_RO);
          break;
        case READ_WRITE:
          sqlBuilder.append(PREDEFINED_TX_RW);
          break;
      }
    }

    return sqlBuilder.toString();
  }
}
