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
package io.vertx.oracle.test.tck;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracle.test.junit.OracleRule;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.PreparedBatchTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class OraclePreparedBatchTest extends PreparedBatchTestBase {
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
  public void cleanTestTable(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("TRUNCATE TABLE mutable").execute(result -> {
        conn.close();
      });
    }));
  }

  @Test
  @Override
  public void testInsert(TestContext ctx) {
    Async async = ctx.async();
    this.connector.connect(ctx.asyncAssertSuccess((conn) -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(79991, "batch one"));
      batch.add(Tuple.of(79992, "batch two"));
      batch.add(Tuple.wrap(Arrays.asList(79993, "batch three")));
      batch.add(Tuple.wrap(Arrays.asList(79994, "batch four")));
      Future<RowSet<Row>> fut = conn.preparedQuery("INSERT INTO MUTABLE (id, val) VALUES (?, ?)")
        .executeBatch(batch);

      fut.onComplete(result -> {
        ctx.assertFalse(result.failed());
        ctx.assertEquals(4, result.result().rowCount());
        conn.preparedQuery("SELECT * FROM mutable WHERE id=?")
          .execute(Tuple.of(79991), ctx.asyncAssertSuccess(rows1 -> {
            verify(rows1, ctx, 79991, "batch one");
            conn.preparedQuery("SELECT * FROM mutable WHERE id=?")
              .execute(Tuple.of(79992), ctx.asyncAssertSuccess((ar2) -> {
                verify(ar2, ctx, 79992, "batch two");
                conn.preparedQuery(this.statement("SELECT * FROM mutable WHERE id=", ""))
                  .execute(Tuple.of(79993), ctx.asyncAssertSuccess((ar3) -> {
                    verify(ar3, ctx, 79993, "batch three");
                    conn.preparedQuery(
                      this.statement("SELECT * FROM mutable WHERE id=", ""))
                      .execute(Tuple.of(79994), ctx.asyncAssertSuccess((ar4) -> {
                        verify(ar4, ctx, 79994, "batch four");
                        async.complete();
                      }));
                  }));
              }));
          }));
      });
    }));
  }

  private void verify(RowSet<Row> rows, TestContext ctx, int id, String val) {
    ctx.assertEquals(1, rows.size());
    Row one = rows.iterator().next();
    ctx.assertEquals(id, one.getInteger(0));
    ctx.assertEquals(val, one.getString(1));
  }

  @Test
  @Ignore("Oracle does not support batching queries")
  public void testBatchQuery(TestContext ctx) {

  }

  @Test
  public void testEmptyBatch(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      conn.preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""))
        .executeBatch(batch, ctx.asyncAssertFailure(err -> {
          async.complete();
        }));
    }));
  }

  @Test
  public void testIncorrectNumBatchArguments(TestContext ctx) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = new ArrayList<>();
      batch.add(Tuple.of(1, 2));
      conn.preparedQuery(statement("SELECT * FROM immutable WHERE id=", ""))
        .executeBatch(batch, ctx.asyncAssertFailure(err -> {
          async.complete();
        }));
    }));
  }

}
