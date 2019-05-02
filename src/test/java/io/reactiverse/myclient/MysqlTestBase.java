package io.reactiverse.myclient;

import io.reactiverse.myclient.junit.MyRule;
import io.reactiverse.pgclient.PgConnectOptions;
import org.junit.BeforeClass;
import org.junit.ClassRule;

@Deprecated
public abstract class MysqlTestBase {

  @ClassRule
  public static MyRule rule = new MyRule();

  protected static PgConnectOptions options;

  @BeforeClass
  public static void before() {
    options = rule.options();
  }
}
