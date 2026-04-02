/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.db2client.tck;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DB2PreparedQueryTestBase extends PreparedQueryTestBase {

  private static final Logger logger = LoggerFactory.getLogger(DB2PreparedQueryTestBase.class);

  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    logger.info(">>> BEGIN {}.{}", getClass().getSimpleName(), testName.getMethodName());
  }

  @Override
  protected void cleanTestTable(TestContext ctx) {
    // use DELETE FROM because DB2 does not support TRUNCATE TABLE
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("DELETE FROM mutable").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Override
  protected boolean cursorRequiresTx() {
      return false;
  }

  @Test
  @Override
  public void testPrepareError(TestContext ctx) {
    msgVerifier = (err) -> {
      ctx.assertTrue(err.getMessage().startsWith("The object '" + rule.options().getUser().toUpperCase() + ".DOES_NOT_EXIST' provided is not defined"));
    };
    super.testPrepareError(ctx);
  }

  @Test
  @Override
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    msgVerifier = (err) -> {
      ctx.assertEquals("Parameter at position[0] with class = [java.lang.String] and value = [1] can not be coerced to the expected class = [java.lang.Integer] for encoding.",
          err.getMessage());
    };
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }

  @Test
  @Override
  public void testPreparedUpdateWithNullParams(TestContext ctx) {
    msgVerifier = (err) -> {
      String msg = "An attempt was made to INSERT or UPDATE a column that was declared as not nullable with the NULL value";
      ctx.assertTrue(err.getMessage().contains(msg), "Expected to find '" + msg + "' in throwable but error message was: " + err.getMessage());
    };
    super.testPreparedUpdateWithNullParams(ctx);
  }
}
