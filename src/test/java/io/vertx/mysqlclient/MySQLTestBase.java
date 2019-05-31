package io.vertx.mysqlclient;

import io.vertx.mysqlclient.junit.MySQLRule;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MySQLTestBase {

  @ClassRule
  public static MySQLRule rule = new MySQLRule();

  protected static MySQLConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }
}
