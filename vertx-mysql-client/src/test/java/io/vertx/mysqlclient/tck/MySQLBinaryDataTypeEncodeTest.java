package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.Duration;

@RunWith(VertxUnitRunner.class)
public class MySQLBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("UPDATE basicdatatype SET test_boolean = ? WHERE id = 2").execute(Tuple.tuple().addValue(true), ctx.asyncAssertSuccess(updateResult -> {
        conn.preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 2").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(true, row.getBoolean(0));
          ctx.assertEquals(true, row.getBoolean("test_boolean"));
          ctx.assertEquals((byte) 1, row.getValue(0));
          ctx.assertEquals((byte) 1, row.getValue("test_boolean"));
        }));
      }));
    }));
  }

  @Test
  @Override
  public void testTime(TestContext ctx) {
    // MySQL TIME type is mapped to java.time.Duration so we need to override here
    testEncodeGeneric(ctx, "test_time", Duration.class, null, Duration.ofHours(18).plusMinutes(45).plusSeconds(2));
  }
}
