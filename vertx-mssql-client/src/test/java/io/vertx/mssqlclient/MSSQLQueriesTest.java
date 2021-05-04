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

package io.vertx.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class MSSQLQueriesTest extends MSSQLTestBase {

  Vertx vertx;
  MSSQLConnection connnection;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> this.connnection = conn));
  }

  @After
  public void tearDown(TestContext ctx) {
    if (connnection != null) {
      connnection.close(ctx.asyncAssertSuccess());
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testSimpleQueryOrderBy(TestContext ctx) {
    connnection.query("SELECT message FROM immutable ORDER BY message DESC")
      .execute(ctx.asyncAssertSuccess(rs -> ctx.assertTrue(rs.size() > 1)));
  }

  @Test
  public void testPreparedQueryOrderBy(TestContext ctx) {
    connnection.preparedQuery("SELECT message FROM immutable WHERE id BETWEEN @p1 AND @p2 ORDER BY message DESC")
      .execute(Tuple.of(4, 9), ctx.asyncAssertSuccess(rs -> ctx.assertEquals(6, rs.size())));
  }

  @Test
  public void testQueryCurrentTimestamp(TestContext ctx) {
    LocalDateTime start = LocalDateTime.now();
    connnection.query("SELECT current_timestamp")
      .execute(ctx.asyncAssertSuccess(rs -> {
        Object value = rs.iterator().next().getValue(0);
        ctx.assertTrue(value instanceof LocalDateTime);
        LocalDateTime localDateTime = (LocalDateTime) value;
        ctx.assertTrue(localDateTime.isAfter(start));
      }));
  }
}
