package io.vertx.mssqlclient;

import io.vertx.mssqlclient.junit.MSSQLRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MSSQLTestBase {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  protected static MSSQLConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }
}
