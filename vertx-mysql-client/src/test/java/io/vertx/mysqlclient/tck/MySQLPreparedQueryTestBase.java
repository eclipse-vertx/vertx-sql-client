package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

public abstract class MySQLPreparedQueryTestBase extends PreparedQueryTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test
  @Ignore
  @Override
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    // Does not pass, we can't achieve this feature on MySQL for now, see io.vertx.mysqlclient.impl.codec.MySQLParamDesc#prepare for reasons.
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }
}
