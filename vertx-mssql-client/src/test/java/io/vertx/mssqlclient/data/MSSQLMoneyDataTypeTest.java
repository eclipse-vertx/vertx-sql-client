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

@RunWith(VertxUnitRunner.class)
public class MSSQLMoneyDataTypeTest extends MSSQLDataTypeTestBase {

  public static final String MAX_MONEY_STRING = "922337203685477.5807";
  public static final double MAX_MONEY = Double.parseDouble(MAX_MONEY_STRING);

  @Test
  public void testQueryLargeMoney(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", MAX_MONEY_STRING, MAX_MONEY);
  }

  @Test
  public void testQueryLargeNegativeMoney(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", String.format("-%s", MAX_MONEY_STRING), -MAX_MONEY);
  }

  @Test
  public void testQueryMoneyWithCurrency(TestContext ctx) {
    testQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", "$12.34", 12.34d);
  }

  @Test
  public void testPreparedQueryLargeMoney(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", MAX_MONEY_STRING, MAX_MONEY);
  }

  @Test
  public void testPreparedQueryLargeNegativeMoney(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", String.format("-%s", MAX_MONEY_STRING), -MAX_MONEY);
  }

  @Test
  public void testPreparedQueryMoneyWithCurrency(TestContext ctx) {
    testPreparedQueryDecodeGenericWithoutTable(ctx, "test_money", "MONEY", "$12.34", 12.34d);
  }
}
