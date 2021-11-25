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
package io.vertx.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public class MoneyTypeSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {

  @Test
  public void testMoney(TestContext ctx) {
    Money expected = new Money(1234, 56);
    testDecodeGeneric(ctx, "1234.56", "MONEY", "money", Tuple::getValue, Row::getValue, expected);
  }

  @Test
  public void testNegativeMoney(TestContext ctx) {
    // Does not look possible with text format
    Money expected = new Money(1234, 56);
    testDecodeGeneric(ctx, "-1234.56", "MONEY", "money", Tuple::getValue, Row::getValue, expected);
  }

  @Test
  public void testMoneyArray(TestContext ctx) {
    Money expected = new Money(1234, 56);
    testDecodeGenericArray(ctx, "ARRAY ['1234.56' :: MONEY]", "money", Tuple::getValue, Row::getValue, expected);
  }
}
