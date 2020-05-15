package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNotNullableDataTypeTest extends MSSQLNotNullableDataTypeTestBase {
  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_tinyint", (short) 255, row -> {
      ctx.assertEquals((short) 255, row.getValue("test_tinyint"));
      ctx.assertEquals((short) 255, row.getValue(0));
      ctx.assertEquals((short) 255, row.get(Short.class, "test_tinyint"));
      ctx.assertEquals((short) 255, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_smallint", (short) -32768, row -> {
      ctx.assertEquals((short) -32768, row.getValue("test_smallint"));
      ctx.assertEquals((short) -32768, row.getValue(0));
      ctx.assertEquals((short) -32768, row.getShort("test_smallint"));
      ctx.assertEquals((short) -32768, row.getShort(0));
      ctx.assertEquals((short) -32768, row.get(Short.class, "test_smallint"));
      ctx.assertEquals((short) -32768, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_int", -2147483648, row -> {
      ctx.assertEquals(-2147483648, row.getValue("test_int"));
      ctx.assertEquals(-2147483648, row.getValue(0));
      ctx.assertEquals(-2147483648, row.getInteger("test_int"));
      ctx.assertEquals(-2147483648, row.getInteger(0));
      ctx.assertEquals(-2147483648, row.get(Integer.class, "test_int"));
      ctx.assertEquals(-2147483648, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_bigint", -9223372036854775808L, row -> {
      ctx.assertEquals(-9223372036854775808L, row.getValue("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getValue(0));
      ctx.assertEquals(-9223372036854775808L, row.getLong("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getLong(0));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, 0));
    });
  }

  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    testPreparedQueryDecodeGeneric(ctx, "not_nullable_datatype", columnName, "1", checker);
  }
}
