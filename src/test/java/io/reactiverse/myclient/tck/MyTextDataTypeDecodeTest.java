package io.reactiverse.myclient.tck;

import io.reactiverse.myclient.junit.MyRule;
import io.reactiverse.sqlclient.TextDataTypeDecodeTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
  @ClassRule
  public static MyRule rule = new MyRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  public void testFloat4(TestContext ctx) {
    //TODO in MySQL query float type return double
    testDecodeGeneric(ctx, "test_float_4", Double.class, 3.4028235E38);
  }
}
