package io.vertx.pgclient.impl.util;

import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;
import org.junit.Assert;
import org.junit.Test;

public class TransactionSqlBuilderTest {
  @Test
  public void testStartTxReadOnly() {
    String sql = TransactionSqlBuilder.buildStartTxSql(null, TransactionAccessMode.READ_ONLY);
    Assert.assertEquals("START TRANSACTION READ ONLY", sql);
  }

  @Test
  public void testStartTxSerializable() {
    String sql = TransactionSqlBuilder.buildStartTxSql(TransactionIsolationLevel.SERIALIZABLE, null);
    Assert.assertEquals("START TRANSACTION ISOLATION LEVEL SERIALIZABLE", sql);
  }

  @Test
  public void testStartTxCommittedReadAndReadOnly() {
    String sql = TransactionSqlBuilder.buildStartTxSql(TransactionIsolationLevel.SERIALIZABLE, TransactionAccessMode.READ_ONLY);
    Assert.assertEquals("START TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY", sql);
  }
}
