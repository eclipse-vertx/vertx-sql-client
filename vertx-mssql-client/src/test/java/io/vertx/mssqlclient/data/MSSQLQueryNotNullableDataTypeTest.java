package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLQueryNotNullableDataTypeTest extends MSSQLNotNullableDataTypeTestBase {
  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    testQueryDecodeGeneric(ctx, "not_nullable_datatype", columnName, "1", checker);
  }
}
