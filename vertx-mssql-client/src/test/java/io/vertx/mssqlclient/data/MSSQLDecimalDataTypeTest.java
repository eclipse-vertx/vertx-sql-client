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

import java.math.BigDecimal;

@RunWith(VertxUnitRunner.class)
public class MSSQLDecimalDataTypeTest extends MSSQLDataTypeTestBase {
  @Test
  public void testQueryLargeNumeric(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", new BigDecimal("99999999999999999999999999999999999999"));
  }

  @Test
  public void testPreparedQueryLargeNumeric(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", new BigDecimal("99999999999999999999999999999999999999"));
  }

  @Test
  public void testQueryLargeDecimal(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", new BigDecimal("99999999999999999999.9999999999"));
  }

  @Test
  public void testPreparedQueryLargeDecimal(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", new BigDecimal("99999999999999999999.9999999999"));
  }

  @Test
  public void testDecodingDecimal(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(19, 2)", "21474836.48", new BigDecimal("21474836.48"));
  }
}
