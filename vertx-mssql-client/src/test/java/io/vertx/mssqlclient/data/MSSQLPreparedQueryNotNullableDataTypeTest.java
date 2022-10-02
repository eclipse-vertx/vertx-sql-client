/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
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
  public void testEncodeVarCharMax(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_varchar_max", "testedvarchar", row -> {
      ColumnChecker.checkColumn(0, "test_varchar_max")
        .returns(Tuple::getValue, Row::getValue, "testedvarchar")
        .returns(Tuple::getString, Row::getString, "testedvarchar")
        .returns(String.class, "testedvarchar")
        .forRow(row);
    });
  }

  @Test
  public void testEncodeText(TestContext ctx) {
    String bigString = createBigString(false);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_text", bigString, row -> {
      ColumnChecker.checkColumn(0, "test_text")
        .returns(Tuple::getValue, Row::getValue, bigString)
        .returns(Tuple::getString, Row::getString, bigString)
        .returns(String.class, bigString)
        .forRow(row);
    });
  }

  private static String createBigString(boolean withNonLatinCharacters) {
    StringBuilder sb = new StringBuilder(10000);
    while (sb.length() < 10000) {
      sb.append("ae $ \u20AC iou y \u00E9\u00E8 %\u00FB* <> '");
      if (withNonLatinCharacters) {
        sb.append(" \u30D5\u30EC\u30FC\u30E0\u30EF\u30FC\u30AF\u306E\u30D9\u30F3\u30C1\u30DE\u30FC\u30AF ");
      }
    }
    return sb.toString();
  }

  @Test
  public void testEncodeNText(TestContext ctx) {
    String bigString = createBigString(true);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_ntext", bigString, row -> {
      ColumnChecker.checkColumn(0, "test_ntext")
        .returns(Tuple::getValue, Row::getValue, bigString)
        .returns(Tuple::getString, Row::getString, bigString)
        .returns(String.class, bigString)
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
    // Make sure the number of significant digits matches the column precision
    int nanoOfSecond = 1_000 * ThreadLocalRandom.current().nextInt(1_000_000);
    LocalTime now = LocalTime.now().withNano(nanoOfSecond);
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
    int roundedUpMinute = now.getSecond() < 30 ? 0 : 1;
    int plusMinutes = now.getMinute() + roundedUpMinute;
    LocalDateTime convertedNow = now.withSecond(0).withNano(0).withMinute(0).plusMinutes(plusMinutes);
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

    // Reduce accuracy since datatype accuracy is rounded to increments of .000, .003, or .007 seconds
    int nanoOfDay = (int) Math.round(Math.round((now.getNano()/1000000d)/3.333333)*3.333333)*1000000;

    LocalDateTime expected = nanoOfDay == nanosPerSecond ? now.withNano(0).plusSeconds(1) : now.withNano(nanoOfDay);

    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_datetime", now, row -> {
      ColumnChecker.checkColumn(0, "test_datetime")
        .returns(Tuple::getValue, Row::getValue, expected)
        .returns(Tuple::getLocalDateTime, Row::getLocalDateTime, expected)
        .returns(Tuple::getLocalDate, Row::getLocalDate, expected.toLocalDate())
        .returns(Tuple::getLocalTime, Row::getLocalTime, expected.toLocalTime())
        .returns(LocalDateTime.class, expected)
        .forRow(row);
    });
  }

  @Test
  @Repeat(100)
  public void testEncodeDateTime2(TestContext ctx) {
    // Make sure the number of significant digits matches the column precision
    int nanoOfSecond = 100 * ThreadLocalRandom.current().nextInt(10_000_000);
    LocalDateTime now = LocalDateTime.now().withNano(nanoOfSecond);
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
    // Make sure the number of significant digits matches the column precision
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

  @Test
  public void testEncodeVarBinaryMax(TestContext ctx) {
    byte[] bytes = new byte[15 * 1024];
    ThreadLocalRandom.current().nextBytes(bytes);
    Buffer buffer = Buffer.buffer(bytes);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_varbinary_max", buffer, row -> {
      ColumnChecker.checkColumn(0, "test_varbinary_max")
        .returns(Tuple::getValue, Row::getValue, buffer)
        .returns(Tuple::getBuffer, Row::getBuffer, buffer)
        .returns(Buffer.class, buffer)
        .forRow(row);
    });
  }

  @Test
  public void testEncodeImage(TestContext ctx) {
    byte[] bytes = new byte[15 * 1024];
    ThreadLocalRandom.current().nextBytes(bytes);
    Buffer buffer = Buffer.buffer(bytes);
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_image", buffer, row -> {
      ColumnChecker.checkColumn(0, "test_image")
        .returns(Tuple::getValue, Row::getValue, buffer)
        .returns(Tuple::getBuffer, Row::getBuffer, buffer)
        .returns(Buffer.class, buffer)
        .forRow(row);
    });
  }

  @Test
  public void testEncodeMoney(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_money", "€12.1234", row -> {
      checkNumber(row, "test_money", new BigDecimal("12.1234"));
    });
  }

  @Test
  public void testEncodeSmallMoney(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "not_nullable_datatype", "test_smallmoney", "€12.12", row -> {
      checkNumber(row, "test_smallmoney", new BigDecimal("12.12"));
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
