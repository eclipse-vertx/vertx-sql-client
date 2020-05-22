package io.vertx.mysqlclient.impl.util;

import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;
import org.junit.Assert;
import org.junit.Test;

public class TransactionSqlBuilderTest {
  @Test
  public void testBuildSetIsolationLevel() {
    String sql = TransactionSqlBuilder.buildSetTxIsolationLevelSql(TransactionIsolationLevel.SERIALIZABLE);
    Assert.assertEquals("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE", sql);
  }

  @Test
  public void testStartReadOnlyTx() {
    String sql = TransactionSqlBuilder.buildStartTxSql(TransactionAccessMode.READ_ONLY);
    Assert.assertEquals("START TRANSACTION READ ONLY", sql);
  }

  @Test
  public void testStartDefaultTx() {
    String sql = TransactionSqlBuilder.buildStartTxSql(null);
    Assert.assertEquals("START TRANSACTION", sql);
  }
}
