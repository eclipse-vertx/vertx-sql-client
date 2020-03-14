/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLPreparedStatementTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuousPreparedQueriesWithSameTypeParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT id, message FROM immutable WHERE id = ? AND message = ?", ctx.asyncAssertSuccess(preparedQuery -> {
        preparedQuery.execute(Tuple.of(1, "fortune: No such file or directory"), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          preparedQuery.execute(Tuple.of(4, "After enough decimal places, nobody gives a damn."), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(0, res2.size());
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testContinuousPreparedQueriesWithDifferentTypeParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT id, message FROM immutable WHERE id = ? AND message = ?", ctx.asyncAssertSuccess(preparedQuery -> {
        preparedQuery.execute(Tuple.of("1", "fortune: No such file or directory"), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          preparedQuery.execute(Tuple.of(4, "A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1"), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1, res2.size());
            conn.close();
          }));
        }));
      }));
    }));
  }
}
