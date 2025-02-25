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

package io.vertx.tests.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class MSSQLSmallDateTimeDataTypeTest extends MSSQLDataTypeTestBase {

  private static final int minutes = 33;
  private static final List<Integer> seconds = Arrays.asList(0, 29, 30, 59);

  @Test
  public void testQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, minutes, 0);
    for (Integer second: seconds) {
      String columnName = String.format("test_smalldatetime_%d", second);
      int roundedUpMinute = (int) Math.floor(second/30.0);
      LocalDateTime expected = localDateTime.withSecond(0).withMinute(minutes + roundedUpMinute);

      // Smalldate isn't ANSI or ISO 8601 compliant, so we have to remove 'T' from the date
      String value = String.format("'%s'", localDateTime.withSecond(second).toString().replace('T', ' '));
      testQueryDecodeGenericWithoutTable(ctx, columnName, "SMALLDATETIME", value, expected);
    }
  }

  @Test
  public void testPreparedQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, minutes, 0);
    for (Integer second: seconds) {
      String columnName = String.format("test_smalldatetime_%d", second);
      int roundedUpMinute = (int) Math.floor(second/30.0);
      LocalDateTime expected = localDateTime.withSecond(0).withMinute(minutes + roundedUpMinute);

      // Smalldate isn't ANSI or ISO 8601 compliant, so we have to remove 'T' from the date
      String value = String.format("'%s'", localDateTime.withSecond(second).toString().replace('T', ' '));
      testPreparedQueryDecodeGenericWithoutTable(ctx, columnName, "SMALLDATETIME", value, expected);
    }
  }
}
