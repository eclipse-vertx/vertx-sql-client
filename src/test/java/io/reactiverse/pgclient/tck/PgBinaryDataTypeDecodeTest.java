package io.reactiverse.pgclient.tck;

import io.reactiverse.pgclient.junit.PgRule;
import io.reactiverse.sqlclient.BinaryDataTypeDecodeTestBase;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.ClassRule;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class PgBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static PgRule rule = new PgRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
