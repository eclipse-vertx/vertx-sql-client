package io.vertx.pgclient.tck;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;

@RunWith(VertxUnitRunner.class)
public class PgBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
