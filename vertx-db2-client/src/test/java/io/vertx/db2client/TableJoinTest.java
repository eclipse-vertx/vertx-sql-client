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

package io.vertx.db2client;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

/**
 * Tests for table joins which are documented here:
 * https://www.ibm.com/support/knowledgecenter/SSEPEK_10.0.0/intro/src/tpc/db2z_joindatafromtables.html
 */
@RunWith(VertxUnitRunner.class)
public class TableJoinTest extends DB2TestBase {

  @Test
  public void testColumnRename(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT immutable.id AS \"IMM ID\"," +
                          "immutable.message AS IMM_MSG," +
                    "Fortune.id AS FORT_ID," +
                          "Fortune.message AS \"fortune msg\" FROM immutable " +
                "INNER JOIN Fortune ON (immutable.id + 1) = Fortune.id " +
          "WHERE immutable.id=1").execute(
          ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        ctx.assertEquals(Arrays.asList("IMM ID", "IMM_MSG", "FORT_ID", "fortune msg"), rowSet.columnsNames());
        Row row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals(1, row.getInteger("IMM ID"));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        ctx.assertEquals("fortune: No such file or directory", row.getString("IMM_MSG"));
        ctx.assertEquals(2, row.getInteger(2));
        ctx.assertEquals(2, row.getInteger("FORT_ID"));
        ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
        ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString("fortune msg"));
        conn.close();
      }));
    }));
  }

  @Test
  public void testInnerJoin(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT immutable.id,immutable.message,Fortune.id,Fortune.message FROM immutable " +
                "INNER JOIN Fortune ON (immutable.id + 1) = Fortune.id " +
          "WHERE immutable.id=1").execute(
          ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        ctx.assertEquals(Arrays.asList("ID", "MESSAGE", "ID", "MESSAGE"), rowSet.columnsNames());
        Row row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        ctx.assertEquals(2, row.getInteger(2));
        ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
        conn.close();
      }));
    }));
  }

  @Test
  public void testInnerJoinPrepared(TestContext ctx) {
    testJoin(ctx, "INNER JOIN");
  }

  @Test
  public void testLeftOuterJoin(TestContext ctx) {
    testJoin(ctx, "LEFT OUTER JOIN");
  }

  @Test
  public void testRightOuterJoin(TestContext ctx) {
    testJoin(ctx, "RIGHT OUTER JOIN");
  }

  @Test
  public void testFullOuterJoin(TestContext ctx) {
    testJoin(ctx, "FULL OUTER JOIN");
  }

  private void testJoin(TestContext ctx, String joinType) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT * FROM immutable " +
                joinType + " Fortune ON (immutable.id + 1) = Fortune.id " +
          "WHERE immutable.id=1").execute(
          ctx.asyncAssertSuccess(rowSet -> {
        ctx.assertEquals(1, rowSet.size());
        ctx.assertEquals(Arrays.asList("ID", "MESSAGE", "ID", "MESSAGE"), rowSet.columnsNames());
        Row row = rowSet.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        ctx.assertEquals(2, row.getInteger(2));
        ctx.assertEquals("A computer scientist is someone who fixes things that aren't broken.", row.getString(3));
        conn.close();
      }));
    }));
  }

}
