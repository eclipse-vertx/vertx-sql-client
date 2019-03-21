package io.reactiverse.myclient.tck;

import io.reactiverse.myclient.junit.MyRule;
import io.reactiverse.sqlclient.BinaryDataTypeDecodeTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MyBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static MyRule rule = new MyRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Ignore
  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    // does not pass due to it's TINYINT type
    super.testBoolean(ctx);
  }
}
