package io.reactiverse.mysqlclient2;

import io.reactiverse.myclient.support.MyRule;
import io.reactiverse.pgclient.PgConnectOptions;
import org.junit.BeforeClass;
import org.junit.ClassRule;

public abstract class MysqlTestBase {

  @ClassRule
  public static MyRule rule = new MyRule();

  protected static PgConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }
}
