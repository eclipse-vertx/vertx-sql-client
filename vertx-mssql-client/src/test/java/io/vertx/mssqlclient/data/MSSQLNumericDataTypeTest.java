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

package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLNumericDataTypeTest extends MSSQLDataTypeTestBase {
  @Test
  public void testQueryLargeNumeric(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", Numeric.parse("99999999999999999999999999999999999999"));
  }

  @Test
  public void testPreparedQueryLargeNumeric(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_numeric", "NUMERIC(38)", "99999999999999999999999999999999999999", Numeric.parse("99999999999999999999999999999999999999"));
  }

  @Test
  public void testQueryLargeDecimal(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", Numeric.parse("99999999999999999999.9999999999"));
  }

  @Test
  public void testPreparedQueryLargeDecimal(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_decimal", "DECIMAL(30, 10)", "99999999999999999999.9999999999", Numeric.parse("99999999999999999999.9999999999"));
  }
}
