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

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@RunWith(VertxUnitRunner.class)
public class MySQLPoolTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;
  MySQLPool pool;

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    pool = MySQLPool.pool(vertx, options, new PoolOptions());
  }

  @After
  public void tearDown(TestContext ctx) {
    if (pool != null) {
      pool.close();
    }
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuouslyConnecting(TestContext ctx) {
    Async async = ctx.async(3);
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn1 -> async.countDown()));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn2 -> async.countDown()));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn3 -> async.countDown()));
    async.await();
  }

  @Test
  public void testContinuouslyQuery(TestContext ctx) {
    Async async = ctx.async(3);
    pool
      .preparedQuery("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool
      .preparedQuery("SELECT 2")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool
      .preparedQuery("SELECT 3")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(res1 -> {
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
        pool
          .preparedQuery("SELECT * FROM Fortune WHERE id=?")
          .execute(Tuple.of(1))
          .onComplete(ctx.asyncAssertSuccess(results -> {
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
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET SESSION wait_timeout=3;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(wait -> {
        vertx.setTimer(5000, id -> {
          conn
            .query("SELECT 'vertx'")
            .execute()
            .onComplete(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Connection is not active now, current status: CLOSED", err.getMessage());
            conn.close(); // close should have no effect here
            pool
              .query("SELECT 'mysql'")
              .execute()
              .onComplete(ctx.asyncAssertSuccess(res -> {
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
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SET SESSION wait_timeout=3;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(wait -> {
        conn.close(); // return it back to the pool
        vertx.setTimer(5000, id -> {
          // the query should succeed using a new connection
          pool
            .query("SELECT 'vertx'")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals("vertx", row.getString(0));
            async.complete();
          }));
        });
      }));
    }));
  }

  @Test
  @Repeat(50)
  public void testNoConnectionLeaks(TestContext ctx) {
    Tuple params = Tuple.of(options.getUser(), options.getDatabase());

    Async killConnections = ctx.async();
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      String sql = "SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE ID <> CONNECTION_ID() AND User = ? AND db = ?";
      Collector<Row, ?, List<Integer>> collector = mapping(row -> row.getInteger(0), toList());
      conn
        .preparedQuery(sql)
        .collecting(collector)
        .execute(params)
        .onComplete(ctx.asyncAssertSuccess(ids -> {
        CompositeFuture killAll = ids.value().stream()
          .<Future>map(connId -> conn.query("KILL " + connId).execute())
          .collect(Collectors.collectingAndThen(toList(), CompositeFuture::all));
        killAll.compose(cf -> conn.close()).onComplete(ctx.asyncAssertSuccess(v -> killConnections.complete()));
      }));
    }));
    killConnections.awaitSuccess();

    String sql = "SELECT CONNECTION_ID() AS cid, (SELECT count(*) FROM INFORMATION_SCHEMA.PROCESSLIST WHERE User = ? AND db = ?) AS cnt";

    int idleTimeout = 50;
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(1)
      .setIdleTimeout(idleTimeout)
      .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
      .setPoolCleanerPeriod(5);
    pool = MySQLPool.pool(options, poolOptions);

    Async async = ctx.async();
    AtomicInteger cid = new AtomicInteger();
    vertx.getOrCreateContext().runOnContext(v -> {
      pool
        .preparedQuery(sql)
        .execute(params)
        .onComplete(ctx.asyncAssertSuccess(rs1 -> {
        Row row1 = rs1.iterator().next();
        cid.set(row1.getInteger("cid"));
        ctx.assertEquals(1, row1.getInteger("cnt"));
        vertx.setTimer(2 * idleTimeout, l -> {
          pool
            .preparedQuery(sql)
            .execute(params)
            .onComplete(ctx.asyncAssertSuccess(rs2 -> {
            Row row2 = rs2.iterator().next();
            ctx.assertEquals(1, row2.getInteger("cnt"));
            ctx.assertNotEquals(cid.get(), row2.getInteger("cid"));
            async.complete();
          }));
        });
      }));
    });
    async.awaitSuccess();
  }
}
