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

@RunWith(VertxUnitRunner.class)
public class MSSQLDateTimeDataTypeTest extends MSSQLDataTypeTestBase {

  // 	Datatype accuracy is rounded to increments of .000, .003, or .007 seconds
  private static final int[] HUNDRED_NANOS = {0, 1000000, 1100000, 1130000, 1170000};

  @Test
  public void testQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, 33, 21);
    for (int i = 0; i <= 4; i++) {
      String columnName = String.format("test_datetime_%d", i);
      LocalDateTime expected = localDateTime.withNano(100 * HUNDRED_NANOS[i]);
      String value = String.format("'%s'", expected);
      testQueryDecodeGenericWithoutTable(ctx, columnName, "DATETIME", value, expected);
    }
  }

  @Test
  public void testPreparedQueryTime(TestContext ctx) {
    LocalDateTime localDateTime = LocalDateTime.of(2021, 3, 26, 8, 33, 21);
    for (int i = 0; i <= 4; i++) {
      String columnName = String.format("test_datetime_%d", i);
      LocalDateTime expected = localDateTime.withNano(100 * HUNDRED_NANOS[i]);
      String value = String.format("'%s'", expected);
      testPreparedQueryDecodeGenericWithoutTable(ctx, columnName, "DATETIME", value, expected);
    }
  }
}
