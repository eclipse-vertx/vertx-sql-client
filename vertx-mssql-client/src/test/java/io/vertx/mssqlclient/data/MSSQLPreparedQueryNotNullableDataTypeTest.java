package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNotNullableDataTypeTest extends MSSQLNotNullableDataTypeTestBase {
  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_tinyint", (short) 255);
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_smallint", (short) -32768);
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_int", -2147483648);
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_bigint", -9223372036854775808L);
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    testEncodeNumber(ctx, "test_float_4", (float) -3.40282E38);
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    testEncodeNumber(ctx, "test_float_8", -1.7976931348623157E308);
  }

  @Test
  @Ignore //FIXME
  public void testEncodeNumeric(TestContext ctx) {
    testEncodeNumber(ctx, "test_numeric", Numeric.create(new BigDecimal("123456789.127")));
  }

  @Test
  @Ignore //FIXME
  public void testEncodeDecimal(TestContext ctx) {
    testEncodeNumber(ctx, "test_decimal", Numeric.create(new BigDecimal("123456789")));
  }

  @Test
  public void testEncodeBit(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_boolean", false, row -> {
      ColumnChecker.checkColumn(0, "test_boolean")
        .returns(Tuple::getValue, Row::getValue, false)
        .returns(Tuple::getBoolean, Row::getBoolean, false)
        .returns(Boolean.class, false)
        .forRow(row);
    });
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_char", "chartest", row -> {
      ColumnChecker.checkColumn(0, "test_char")
        .returns(Tuple::getValue, Row::getValue, "chartest")
        .returns(Tuple::getString, Row::getString, "chartest")
        .returns(String.class, "chartest")
        .forRow(row);
    });
  }

  @Test
  public void testEncodeVarChar(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_varchar", "testedvarchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returns(Tuple::getValue, Row::getValue, "testedvarchar")
        .returns(Tuple::getString, Row::getString, "testedvarchar")
        .returns(String.class, "testedvarchar")
        .forRow(row);
    });
  }

  @Test
  public void testEncodeDate(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_date", LocalDate.of(1999, 12, 31), row -> {
      ColumnChecker.checkColumn(0, "test_date")
        .returns(Tuple::getValue, Row::getValue, LocalDate.of(1999, 12, 31))
        .returns(Tuple::getLocalDate, Row::getLocalDate, LocalDate.of(1999, 12, 31))
        .returns(LocalDate.class, LocalDate.of(1999, 12, 31))
        .forRow(row);
    });
  }

  @Test
  public void testEncodeTime(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_time", LocalTime.of(23, 10, 45), row -> {
      ColumnChecker.checkColumn(0, "test_time")
        .returns(Tuple::getValue, Row::getValue, LocalTime.of(23, 10, 45))
        .returns(Tuple::getLocalTime, Row::getLocalTime, LocalTime.of(23, 10, 45))
        .returns(LocalTime.class, LocalTime.of(23, 10, 45))
        .forRow(row);
    });
  }

  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    testPreparedQueryDecodeGeneric(ctx, "not_nullable_datatype", columnName, "1", checker);
  }

  private void testEncodeNumber(TestContext ctx, String columnName, Number value) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", columnName, value, row -> {
      checkNumber(row, columnName, value);
    });
  }
}
