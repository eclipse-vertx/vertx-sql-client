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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

@RunWith(VertxUnitRunner.class)
public class MSSQLDateTimeOffsetDataTypeTest extends MSSQLDataTypeTestBase {

  private static final int[] HUNDRED_NANOS = {0, 1000000, 1100000, 1110000, 1111000, 1111100, 1111110, 1111111};

  @Test
  public void testQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, 33, 21, HUNDRED_NANOS[7]);
    OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.MAX);
    String value = String.format("'%s'", offsetDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss.nnnnnnnxxx")));
    //String value = String.format("'%s'", offsetDateTime).replace('T', ' ');
    System.out.println("value = " + value);
    value = "'2007-05-08 12:35:29.1234567+12:15'";
    System.out.println("value = " + value);
    for (int i = 0; i <= 7; i++) {
      String columnName = String.format("test_datetimeoffset_%d", i);
      String type = String.format("DATETIMEOFFSET(%d)", i);
      OffsetDateTime expected = localDateTime.withNano(100 * HUNDRED_NANOS[i]).atOffset(ZoneOffset.MAX);
      testQueryDecodeGenericWithoutTable(ctx, columnName, type, value, expected);
    }
  }

  @Test
  public void testPreparedQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, 33, 21, 100 * HUNDRED_NANOS[7]);
    String value = String.format("'%s'", localDateTime);
    for (int i = 0; i <= 7; i++) {
      String columnName = String.format("test_datetime2_%d", i);
      String type = String.format("DATETIME2(%d)", i);
      LocalDateTime expected = localDateTime.withNano(100 * HUNDRED_NANOS[i]);
      testPreparedQueryDecodeGenericWithoutTable(ctx, columnName, type, value, expected);
    }
  }
}
