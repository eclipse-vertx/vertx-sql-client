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

import java.math.BigDecimal;

@RunWith(VertxUnitRunner.class)
public class MSSQLSmallMoneyDataTypeTest extends MSSQLDataTypeTestBase {

  @Test
  public void testQueryLargeMoney(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "214748.36", new BigDecimal("214748.36"));
  }

  @Test
  public void testQueryLargeNegativeMoney(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "-214748.36", new BigDecimal("-214748.36"));
  }

  @Test
  public void testQueryMoneyWithCurrency(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "$12.34", new BigDecimal("12.34"));
  }

  @Test
  public void testPreparedQueryLargeMoney(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "214748.36", new BigDecimal("214748.36"));
  }

  @Test
  public void testPreparedQueryLargeNegativeMoney(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "-214748.36", new BigDecimal("-214748.36"));
  }

  @Test
  public void testPreparedQueryMoneyWithCurrency(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_smallmoney", "SMALLMONEY", "$12.34", new BigDecimal("12.34"));
  }
}
