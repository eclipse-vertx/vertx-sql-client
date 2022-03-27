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

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class MySQLPipeliningTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;
  AtomicInteger orderCheckCounter;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    options.setPipeliningLimit(64);
    orderCheckCounter = new AtomicInteger(0);
    cleanTestTable(ctx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuousSimpleQueryUsingConn(TestContext ctx) {
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> testSequentialQuery(ctx, currentIter -> conn.query("SELECT " + currentIter).execute())));
  }

  @Test
  public void testContinuousSimpleQueryUsingPoolWithSingleConn(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(1));
    testSequentialQuery(ctx, currentIter -> client.query("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousSimpleQueryUsingPool(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testSequentialQuery(ctx, currentIter -> client.query("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotPreparedQueryUsingConn(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> testSequentialQuery(ctx, currentIter -> conn.preparedQuery("SELECT " + currentIter).execute())));
  }

  @Test
  public void testContinuousOneShotPreparedQueryUsingPoolWithSingleConn(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(1));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotPreparedQueryUsingPool(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> testSequentialQuery(ctx, currentIter -> conn.preparedQuery("SELECT ?").execute(Tuple.of(currentIter)))));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingPoolWithSingleConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(1));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT ?").execute(Tuple.of(currentIter)));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingPool(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT ?").execute(Tuple.of(currentIter)));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> testSequentialQuery(ctx, currentIter -> conn.preparedQuery("SELECT " + currentIter).execute())));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingPoolWithSingleConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(1));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingPool(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testSequentialQuery(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testPrepareAndExecuteWithDifferentSql(TestContext ctx) {
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(1000);
        for (int i = 0; i < 1000; i++) {
          final int currentIter = i;
          conn.prepare("SELECT " + currentIter).onComplete(ctx.asyncAssertSuccess(ps -> {
            ps.query().execute().onComplete(ctx.asyncAssertSuccess(res -> {
              checkQueryResult(ctx, res, currentIter, orderCheckCounter);
              ps.close(ctx.asyncAssertSuccess(v -> {
                latch.countDown();
              }));
            }));
          }));
        }
      }));
  }

  @Test
  public void testOneShotPreparedBatchQueryConn(TestContext ctx) {
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        testOneShotPreparedBatchQuery(ctx, conn);
      }));
  }

  @Test
  public void testOneShotPreparedBatchQueryPool(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testOneShotPreparedBatchQuery(ctx, client);
  }

  private void testOneShotPreparedBatchQuery(TestContext ctx, SqlClient client) {
    List<Tuple> batchParams = new ArrayList<>();
    Async latch = ctx.async(1000);
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i));
    }
    client.preparedQuery("SELECT ?")
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertSuccess(res -> {
        for (int i = 0; i < 1000; i++) {
          ctx.assertEquals(1, res.size());
          Row row = res.iterator().next();
          ctx.assertEquals(1, row.size());
          ctx.assertEquals(i, row.getInteger(0));
          latch.countDown();
          res = res.next();
        }
        client.close();
      }));
  }

  @Test
  public void testOneShotPreparedBatchInsertConn(TestContext ctx) {
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        testOneShotPreparedBatchInsert(ctx, conn);
      }));
  }

  @Test
  public void testOneShotPreparedBatchInsertPool(TestContext ctx) {
    SqlClient client = MySQLPool.client(vertx, options, new PoolOptions().setMaxSize(8));
    testOneShotPreparedBatchInsert(ctx, client);
  }

  private void testOneShotPreparedBatchInsert(TestContext ctx, SqlClient client) {
    Async latch = ctx.async(1000);
    List<Tuple> batchParams = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i, String.format("val-%d", i)));
    }
    client.preparedQuery("INSERT INTO mutable(id, val) VALUES (?, ?)")
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertSuccess(res -> {
        for (int i = 0; i < 1000; i++) {
          ctx.assertEquals(1, res.rowCount());
          res = res.next();
          latch.countDown();
        }

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

  private void testSequentialQuery(TestContext ctx, Function<Integer, Future<RowSet<Row>>> resultExecution) {
    Async latch = ctx.async(1000);
    Future<RowSet<Row>> fut = Future.succeededFuture();
    for (int i = 0; i < 1000; i++) {
      final int currentIter = i;
      fut = fut.flatMap(res -> resultExecution.apply(currentIter)).onComplete(ctx.asyncAssertSuccess(res -> {
        checkQueryResult(ctx, res, currentIter, orderCheckCounter);
        latch.countDown();
      }));
    }
  }

  private void checkQueryResult(TestContext ctx, RowSet<Row> result, int currentIter, AtomicInteger orderCheckCounter) {
    ctx.assertEquals(1, result.size());
    Row row = result.iterator().next();
    ctx.assertEquals(1, row.size());
    ctx.assertEquals(currentIter, row.getInteger(0));
    ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
  }
}
