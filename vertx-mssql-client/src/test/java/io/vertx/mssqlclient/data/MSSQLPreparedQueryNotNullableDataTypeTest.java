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
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNotNullableDataTypeTest extends MSSQLNotNullableDataTypeTestBase {

  @Rule
  public RepeatRule rule = new RepeatRule();

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
  @Repeat(100)
  public void testEncodeTime(TestContext ctx) {
    LocalTime now = LocalTime.now();
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_time", now, row -> {
      ColumnChecker.checkColumn(0, "test_time")
        .returns(Tuple::getValue, Row::getValue, now)
        .returns(Tuple::getLocalTime, Row::getLocalTime, now)
        .returns(LocalTime.class, now)
        .forRow(row);
    });
  }

  @Test
  @Repeat(100)
  public void testEncodeSmallDateTime(TestContext ctx) {
    LocalDateTime now = LocalDateTime.now();
    // Seconds are rounded to the nearest minute
    int roundedUpMinute = (int) Math.floor(now.getSecond()/30.0);
    LocalDateTime convertedNow = now.withSecond(0).withNano(0).withMinute(now.getMinute() + roundedUpMinute);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_smalldatetime", now, row -> {
      ColumnChecker.checkColumn(0, "test_smalldatetime")
        .returns(Tuple::getValue, Row::getValue, convertedNow)
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, convertedNow)
        .returns(Tuple::getLocalDate, Row::getLocalDate, convertedNow.toLocalDate())
        .returns(Tuple::getLocalTime, Row::getLocalTime, convertedNow.toLocalTime())
        .returns(LocalDateTime.class, convertedNow)
        .forRow(row);
    });
  }

  @Test
  @Repeat(100)
  public void testEncodeDateTime(TestContext ctx) {
    final int nanosPerSecond = 1000000000;
    LocalDateTime now = LocalDateTime.now();

    // Reduce accuracy since datatype accuracy is rounded to increments of .000, .003, or .007 seconds   210000000
    int nanoOfDay = (int) Math.round(Math.round((now.getNano()/1000000d)/3.333333)*3.333333)*1000000;
    int secondOfDay = now.getSecond();

    // If nanoseconds is rounded up to one second
    if (nanoOfDay == nanosPerSecond) {
      nanoOfDay = 0;
      secondOfDay++;
    }

    LocalDateTime convertedNow = now.withNano(nanoOfDay).withSecond(secondOfDay);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetime", now, row -> {
      ColumnChecker.checkColumn(0, "test_datetime")
        .returns(Tuple::getValue, Row::getValue, convertedNow)
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, convertedNow)
        .returns(Tuple::getLocalDate, Row::getLocalDate, convertedNow.toLocalDate())
        .returns(Tuple::getLocalTime, Row::getLocalTime, convertedNow.toLocalTime())
        .returns(LocalDateTime.class, convertedNow)
        .forRow(row);
    });
  }

  @Test
  @Repeat(100)
  public void testEncodeDateTime2(TestContext ctx) {
    LocalDateTime now = LocalDateTime.now();
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetime2", now, row -> {
      ColumnChecker.checkColumn(0, "test_datetime2")
        .returns(Tuple::getValue, Row::getValue, now)
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, now)
        .returns(Tuple::getLocalDate, Row::getLocalDate, now.toLocalDate())
        .returns(Tuple::getLocalTime, Row::getLocalTime, now.toLocalTime())
        .returns(LocalDateTime.class, now)
        .forRow(row);
    });
  }

  @Test
  @Repeat(100)
  public void testEncodeOffsetDateTime(TestContext ctx) {
    /*
    Starting from Java 11, LocalDateTime.now() can have microseconds precision.
    But the test database defines the test_datetimeoffset column with scale of 5.

    Therefore, in order to get consistent behavior between Java 8 and Java 11,
    we erase the nanoOfSecond value obtained from the clock with a random number
    which has at most tens of microseconds precision.
     */
    int nanoOfSecond = ThreadLocalRandom.current().nextInt(100_000) * 10_000;
    OffsetDateTime now = LocalDateTime.now().withNano(nanoOfSecond)
      .atOffset(ZoneOffset.ofHoursMinutes(-3, -15));
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetimeoffset", now, row -> {
      ColumnChecker.checkColumn(0, "test_datetimeoffset")
        .returns(Tuple::getValue, Row::getValue, now)
        .returns(Tuple::getOffsetDateTime, Row::getOffsetDateTime, now)
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, now.toLocalDateTime())
        .returns(Tuple::getLocalDate, Row::getLocalDate, now.toLocalDate())
        .returns(Tuple::getLocalTime, Row::getLocalTime, now.toLocalTime())
        .returns(OffsetDateTime.class, now)
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
