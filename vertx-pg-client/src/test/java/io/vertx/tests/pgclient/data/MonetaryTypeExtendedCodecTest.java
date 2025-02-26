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
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.math.BigDecimal;

public class MonetaryTypeExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {

  @Test
  public void testDecodeMoney(TestContext ctx) {
    testDecode(ctx, "SELECT 1234.45::MONEY, (-1234.45)::MONEY", Tuple::getValue, new Money(new BigDecimal("1234.45")), new Money(new BigDecimal("-1234.45")));
  }

  @Test
  public void testEncodeMoney(TestContext ctx) {
    testEncode(ctx, "SELECT ($1::MONEY)::VARCHAR, ($2::MONEY)::VARCHAR", Tuple.of(new Money(new BigDecimal("1234.45")), new Money(new BigDecimal("-1234.45"))), "$1,234.45", "-$1,234.45");
  }

  @Test
  public void testDecodeMoneyArray(TestContext ctx) {
    testDecode(ctx, "SELECT '{ 1234.45, -1234.45 }'::MONEY[]", Tuple::getValue, (Object) (new Money[]{new Money(new BigDecimal("1234.45")), new Money(new BigDecimal("-1234.45"))}));
  }

  @Test
  public void testEncodeMoneyArray(TestContext ctx) {
    testEncode(ctx, "SELECT (($1::MONEY[])[1])::VARCHAR", Tuple.of(new Money[]{new Money(new BigDecimal("1234.45"))}), "$1,234.45");
  }
}
