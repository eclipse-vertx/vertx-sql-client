/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

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
public class MSSQLPreparedQueryNullableDataTypeTest extends MSSQLNullableDataTypeTestBase {

  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_tinyint", (short) 255);
  }

  @Test
  public void testEncodeNullTinyInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_tinyint", SHORT_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_tinyint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_smallint", (short) -32768);
  }

  @Test
  public void testEncodeNullSmallInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_smallint", SHORT_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_smallint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_int", -2147483648);
  }

  @Test
  public void testEncodeNullInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_int", INT_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_int")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testEncodeNumber(ctx, "test_bigint", -9223372036854775808L);
  }

  @Test
  public void testEncodeNullBigInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_bigint", LONG_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_bigint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    testEncodeNumber(ctx, "test_float_4", (float) -3.40282E38);
  }

  @Test
  public void testEncodeNullFloat4(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_float_4", FLOAT_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_float_4")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    testEncodeNumber(ctx, "test_float_8", -1.7976931348623157E308);
  }

  @Test
  public void testEncodeNullFloat8(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_float_8", DOUBLE_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_float_8")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeNumeric(TestContext ctx) {
    testEncodeNumber(ctx, "test_numeric", Numeric.create(123456789.13));
  }

  @Test
  public void testEncodeNullNumeric(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_numeric", NUMERIC_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_numeric")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeDecimal(TestContext ctx) {
    testEncodeNumber(ctx, "test_decimal", Numeric.create(123456789));
  }

  @Test
  public void testEncodeNullDecimal(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_decimal", NUMERIC_NULL_VALUE, row -> {
      ColumnChecker.checkColumn(0, "test_decimal")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testEncodeBit(TestContext ctx) {
    testEncodeBitValue(ctx, false);
  }

  @Test
  public void testEncodeNullBit(TestContext ctx) {
    testEncodeBitValue(ctx, BOOLEAN_NULL_VALUE);
  }

  private void testEncodeBitValue(TestContext ctx, Boolean value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_boolean", value, row -> {
      ColumnChecker checker = ColumnChecker.checkColumn(0, "test_boolean");
      if (value == null) {
        checker.returnsNull();
      } else {
        checker
          .returns(Tuple::getValue, Row::getValue, value)
          .returns(Tuple::getBoolean, Row::getBoolean, value)
          .returns(Boolean.class, value);
      }
      checker.forRow(row);
    });
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    testEncodeCharValue(ctx, "chartest");
  }

  @Test
  public void testEncodeNullChar(TestContext ctx) {
    testEncodeCharValue(ctx, STRING_NULL_VALUE);
  }

  private void testEncodeCharValue(TestContext ctx, String value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_char", value, row -> {
      ColumnChecker checker = ColumnChecker.checkColumn(0, "test_char");
      if (value == null) {
        checker.returnsNull();
      } else {
        checker
          .returns(Tuple::getValue, Row::getValue, value)
          .returns(Tuple::getString, Row::getString, value)
          .returns(String.class, value);
      }
      checker.forRow(row);
    });
  }

  @Test
  public void testEncodeVarChar(TestContext ctx) {
    testEncodeVarCharValue(ctx, "testedvarchar");
  }

  @Test
  public void testEncodeNullVarChar(TestContext ctx) {
    testEncodeVarCharValue(ctx, STRING_NULL_VALUE);
  }

  private void testEncodeVarCharValue(TestContext ctx, String value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_varchar", value, row -> {
      ColumnChecker checker = ColumnChecker.checkColumn(0, "test_varchar");
      if (value == null) {
        checker.returnsNull();
      } else {
        checker
          .returns(Tuple::getValue, Row::getValue, value)
          .returns(Tuple::getString, Row::getString, value)
          .returns(String.class, value);
      }
      checker.forRow(row);
    });
  }

  @Test
  public void testEncodeDate(TestContext ctx) {
    testEncodeDateValue(ctx, LocalDate.of(1999, 12, 31));
  }

  @Test
  public void testEncodeNullDate(TestContext ctx) {
    testEncodeDateValue(ctx, LOCALDATE_NULL_VALUE);
  }

  private void testEncodeDateValue(TestContext ctx, LocalDate value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_date", value, row -> {
      ColumnChecker checker = ColumnChecker.checkColumn(0, "test_date");
      if (value == null) {
        checker.returnsNull();
      } else {
        checker
          .returns(Tuple::getValue, Row::getValue, value)
          .returns(Tuple::getLocalDate, Row::getLocalDate, value)
          .returns(LocalDate.class, value);
      }
      checker.forRow(row);
    });
  }

  @Test
  public void testEncodeTime(TestContext ctx) {
    testEncodeTimeValue(ctx, LocalTime.of(23, 10, 45));
  }

  @Test
  public void testEncodeNullTime(TestContext ctx) {
    testEncodeTimeValue(ctx, LOCALTIME_NULL_VALUE);
  }

  private void testEncodeTimeValue(TestContext ctx, LocalTime value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_time", value, row -> {
      ColumnChecker checker = ColumnChecker.checkColumn(0, "test_time");
      if (value == null) {
        checker.returnsNull();
      } else {
        checker
          .returns(Tuple::getValue, Row::getValue, value)
          .returns(Tuple::getLocalTime, Row::getLocalTime, value)
          .returns(LocalTime.class, value);
      }
      checker.forRow(row);
    });
  }

  private void testEncodeNumber(TestContext ctx, String columnName, Number value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", columnName, value, row -> {
      checkNumber(row, columnName, value);
    });
  }

  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    if (isNull) {
      testPreparedQueryDecodeGeneric(ctx, "nullable_datatype", columnName, "3", checker);
    } else {
      testPreparedQueryDecodeGeneric(ctx, "nullable_datatype", columnName, "1", checker);
    }
  }
}
