package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class SpatialTextCodecTest extends SpatialDataTypeCodecTestBase {
  @Override
  protected void testDecodeGeometry(TestContext ctx, String sql, Consumer<RowSet<Row>> checker) {
    testTextDecode(ctx, sql, checker);
  }
}
