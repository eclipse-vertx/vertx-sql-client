package io.vertx.clickhousenativeclient.tck;

import io.vertx.clickhousenativeclient.ClickhouseResource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.tck.TextDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ClickhouseNativeTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
  @ClassRule
  public static ClickhouseResource rule = new ClickhouseResource();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Ignore
  @Test
  public void testTime(TestContext ctx) {
    //no time support
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testDecodeGeneric(ctx, "test_boolean", Byte.class, (byte)1);
  }
}
