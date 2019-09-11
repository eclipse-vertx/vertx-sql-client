package io.vertx.mysqlclient;

import io.vertx.ext.unit.TestContext;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.SqlClient;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MySQLTestBase {

  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  protected static MySQLConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }

  static void deleteFromMutableTable(TestContext ctx, SqlClient client, Runnable completionHandler) {
    client.query(
      "TRUNCATE TABLE mutable",
      ctx.asyncAssertSuccess(result -> completionHandler.run()));
  }
}
