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

@RunWith(VertxUnitRunner.class)
public class MySQLPipeliningTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    cleanTestTable(ctx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuousSimpleQuery(TestContext ctx) {
    options.setPipeliningLimit(64);
    AtomicInteger orderCheckCounter = new AtomicInteger(0);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(1000);
        for (int i = 0; i < 1000; i++) {
          final int currentIter = i;
          conn.query("SELECT " + currentIter).execute().onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(currentIter, row.getInteger(0));
            ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
            latch.countDown();
          }));
        }
      }));
  }

  @Test
  public void testContinuousOneShotPreparedQuery(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setPipeliningLimit(64);
    options.setCachePreparedStatements(false);
    AtomicInteger orderCheckCounter = new AtomicInteger(0);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(2000);
        for (int i = 0; i < 2000; i++) {
          final int currentIter = i;
          conn.preparedQuery("SELECT " + currentIter).execute().onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(currentIter, row.getInteger(0));
            ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
            latch.countDown();
          }));
        }
      }));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSql(TestContext ctx) {
    options.setPipeliningLimit(64);
    options.setCachePreparedStatements(true);
    AtomicInteger orderCheckCounter = new AtomicInteger(0);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(2000);
        for (int i = 0; i < 2000; i++) {
          final int currentIter = i;
          conn.preparedQuery("SELECT ?").execute(Tuple.of(currentIter)).onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(currentIter, row.getInteger(0));
            ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
            latch.countDown();
          }));
        }
      }));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSql(TestContext ctx) {
    // cache eviction auto closing
    options.setPipeliningLimit(64);
    options.setCachePreparedStatements(true);
    AtomicInteger orderCheckCounter = new AtomicInteger(0);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(2000);
        for (int i = 0; i < 2000; i++) {
          final int currentIter = i;
          conn.preparedQuery("SELECT " + currentIter).execute().onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(1, res.size());
            Row row = res.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(currentIter, row.getInteger(0));
            ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
            latch.countDown();
          }));
        }
      }));
  }

  @Test
  public void testPrepareAndExecuteWithDifferentSql(TestContext ctx) {
    options.setPipeliningLimit(64);
    AtomicInteger orderCheckCounter = new AtomicInteger(0);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        Async latch = ctx.async(1000);
        for (int i = 0; i < 1000; i++) {
          final int currentIter = i;
          conn.prepare("SELECT " + currentIter).onComplete(ctx.asyncAssertSuccess(ps -> {
            ps.query().execute().onComplete(ctx.asyncAssertSuccess(res -> {
              ctx.assertEquals(1, res.size());
              Row row = res.iterator().next();
              ctx.assertEquals(1, row.size());
              ctx.assertEquals(currentIter, row.getInteger(0));
              ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
              ps.close(ctx.asyncAssertSuccess(v -> {
                latch.countDown();
              }));
            }));
          }));
        }
      }));
  }

  @Test
  public void testOneShotPreparedBatchQuery(TestContext ctx) {
    options.setPipeliningLimit(64);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        List<Tuple> batchParams = new ArrayList<>();
        Async latch = ctx.async(1000);
        for (int i = 0; i < 1000; i++) {
          batchParams.add(Tuple.of(i));
        }
        conn.preparedQuery("SELECT ?")
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
            conn.close();
        }));
      }));
  }

  @Test
  public void testOneShotPreparedBatchInsert(TestContext ctx) {
    options.setPipeliningLimit(64);
    Async latch = ctx.async(1000);
    MySQLConnection.connect(vertx, options)
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        List<Tuple> batchParams = new ArrayList<>();
        for (int i = 0; i < 1000; i++) {
          batchParams.add(Tuple.of(i, String.format("val-%d", i)));
        }
        conn.preparedQuery("INSERT INTO mutable(id, val) VALUES (?, ?)")
          .executeBatch(batchParams)
          .onComplete(ctx.asyncAssertSuccess(res -> {
            for (int i = 0; i < 1000; i++) {
              ctx.assertEquals(1, res.rowCount());
              res = res.next();
              latch.countDown();
            }

            conn.query("SELECT id, val FROM mutable")
              .execute()
              .onComplete(ctx.asyncAssertSuccess(res2-> {
                ctx.assertEquals(1000, res2.size());
                int i = 0;
                for (Row row : res2) {
                  ctx.assertEquals(2, row.size());
                  ctx.assertEquals(i, row.getInteger(0));
                  ctx.assertEquals(String.format("val-%d", i), row.getString(1));
                  i++;
                }
                conn.close();
              }));
          }));
      }));
  }

  @Test
  public void testBatchException(TestContext ctx) {
    // TODO
  }

  private void cleanTestTable(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }
}
