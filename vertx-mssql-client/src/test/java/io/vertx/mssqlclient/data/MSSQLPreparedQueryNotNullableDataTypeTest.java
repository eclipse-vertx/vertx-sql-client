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
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.*;
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
  public void testEncodeNumeric(TestContext ctx) {
    testEncodeNumber(ctx, "test_numeric", new BigDecimal("-123.13"));
  }

  @Test
  public void testEncodeDecimal(TestContext ctx) {
    testEncodeNumber(ctx, "test_decimal", new BigDecimal("123456789"));
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

  @Test
  public void testEncodeDateTime(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetime2", LocalDateTime.of(1999, 12, 31, 23, 10, 45), row -> {
      ColumnChecker.checkColumn(0, "test_datetime2")
        .returns(Tuple::getValue, Row::getValue, LocalDateTime.of(1999, 12, 31, 23, 10, 45))
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, LocalDateTime.of(1999, 12, 31, 23, 10, 45))
        .returns(Tuple::getLocalDate, Row::getLocalDate, LocalDate.of(1999, 12, 31))
        .returns(Tuple::getLocalTime, Row::getLocalTime, LocalTime.of(23, 10, 45))
        .returns(LocalDateTime.class, LocalDateTime.of(1999, 12, 31, 23, 10, 45))
        .forRow(row);
    });
  }

  @Test
  public void testEncodeOffsetDateTime(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetimeoffset", LocalDateTime.of(1999, 12, 31, 23, 10, 45).atOffset(ZoneOffset.ofHoursMinutes(-3, -15)), row -> {
      ColumnChecker.checkColumn(0, "test_datetimeoffset")
        .returns(Tuple::getValue, Row::getValue, LocalDateTime.of(1999, 12, 31, 23, 10, 45).atOffset(ZoneOffset.ofHoursMinutes(-3, -15)))
        .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, LocalDateTime.of(1999, 12, 31, 23, 10, 45).atOffset(ZoneOffset.ofHoursMinutes(-3, -15)))
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, LocalDateTime.of(1999, 12, 31, 23, 10, 45))
        .returns(Tuple::getLocalDate, Row::getLocalDate, LocalDate.of(1999, 12, 31))
        .returns(Tuple::getLocalTime, Row::getLocalTime, LocalTime.of(23, 10, 45))
        .returns(OffsetDateTime.class, LocalDateTime.of(1999, 12, 31, 23, 10, 45).atOffset(ZoneOffset.ofHoursMinutes(-3, -15)))
        .forRow(row);
    });
  }

  @Test
  public void testEncodeBinary(TestContext ctx) {
    String str = "john doe";
    Buffer param = Buffer.buffer(str);
    Buffer expected = Buffer.buffer(str).appendBytes(new byte[20 - str.length()]);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_binary", param, row -> {
      ColumnChecker.checkColumn(0, "test_binary")
        .returns(Tuple::getValue, Row::getValue, expected)
        .returns(Tuple::getBuffer, Row::getBuffer, expected)
        .returns(Buffer.class, expected)
        .forRow(row);
    });
  }

  @Test
  public void testEncodeVarBinary(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_varbinary", Buffer.buffer("ninja plumber"), row -> {
      ColumnChecker.checkColumn(0, "test_varbinary")
        .returns(Tuple::getValue, Row::getValue, Buffer.buffer("ninja plumber"))
        .returns(Tuple::getBuffer, Row::getBuffer, Buffer.buffer("ninja plumber"))
        .returns(Buffer.class, Buffer.buffer("ninja plumber"))
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
