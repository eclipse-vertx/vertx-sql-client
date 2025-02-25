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

package io.vertx.tests.mssqlclient.tck;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryTest extends MSSQLPreparedQueryTestBase {
  @Override
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
    cleanTestTable(ctx); // need to use batch instead of prepared statements
  }

  @Override
  protected void initConnector() {
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Test
  public void closePreparedNotExecuted(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM IMMUTABLE WHERE id = @p1").onComplete(ctx.asyncAssertSuccess(ps -> {
        ps.close().onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test
  public void closePreparedExecuted(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT * FROM IMMUTABLE WHERE id = @p1").onComplete(ctx.asyncAssertSuccess(ps -> {
        ps
          .query()
          .execute(Tuple.of(3))
          .onComplete( ctx.asyncAssertSuccess(rs -> {
          Row row = rs.iterator().next();
          ctx.assertEquals(3, row.getInteger(0));
          ctx.assertEquals("After enough decimal places, nobody gives a damn.", row.getString(1));
          ps
            .query()
            .execute(Tuple.of(7))
            .onComplete(ctx.asyncAssertSuccess(rs2 -> {
            Row row2 = rs2.iterator().next();
            ctx.assertEquals(7, row2.getInteger(0));
            ctx.assertEquals("Any program that runs right is obsolete.", row2.getString(1));
            ps.close().onComplete(ctx.asyncAssertSuccess());
          }));
        }));
      }));
    }));
  }
}
