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
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

public abstract class MSSQLNullableDataTypeTestBase extends MSSQLFullDataTypeTestBase {

  protected static final Short SHORT_NULL_VALUE = null;
  protected static final Integer INT_NULL_VALUE = null;
  protected static final Long LONG_NULL_VALUE = null;
  protected static final Float FLOAT_NULL_VALUE = null;
  protected static final Double DOUBLE_NULL_VALUE = null;
  protected static final Numeric NUMERIC_NULL_VALUE = null;
  protected static final Boolean BOOLEAN_NULL_VALUE = null;
  protected static final String STRING_NULL_VALUE = null;
  protected static final LocalDate LOCALDATE_NULL_VALUE = null;
  protected static final LocalTime LOCALTIME_NULL_VALUE = null;

  @Test
  public void testDecodeNullAllColumns(TestContext ctx) {
    testDecodeNullValue(ctx, "*", row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue("test_smallint"));
      ctx.assertEquals(null, row.getValue("test_int"));
      ctx.assertEquals(null, row.getValue("test_bigint"));
      ctx.assertEquals(null, row.getValue("test_float_4"));
      ctx.assertEquals(null, row.getValue("test_float_8"));
      ctx.assertEquals(null, row.getValue("test_numeric"));
      ctx.assertEquals(null, row.getValue("test_decimal"));
      ctx.assertEquals(null, row.getValue("test_boolean"));
      ctx.assertEquals(null, row.getValue("test_char"));
      ctx.assertEquals(null, row.getValue("test_varchar"));
      ctx.assertEquals(null, row.getValue("test_date"));
      ctx.assertEquals(null, row.getValue("test_time"));
    });
  }

  @Test
  public void testDecodeNullTinyInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_tinyint", row -> {
      ColumnChecker.checkColumn(0, "test_tinyint")
        .returns(Tuple::getValue, Row::getValue, SHORT_NULL_VALUE)
        .returns(Tuple::getShort, Row::getShort, SHORT_NULL_VALUE)
        .returns(Short.class, SHORT_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullSmallIntInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_smallint", row -> {
      ColumnChecker.checkColumn(0, "test_smallint")
        .returns(Tuple::getValue, Row::getValue, SHORT_NULL_VALUE)
        .returns(Tuple::getShort, Row::getShort, SHORT_NULL_VALUE)
        .returns(Short.class, SHORT_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_int", row -> {
      ColumnChecker.checkColumn(0, "test_int")
        .returns(Tuple::getValue, Row::getValue, INT_NULL_VALUE)
        .returns(Tuple::getInteger, Row::getInteger, INT_NULL_VALUE)
        .returns(Integer.class, INT_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullBigInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_bigint", row -> {
      ColumnChecker.checkColumn(0, "test_bigint")
        .returns(Tuple::getValue, Row::getValue, LONG_NULL_VALUE)
        .returns(Tuple::getLong, Row::getLong, LONG_NULL_VALUE)
        .returns(Long.class, LONG_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullFloat4(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_4", row -> {
      ColumnChecker.checkColumn(0, "test_float_4")
        .returns(Tuple::getValue, Row::getValue, FLOAT_NULL_VALUE)
        .returns(Tuple::getFloat, Row::getFloat, FLOAT_NULL_VALUE)
        .returns(Float.class, FLOAT_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullFloat8(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_8", row -> {
      ColumnChecker.checkColumn(0, "test_float_8")
        .returns(Tuple::getValue, Row::getValue, DOUBLE_NULL_VALUE)
        .returns(Tuple::getDouble, Row::getDouble, DOUBLE_NULL_VALUE)
        .returns(Double.class, DOUBLE_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullNumeric(TestContext ctx) {
    testDecodeNullValue(ctx, "test_numeric", row -> {
      ColumnChecker.checkColumn(0, "test_numeric")
        .returns(Tuple::getValue, Row::getValue, NUMERIC_NULL_VALUE)
        .returns(Numeric.class, NUMERIC_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDecimal(TestContext ctx) {
    testDecodeNullValue(ctx, "test_decimal", row -> {
      ColumnChecker.checkColumn(0, "test_decimal")
        .returns(Tuple::getValue, Row::getValue, NUMERIC_NULL_VALUE)
        .returns(Numeric.class, NUMERIC_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullBit(TestContext ctx) {
    testDecodeNullValue(ctx, "test_boolean", row -> {
      ColumnChecker.checkColumn(0, "test_boolean")
        .returns(Tuple::getValue, Row::getValue, BOOLEAN_NULL_VALUE)
        .returns(Tuple::getBoolean, Row::getBoolean, BOOLEAN_NULL_VALUE)
        .returns(Boolean.class, BOOLEAN_NULL_VALUE)
        .forRow(row);
    });
  }


  @Test
  public void testDecodeNullChar(TestContext ctx) {
    testDecodeNullValue(ctx, "test_char", row -> {
      ColumnChecker.checkColumn(0, "test_char")
        .returns(Tuple::getValue, Row::getValue, STRING_NULL_VALUE)
        .returns(Tuple::getString, Row::getString, STRING_NULL_VALUE)
        .returns(String.class, STRING_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullVarChar(TestContext ctx) {
    testDecodeNullValue(ctx, "test_varchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returns(Tuple::getValue, Row::getValue, STRING_NULL_VALUE)
        .returns(Tuple::getString, Row::getString, STRING_NULL_VALUE)
        .returns(String.class, STRING_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDate(TestContext ctx) {
    testDecodeNullValue(ctx, "test_varchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returns(Tuple::getValue, Row::getValue, LOCALDATE_NULL_VALUE)
        .returns(Tuple::getLocalDate, Row::getLocalDate, LOCALDATE_NULL_VALUE)
        .returns(LocalDate.class, LOCALDATE_NULL_VALUE)
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullTime(TestContext ctx) {
    testDecodeNullValue(ctx, "test_time", row -> {
      ColumnChecker.checkColumn(0, "test_time")
        .returns(Tuple::getValue, Row::getValue, LOCALTIME_NULL_VALUE)
        .returns(Tuple::getLocalTime, Row::getLocalTime, LOCALTIME_NULL_VALUE)
        .returns(LocalTime.class, LOCALTIME_NULL_VALUE)
        .forRow(row);
    });
  }

  private void testDecodeNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, true, columnName, checker);
  }
}
