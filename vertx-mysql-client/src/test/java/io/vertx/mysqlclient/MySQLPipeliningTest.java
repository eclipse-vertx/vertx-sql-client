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
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class MySQLPipeliningTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    options.setPipeliningLimit(64);
    cleanTestTable(ctx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testBatchInsertExceptionConn(TestContext ctx) {
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        testBatchInsertException(ctx, conn);
      }));
  }

  @Test
  public void testBatchInsertExceptionPool(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testBatchInsertException(ctx, client);
  }

  private void testBatchInsertException(TestContext ctx, SqlClient client) {
    List<Tuple> batchParams = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i, String.format("val-%d", i)));
    }
    batchParams.add(501, Tuple.of(500, "error")); // primary key violation error occurs in the 501st iteration

    client.preparedQuery("INSERT INTO mutable(id, val) VALUES (?, ?)")
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertFailure(error -> {
        ctx.assertEquals(MySQLBatchException.class, error.getClass());
        MySQLBatchException mySQLBatchException = (MySQLBatchException) error;
        ctx.assertTrue(mySQLBatchException.getIterationError().containsKey(501));

        // all the param will be executed
        client.query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1000, res2.size());
            int i = 0;
            for (Row row : res2) {
              ctx.assertEquals(2, row.size());
              ctx.assertEquals(i, row.getInteger(0));
              ctx.assertEquals(String.format("val-%d", i), row.getString(1));
              i++;
            }
            client.close();
          }));
      }));
  }

  private void cleanTestTable(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}
