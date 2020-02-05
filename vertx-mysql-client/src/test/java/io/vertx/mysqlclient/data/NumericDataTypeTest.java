package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;

@RunWith(VertxUnitRunner.class)
public class NumericDataTypeTest extends MySQLDataTypeTestBase {
  @Test
  public void testBinaryEncodeCastShortToDecimal(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND test_decimal = ?", Tuple.of((short) 12345), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      ctx.assertEquals(Numeric.create(12345), row.getValue("test_decimal"));
    });
  }

  @Test
  public void testBinaryEncodeCastLongToShort(TestContext ctx) {
    testBinaryDecode(ctx, "SELECT * FROM basicdatatype WHERE id = 1 AND test_int_2 = ?", Tuple.of(32767L), result -> {
      ctx.assertEquals(1, result.size());
      RowIterator<Row> iterator = result.iterator();
      Row row = iterator.next();
      ctx.assertEquals(1, row.getInteger("id"));
      ctx.assertEquals((short) 32767, row.getValue("test_int_2"));
    });
  }

  @Test
  public void testBinaryEncodeBigDecimal(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_bigint", BigDecimal.valueOf(999999999L), (row, columnName) -> {
      ctx.assertEquals(999999999L, row.getLong(columnName));
      ctx.assertEquals(999999999L, row.getLong(columnName));
      ctx.assertEquals(BigDecimal.valueOf(999999999L), row.getBigDecimal(columnName));
      ctx.assertEquals(BigDecimal.valueOf(999999999L), row.getBigDecimal(columnName));
      ctx.assertEquals(Numeric.parse("999999999"), row.getValue(0));
      ctx.assertEquals(Numeric.parse("999999999"), row.getValue(columnName));
    });
  }

  @Test
  public void testBinaryDecodeUnsignedTinyInt(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_unsigned_tinyint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Short);
      ctx.assertEquals((short) 255, row.getValue(0));
      ctx.assertEquals((short) 255, row.getValue(columnName));
      ctx.assertEquals((short) 255, row.getShort(0));
      ctx.assertEquals((short) 255, row.getShort(columnName));
      ctx.assertEquals(255, row.getInteger(0));
      ctx.assertEquals(255, row.getInteger(columnName));
      ctx.assertEquals(255L, row.getLong(0));
      ctx.assertEquals(255L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(255), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(255), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testBinaryEncodeUnsignedTinyInt(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_tinyint", (short) 128);
  }

  @Test
  public void testBinaryDecodeUnsignedSmallInt(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_unsigned_smallint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Integer);
      ctx.assertEquals(65535, row.getValue(0));
      ctx.assertEquals(65535, row.getValue(columnName));
      ctx.assertEquals(65535, row.getInteger(0));
      ctx.assertEquals(65535, row.getInteger(columnName));
      ctx.assertEquals(65535L, row.getLong(0));
      ctx.assertEquals(65535L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(65535), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(65535), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testBinaryEncodeUnsignedSmallInt(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_smallint", 32768);
  }

  @Test
  public void testBinaryDecodeUnsignedMediumInt(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_unsigned_mediumint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Integer);
      ctx.assertEquals(16777215, row.getValue(0));
      ctx.assertEquals(16777215, row.getValue(columnName));
      ctx.assertEquals(16777215, row.getInteger(0));
      ctx.assertEquals(16777215, row.getInteger(columnName));
      ctx.assertEquals(16777215L, row.getLong(0));
      ctx.assertEquals(16777215L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(16777215), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(16777215), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testBinaryEncodeUnsignedMediumInt(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_mediumint", 8388608);
  }

  @Test
  public void testBinaryDecodeUnsignedInt(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_unsigned_int", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Long);
      ctx.assertEquals(4294967295L, row.getValue(0));
      ctx.assertEquals(4294967295L, row.getValue(columnName));
      ctx.assertEquals(4294967295L, row.getLong(0));
      ctx.assertEquals(4294967295L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(4294967295L), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(4294967295L), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testBinaryEncodeUnsignedInt(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_int", 2147483648L);
  }

  @Test
  public void testBinaryDecodeUnsignedBigInt(TestContext ctx) {
    testBinaryDecodeGenericWithTable(ctx, "test_unsigned_bigint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Numeric);
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.getValue(0));
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.getValue(columnName));
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.get(Numeric.class, 0));
      ctx.assertEquals(new BigDecimal("18446744073709551615"), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal("18446744073709551615"), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testBinaryEncodeUnsignedBigInt(TestContext ctx) {
    testBinaryEncodeGeneric(ctx, "test_unsigned_bigint", Numeric.parse("9223372036854775808"));
  }

  @Test
  public void testTextDecodeUnsignedTinyInt(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_unsigned_tinyint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Short);
      ctx.assertEquals((short) 255, row.getValue(0));
      ctx.assertEquals((short) 255, row.getValue(columnName));
      ctx.assertEquals((short) 255, row.getShort(0));
      ctx.assertEquals((short) 255, row.getShort(columnName));
      ctx.assertEquals(255, row.getInteger(0));
      ctx.assertEquals(255, row.getInteger(columnName));
      ctx.assertEquals(255L, row.getLong(0));
      ctx.assertEquals(255L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(255), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(255), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testTextDecodeUnsignedSmallInt(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_unsigned_smallint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Integer);
      ctx.assertEquals(65535, row.getValue(0));
      ctx.assertEquals(65535, row.getValue(columnName));
      ctx.assertEquals(65535, row.getInteger(0));
      ctx.assertEquals(65535, row.getInteger(columnName));
      ctx.assertEquals(65535L, row.getLong(0));
      ctx.assertEquals(65535L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(65535), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(65535), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testTextDecodeUnsignedMediumInt(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_unsigned_mediumint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Integer);
      ctx.assertEquals(16777215, row.getValue(0));
      ctx.assertEquals(16777215, row.getValue(columnName));
      ctx.assertEquals(16777215, row.getInteger(0));
      ctx.assertEquals(16777215, row.getInteger(columnName));
      ctx.assertEquals(16777215L, row.getLong(0));
      ctx.assertEquals(16777215L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(16777215), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(16777215), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testTextDecodeUnsignedInt(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_unsigned_int", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Long);
      ctx.assertEquals(4294967295L, row.getValue(0));
      ctx.assertEquals(4294967295L, row.getValue(columnName));
      ctx.assertEquals(4294967295L, row.getLong(0));
      ctx.assertEquals(4294967295L, row.getLong(columnName));
      ctx.assertEquals(new BigDecimal(4294967295L), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal(4294967295L), row.getBigDecimal(columnName));
    }));
  }

  @Test
  public void testTextDecodeUnsignedBigInt(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_unsigned_bigint", ((row, columnName) -> {
      ctx.assertTrue(row.getValue(0) instanceof Numeric);
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.getValue(0));
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.getValue(columnName));
      ctx.assertEquals(Numeric.parse("18446744073709551615"), row.get(Numeric.class, 0));
      ctx.assertEquals(new BigDecimal("18446744073709551615"), row.getBigDecimal(0));
      ctx.assertEquals(new BigDecimal("18446744073709551615"), row.getBigDecimal(columnName));
    }));
  }
}
