/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import org.junit.Assume;
import org.junit.Test;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.function.Consumer;

public abstract class DateTimeCodecTest extends MySQLDataTypeTestBase {
  @Test
  public void testDecodeAbbreviatedValue(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12", "TIME", "test_time", Duration.ofHours(11).plusMinutes(12));
  }

  @Test
  public void testDecodeAbbreviatedValueWithoutColons(TestContext ctx) {
    testDecodeGeneric(ctx, "1112", "TIME", "test_time", Duration.ofMinutes(11).plusSeconds(12));
  }

  @Test
  public void testDecodeAbbreviatedValueWithoutColons2(TestContext ctx) {
    testDecodeGeneric(ctx, "12", "TIME", "test_time", Duration.ofSeconds(12));
  }

  @Test
  public void testDecodeMaxTime(TestContext ctx) {
    testDecodeGeneric(ctx, "838:59:59", "TIME", "test_time", Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testDecodeMinTime(TestContext ctx) {
    testDecodeGeneric(ctx, "-838:59:59", "TIME", "test_time", Duration.ofHours(-838).plusMinutes(-59).plusSeconds(-59));
  }

  @Test
  public void testDecodeMaxTimeOverflow(TestContext ctx) {
    testDecodeGeneric(ctx, "850:00:00", "TIME", "test_time", Duration.ofHours(838).plusMinutes(59).plusSeconds(59));
  }

  @Test
  public void testDecodeMinTimeOverflow(TestContext ctx) {
    testDecodeGeneric(ctx, "-850:00:00", "TIME", "test_time", Duration.ofHours(-838).plusMinutes(-59).plusSeconds(-59));
  }

  @Test
  public void testDecodeDefaultFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME", "test_time", Duration.ofHours(11).plusMinutes(12));
  }

  @Test
  public void testDecodeTimeAsLocalTimeWithoutFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME", row -> {
      ctx.assertEquals(LocalTime.of(11, 12, 0), row.getLocalTime(0));
      ctx.assertEquals(LocalTime.of(11, 12, 0), row.getLocalTime("test_time"));
    }, "test_time");
  }

  @Test
  public void testDecodeFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(6)", "test_time", Duration.ofHours(11).plusMinutes(12).plusNanos(123456000));
  }

  @Test
  public void testDecodeTimeAsLocalTimeWithFractionalSecondsPart(TestContext ctx) {
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(6)", row -> {
      ctx.assertEquals(LocalTime.of(11, 12, 0,123456000), row.getLocalTime(0));
      ctx.assertEquals(LocalTime.of(11, 12, 0,123456000), row.getLocalTime("test_time"));
    }, "test_time");
  }

  @Test
  public void testDecodeFractionalSecondsPartTruncation(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMariaDB()); // MariaDB has not auto rounding for fractional seconds
    testDecodeGeneric(ctx, "11:12:00.123456", "TIME(4)", "test_time", Duration.ofHours(11).plusMinutes(12).plusNanos(123500000));
  }

  @Test
  public void testDecodeDateEmpty(TestContext ctx) {
      testDecodeGeneric(ctx, "0000-00-00", "DATE", "test_date", null);
  }
  
  @Test
  public void testDecodeDateTimeEmpty(TestContext ctx) {
      testDecodeGeneric(ctx, "0000-00-00 00:00:00", "DATETIME", "test_datetime", null);
  }
  
  @Test
  public void testDecodeDatetime(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-01-01 10:20:30", "DATETIME", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30));
  }

  @Test
  public void testDecodeDatetimeWithFractionalSeconds(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-01-01 10:20:30.123456", "DATETIME(6)", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123456000));
  }

  @Test
  public void testDecodeDatetimeWithFractionalSecondsTruncation(TestContext ctx) {
    Assume.assumeFalse(rule.isUsingMariaDB()); // MariaDB has not auto rounding for fractional seconds
    testDecodeGeneric(ctx, "2000-01-01 10:20:30.123456", "DATETIME(4)", "test_datetime", LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123500000));
  }

  @Test
  public void testDecodeInvalidDatetime(TestContext ctx) {
    testDecodeGeneric(ctx, "2000-00-34 25:20:30", "DATETIME", "test_datetime", null);
  }

  protected abstract <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected);

  protected abstract void testDecodeGeneric(TestContext ctx, String data, String dataType, Consumer<Row> valueAccessor, String columnName);
}
