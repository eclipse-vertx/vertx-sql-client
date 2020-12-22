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

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLPoolTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;
  MySQLPool pool;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    pool = MySQLPool.pool(vertx, options, new PoolOptions());
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuouslyConnecting(TestContext ctx) {
    Async async = ctx.async(3);
    pool.getConnection(ctx.asyncAssertSuccess(conn1 -> async.countDown()));
    pool.getConnection(ctx.asyncAssertSuccess(conn2 -> async.countDown()));
    pool.getConnection(ctx.asyncAssertSuccess(conn3 -> async.countDown()));
    async.await();
  }

  @Test
  public void testContinuouslyQuery(TestContext ctx) {
    Async async = ctx.async(3);
    pool.preparedQuery("SELECT 1").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool.preparedQuery("SELECT 2").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool.preparedQuery("SELECT 3").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    async.await();
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    PoolOptions poolOptions = new PoolOptions().setMaxSize(2);
    MySQLPool pool = MySQLPool.pool(vertx, new MySQLConnectOptions(this.options).setCachePreparedStatements(false), poolOptions);
    try {
      int numRequests = 1500;
      Async async = ctx.async(numRequests);
      for (int i = 0;i < numRequests;i++) {
        pool.preparedQuery("SELECT * FROM Fortune WHERE id=?").execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.size());
          Tuple row = results.iterator().next();
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          async.countDown();
        }));
      }
      async.awaitSuccess(10_000);
    } finally {
      pool.close();
    }
  }

  @Test
  public void testBorrowedPooledConnectionClosedByServer(TestContext ctx) {
    Async async = ctx.async();
    PoolOptions poolOptions = new PoolOptions().setMaxSize(1);
    MySQLPool pool = MySQLPool.pool(vertx, new MySQLConnectOptions(this.options).setCachePreparedStatements(false), poolOptions);
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.query("SET SESSION wait_timeout=3;").execute(ctx.asyncAssertSuccess(wait -> {
        vertx.setTimer(5000, id -> {
          conn.query("SELECT 'vertx'").execute(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Connection is not active now, current status: CLOSED", err.getMessage());
            conn.close(); // close should have no effect here
            pool.query("SELECT 'mysql'").execute(ctx.asyncAssertSuccess(res -> {
              // the pool will construct a new connection and use it
              ctx.assertEquals(1, res.size());
              Row row = res.iterator().next();
              ctx.assertEquals("mysql", row.getString(0));
              async.complete();
            }));
          }));
        });
      }));
    }));
  }

  @Test
  public void testPooledConnectionClosedByServer(TestContext ctx) {
    Async async = ctx.async();
    PoolOptions poolOptions = new PoolOptions().setMaxSize(1);
    MySQLPool pool = MySQLPool.pool(vertx, new MySQLConnectOptions(this.options).setCachePreparedStatements(false), poolOptions);
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.query("SET SESSION wait_timeout=3;").execute(ctx.asyncAssertSuccess(wait -> {
        conn.close(); // return it back to the pool
        vertx.setTimer(5000, id -> {
          // the query should succeed using a new connection
          pool.query("SELECT 'vertx'").execute(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals("vertx", row.getString(0));
            async.complete();
          }));
        });
      }));
    }));
  }
}
