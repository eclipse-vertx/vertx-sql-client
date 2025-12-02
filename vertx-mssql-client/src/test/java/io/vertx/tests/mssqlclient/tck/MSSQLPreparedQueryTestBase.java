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

package io.vertx.tests.mssqlclient.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.tests.mssqlclient.junit.MSSQLRule;
import io.vertx.tests.sqlclient.tck.PreparedQueryTestBase;
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
      conn
        .query("TRUNCATE TABLE mutable;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
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

  @Test
  public void testPrepareError(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT whatever FROM DOES_NOT_EXIST").onComplete(ctx.asyncAssertSuccess(ps -> {
        ps
          .query()
          .execute()
          .onComplete(ctx.asyncAssertFailure(error -> {
          ctx.assertTrue(error instanceof MSSQLException);
          MSSQLException e = (MSSQLException) error;
          ctx.assertEquals("Invalid object name 'DOES_NOT_EXIST'.", e.errorMessage());
        }));
      }));
    }));
  }

  @Override
  @Test
  @Ignore
  public void testPreparedQueryParamCoercionQuantityError(TestContext ctx) {
    // can't check this due to prepexec cmd
    super.testPreparedQueryParamCoercionQuantityError(ctx);
  }

  @Override
  @Test
  @Ignore
  public void testPreparedQueryParamCoercionTypeError(TestContext ctx) {
    // can't check this due to prepexec cmd
    super.testPreparedQueryParamCoercionTypeError(ctx);
  }

  @Test
  public void failureWhenPreparingCursor(TestContext ctx) {
    Async async = ctx.async();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT invalid_function()").onComplete(ctx.asyncAssertSuccess(ps -> {
        ps.createStream(50)
          .exceptionHandler(error -> {
            ctx.assertTrue(error instanceof MSSQLException);
            MSSQLException e = (MSSQLException) error;
            ctx.assertEquals("'invalid_function' is not a recognized built-in function name.", e.errorMessage());
            async.complete();
          })
          .handler(row -> ctx.fail());
      }));
    }));
  }

  @Test
  public void testNbcRowWithCursor(TestContext ctx) {
    Async async = ctx.async();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM nbcrow_with_rowstat").onComplete(ctx.asyncAssertSuccess(ps -> {
        ps.createStream(50)
          .exceptionHandler(ctx::fail)
          .handler(row -> {
            // Make sure NbcRow handling is correct when the number of columns is a multiple of 8
            ctx.assertEquals(0, row.size() % 8);
            for (int i = 1; i <= 8; i++) {
              if (i % 2 != 0) {
                ctx.assertEquals(String.valueOf(i), row.getString(i - 1));
              } else {
                ctx.assertNull(row.getString(i - 1));
              }
            }
          })
          .endHandler(v -> async.complete());
      }));
    }));
  }
}

