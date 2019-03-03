package io.reactiverse.myclient.tck;

import io.reactiverse.myclient.junit.MyRule;
import io.reactiverse.sqlclient.ConnectionTestBase;
import org.junit.ClassRule;

public abstract class MyConnectionTestBase extends ConnectionTestBase {
  @ClassRule
  public static MyRule rule = new MyRule();

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
  }
}
