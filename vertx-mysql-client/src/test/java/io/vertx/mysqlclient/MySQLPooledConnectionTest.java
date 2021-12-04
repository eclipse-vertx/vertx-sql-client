/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Cursor;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MySQLPooledConnectionTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;
  MySQLPool pool;
  Consumer<Handler<AsyncResult<SqlConnection>>> connector;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    pool = MySQLPool.pool(vertx, options, new PoolOptions());
    connector = handler -> {
      if (pool == null) {
        pool = MySQLPool.pool(vertx, options, new PoolOptions().setMaxSize(1));
      }
      pool.getConnection(handler);
    };
  }

  @After
  public void tearDown(TestContext ctx) {
    if (pool != null) {
      pool.close();
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTransactionRollbackUnfinishedOnRecycle(TestContext ctx) {
    String txnIdSql = "SELECT tx.trx_id " +
      "FROM information_schema.innodb_trx tx " +
      "WHERE tx.trx_mysql_thread_id = connection_id()";
    Async done = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(conn1 -> {
      deleteFromMutableTable(ctx, conn1, () -> {
        conn1.begin();
        conn1.query("INSERT INTO mutable (id, val) VALUES (5, 'some-value')").execute(ctx.asyncAssertSuccess());
        conn1.query(txnIdSql).execute(ctx.asyncAssertSuccess(result -> {
          Object txid1 = result.iterator().next().getValue(0);
          conn1.close();
          // It will be the same connection
          connector.accept(ctx.asyncAssertSuccess(conn2 -> {
            conn2.query("SELECT id FROM mutable WHERE id=5").execute(ctx.asyncAssertSuccess(result2 -> {
              ctx.assertEquals(0, result2.size());
              done.countDown();
            }));
            conn2.query(txnIdSql).execute(ctx.asyncAssertSuccess(result2 -> {
              Object txid2 = result.iterator().next().getValue(0);
              ctx.assertEquals(txid1, txid2);
              done.countDown();
            }));
          }));
        }));
      });
    }));
  }

  @Test
  public void testQueryConstantWithCursor(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT 1")
        .onComplete(ctx.asyncAssertSuccess(ps -> {
          Cursor cursor = ps.cursor();
          cursor.read(1024).onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(1, row.getInteger(0));
          }));
        }));
    }));
  }
}
