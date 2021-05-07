package io.vertx.pgclient.tck;

import org.junit.ClassRule;
import org.junit.runner.RunWith;

import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;

import java.sql.JDBCType;

@RunWith(VertxUnitRunner.class)
public class PgBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  public PgBinaryDataTypeDecodeTest() {
    NUMERIC_TYPE = JDBCType.NUMERIC;
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }
}
