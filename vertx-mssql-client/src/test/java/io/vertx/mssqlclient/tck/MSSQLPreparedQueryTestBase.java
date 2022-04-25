/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.mssqlclient.junit.MSSQLRule;
import io.vertx.sqlclient.tck.PreparedQueryTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;

public abstract class MSSQLPreparedQueryTestBase extends PreparedQueryTestBase {

  @ClassRule
  public static MSSQLRule rule = MSSQLRule.SHARED_INSTANCE;

  @Override
  protected boolean cursorRequiresTx() {
    return false;
  }

  protected void cleanTestTable(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("@p").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Override
  @Test
  @Ignore
  public void testPrepareError(TestContext ctx) {
    // prepexec prepared statement will not care about the SQL
    super.testPrepareError(ctx);
  }

  @Override
  @Test
  @Ignore
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    // can't check this for now due to prepexec cmd
    super.testPreparedQueryParamCoercionQuantityError(ctx);
  }

  @Override
  @Test
  @Ignore
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    // can't check this for now due to prepexec cmd
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }
}

