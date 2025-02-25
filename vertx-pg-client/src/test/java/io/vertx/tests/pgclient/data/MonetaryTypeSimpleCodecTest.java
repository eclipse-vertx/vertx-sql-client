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
package io.vertx.tests.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.data.Money;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.math.BigDecimal;

public class MonetaryTypeSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {

  @Test
  public void testMoney(TestContext ctx) {
    Money expected = new Money(new BigDecimal("1234.56"));
    testDecodeGeneric(ctx, "1234.56", "MONEY", "money", Tuple::getValue, Row::getValue, expected);
  }

  @Test
  public void testNegativeMoney(TestContext ctx) {
    Money expected = new Money(new BigDecimal("-1234.56"));
    testDecodeGeneric(ctx, "-1234.56", "MONEY", "money", Tuple::getValue, Row::getValue, expected);
  }

  @Test
  public void testMoneyArray(TestContext ctx) {
    Money[] expected = {new Money(new BigDecimal("1234.56")), new Money(new BigDecimal("-1234.56"))};
    testDecodeGenericArray(ctx, "ARRAY ['1234.56' :: MONEY , '-1234.56' :: MONEY]", "money", Tuple::getValue, Row::getValue, expected);
  }
}
