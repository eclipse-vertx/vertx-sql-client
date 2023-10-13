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

package io.vertx.oracleclient.test.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OraclePreparedQueryTest extends PreparedQueryTestBase {

  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected void initConnector() {
    connector = ClientConfig.POOLED.connect(vertx, rule.options());
  }

  @Override
  protected String statement(String... parts) {
    return String.join(" ?", parts);
  }

  @Override
  protected boolean cursorRequiresTx() {
    return false;
  }

  @Override
  @Test
  @Ignore("unsupported")
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }

  @Override
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    msgVerifier = t -> ctx.assertTrue(t.getMessage().contains("Invalid column index"));
    super.testPreparedQueryParamCoercionQuantityError(ctx);
  }
}

