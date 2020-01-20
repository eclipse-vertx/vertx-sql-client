package io.vertx.pgclient.tck;

import io.vertx.pgclient.junit.PgRule;
import io.vertx.sqlclient.tck.TextDataTypeDecodeTestBase;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
  @ClassRule
  public static PgRule rule = PgRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
