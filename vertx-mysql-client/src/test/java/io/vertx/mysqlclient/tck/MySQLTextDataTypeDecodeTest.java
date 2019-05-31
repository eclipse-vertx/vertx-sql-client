package io.vertx.mysqlclient.tck;

import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.TextDataTypeDecodeTestBase;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;

@RunWith(VertxUnitRunner.class)
public class MySQLTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
  @ClassRule
  public static MySQLRule rule = new MySQLRule();

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

  @Test
  @Override
  public void testTime(TestContext ctx) {
    // MySQL TIME type is mapped to java.time.Duration so we need to override here
    testDecodeGeneric(ctx, "test_time", Duration.class, Duration.ofHours(18).plusMinutes(45).plusSeconds(2));
  }
}
