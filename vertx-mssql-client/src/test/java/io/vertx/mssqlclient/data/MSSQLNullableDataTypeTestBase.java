/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.data;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import org.junit.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.util.function.Consumer;

public abstract class MSSQLNullableDataTypeTestBase extends MSSQLFullDataTypeTestBase {

  protected static final Short SHORT_NULL_VALUE = null;
  protected static final Integer INT_NULL_VALUE = null;
  protected static final Long LONG_NULL_VALUE = null;
  protected static final Float FLOAT_NULL_VALUE = null;
  protected static final Double DOUBLE_NULL_VALUE = null;
  protected static final BigDecimal DECIMAL_NULL_VALUE = null;
  protected static final Boolean BOOLEAN_NULL_VALUE = null;
  protected static final String STRING_NULL_VALUE = null;
  protected static final LocalDate LOCALDATE_NULL_VALUE = null;
  protected static final LocalTime LOCALTIME_NULL_VALUE = null;
  protected static final LocalDateTime LOCALDATETIME_NULL_VALUE = null;
  protected static final OffsetDateTime OFFSETDATETIME_NULL_VALUE = null;
  protected static final Buffer BUFFER_NULL_VALUE = null;

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
      ctx.assertEquals(null, row.getValue("test_smalldatetime"));
      ctx.assertEquals(null, row.getValue("test_datetime"));
      ctx.assertEquals(null, row.getValue("test_datetime2"));
      ctx.assertEquals(null, row.getValue("test_datetimeoffset"));
      ctx.assertEquals(null, row.getValue("test_binary"));
      ctx.assertEquals(null, row.getValue("test_varbinary"));
      ctx.assertEquals(null, row.getValue("test_money"));
      ctx.assertEquals(null, row.getValue("test_smallmoney"));
    });
  }

  @Test
  public void testDecodeNullTinyInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_tinyint", row -> {
      ColumnChecker.checkColumn(0, "test_tinyint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullSmallIntInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_smallint", row -> {
      ColumnChecker.checkColumn(0, "test_smallint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_int", row -> {
      ColumnChecker.checkColumn(0, "test_int")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullBigInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_bigint", row -> {
      ColumnChecker.checkColumn(0, "test_bigint")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullFloat4(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_4", row -> {
      ColumnChecker.checkColumn(0, "test_float_4")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullFloat8(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_8", row -> {
      ColumnChecker.checkColumn(0, "test_float_8")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullNumeric(TestContext ctx) {
    testDecodeNullValue(ctx, "test_numeric", row -> {
      ColumnChecker.checkColumn(0, "test_numeric")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDecimal(TestContext ctx) {
    testDecodeNullValue(ctx, "test_decimal", row -> {
      ColumnChecker.checkColumn(0, "test_decimal")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullBit(TestContext ctx) {
    testDecodeNullValue(ctx, "test_boolean", row -> {
      ColumnChecker.checkColumn(0, "test_boolean")
        .returnsNull()
        .forRow(row);
    });
  }


  @Test
  public void testDecodeNullChar(TestContext ctx) {
    testDecodeNullValue(ctx, "test_char", row -> {
      ColumnChecker.checkColumn(0, "test_char")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullVarChar(TestContext ctx) {
    testDecodeNullValue(ctx, "test_varchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDate(TestContext ctx) {
    testDecodeNullValue(ctx, "test_varchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullTime(TestContext ctx) {
    testDecodeNullValue(ctx, "test_time", row -> {
      ColumnChecker.checkColumn(0, "test_time")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDateTime(TestContext ctx) {
    testDecodeNullValue(ctx, "test_datetime", row -> {
      ColumnChecker.checkColumn(0, "test_datetime")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullSmallDateTime(TestContext ctx) {
    testDecodeNullValue(ctx, "test_smalldatetime", row -> {
      ColumnChecker.checkColumn(0, "test_smalldatetime")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullDateTime2(TestContext ctx) {
    testDecodeNullValue(ctx, "test_datetime2", row -> {
      ColumnChecker.checkColumn(0, "test_datetime2")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullOffsetDateTime(TestContext ctx) {
    testDecodeNullValue(ctx, "test_datetimeoffset", row -> {
      ColumnChecker.checkColumn(0, "test_datetimeoffset")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullBinary(TestContext ctx) {
    testDecodeNullValue(ctx, "test_binary", row -> {
      ColumnChecker.checkColumn(0, "test_binary")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullVarBinary(TestContext ctx) {
    testDecodeNullValue(ctx, "test_varbinary", row -> {
      ColumnChecker.checkColumn(0, "test_varbinary")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullMoney(TestContext ctx) {
    testDecodeNullValue(ctx, "test_money", row -> {
      ColumnChecker.checkColumn(0, "test_money")
        .returnsNull()
        .forRow(row);
    });
  }

  @Test
  public void testDecodeNullSmallMoney(TestContext ctx) {
    testDecodeNullValue(ctx, "test_smallmoney", row -> {
      ColumnChecker.checkColumn(0, "test_smallmoney")
        .returnsNull()
        .forRow(row);
    });
  }

  private void testDecodeNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, true, columnName, checker);
  }
}
