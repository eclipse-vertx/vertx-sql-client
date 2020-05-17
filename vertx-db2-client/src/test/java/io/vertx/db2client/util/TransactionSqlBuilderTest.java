package io.vertx.db2client.util;

import io.vertx.db2client.impl.util.TransactionSqlBuilder;
import io.vertx.sqlclient.transaction.TransactionAccessMode;
import io.vertx.sqlclient.transaction.TransactionIsolationLevel;
import org.junit.Assert;
import org.junit.Test;

public class TransactionSqlBuilderTest {
  @Test
  public void testSetReadCommitted() {
    String sql = TransactionSqlBuilder.buildSetTxIsolationLevelSql(TransactionIsolationLevel.READ_COMMITTED, null);
    Assert.assertEquals("SET TRANSACTION ISOLATION LEVEL READ COMMITTED" ,sql);
  }

  @Test
  public void testSetReadOnly() {
    String sql = TransactionSqlBuilder.buildSetTxIsolationLevelSql(null, TransactionAccessMode.READ_ONLY);
    Assert.assertEquals("SET TRANSACTION READ ONLY" ,sql);
  }

  @Test
  public void testSerializableReadOnly() {
    String sql = TransactionSqlBuilder.buildSetTxIsolationLevelSql(TransactionIsolationLevel.SERIALIZABLE, TransactionAccessMode.READ_ONLY);
    Assert.assertEquals("SET TRANSACTION ISOLATION LEVEL SERIALIZABLE, READ ONLY" ,sql);
  }
}
