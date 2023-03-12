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
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.*;

public abstract class MySQLBatchInsertExceptionTestBase extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = createOptions();
    cleanTestTable(ctx);
  }

  protected MySQLConnectOptions createOptions() {
    return new MySQLConnectOptions(rule.options());
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testBatchInsertExceptionConn(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      testBatchInsertException(ctx, conn);
    }));
  }

  @Test
  public void testBatchInsertExceptionPool(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testBatchInsertException(ctx, client);
  }

  private void testBatchInsertException(TestContext ctx, SqlClient client) {
    int total = 50;
    List<Tuple> batchParams = new ArrayList<>();
    for (int i = 0; i < total; i++) {
      int val = (i & 1) == 1 ? i - 1 : i;
      batchParams.add(Tuple.of(val, String.format("val-%d", val))); // primary key violation error occurs on odd numbers
    }

    client.preparedQuery("INSERT INTO mutable(id, val) VALUES (?, ?)")
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertFailure(error -> {
        ctx.assertEquals(MySQLBatchException.class, error.getClass());
        MySQLBatchException mySQLBatchException = (MySQLBatchException) error;
        ctx.assertEquals(IntStream.iterate(1, i -> i + 2).limit(total / 2).boxed().collect(toSet()), mySQLBatchException.getIterationError().keySet());

        // all the param will be executed
        client.query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(total / 2, res2.size());
            int i = 0;
            for (Row row : res2) {
              ctx.assertEquals(2, row.size());
              ctx.assertEquals(i, row.getInteger(0));
              ctx.assertEquals(String.format("val-%d", i), row.getString(1));
              i += 2;
            }
            client.close();
          }));
      }));
  }

  private void cleanTestTable(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}
