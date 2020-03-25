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
import io.vertx.sqlclient.Transaction;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(VertxUnitRunner.class)
public class MySQLConnectionTestBase extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testTx(TestContext ctx) {
    Async async = ctx.async();
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("BEGIN").execute(ctx.asyncAssertSuccess(result1 -> {
        ctx.assertEquals(0, result1.size());
        ctx.assertNotNull(result1.iterator());
        conn.query("COMMIT").execute(ctx.asyncAssertSuccess(result2 -> {
          ctx.assertEquals(0, result2.size());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testTransactionCommit(TestContext ctx) {
    testTransactionCommit(ctx, Runnable::run);
  }

  @Test
  public void testTransactionCommitFromAnotherThread(TestContext ctx) {
    testTransactionCommit(ctx, t -> new Thread(t).start());
  }

  private void testTransactionCommit(TestContext ctx, Executor exec) {
    Async done = ctx.async();
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      deleteFromMutableTable(ctx, conn, () -> {
        exec.execute(() -> {
          Transaction tx = conn.begin();
          AtomicInteger u1 = new AtomicInteger();
          AtomicInteger u2 = new AtomicInteger();
          conn.query("INSERT INTO mutable (id, val) VALUES (1, 'val-1')").execute(ctx.asyncAssertSuccess(res1 -> {
            u1.addAndGet(res1.rowCount());
            exec.execute(() -> {
              conn.query("INSERT INTO mutable (id, val) VALUES (2, 'val-2')").execute(ctx.asyncAssertSuccess(res2 -> {
                u2.addAndGet(res2.rowCount());
                exec.execute(() -> {
                  tx.commit(ctx.asyncAssertSuccess(v -> {
                    ctx.assertEquals(1, u1.get());
                    ctx.assertEquals(1, u2.get());
                    conn.query("SELECT id FROM mutable WHERE id=1 OR id=2").execute(ctx.asyncAssertSuccess(result -> {
                      ctx.assertEquals(2, result.size());
                      done.complete();
                    }));
                  }));
                });
              }));
            });
          }));
        });
      });
    }));
  }

  @Test
  public void testTransactionRollback(TestContext ctx) {
    testTransactionRollback(ctx, Runnable::run);
  }

  @Test
  public void testTransactionRollbackFromAnotherThread(TestContext ctx) {
    testTransactionRollback(ctx, t -> new Thread(t).start());
  }

  private void testTransactionRollback(TestContext ctx, Executor exec) {
    Async done = ctx.async();
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      deleteFromMutableTable(ctx, conn, () -> {
        exec.execute(() -> {
          Transaction tx = conn.begin();
          AtomicInteger u1 = new AtomicInteger();
          AtomicInteger u2 = new AtomicInteger();
          conn.query("INSERT INTO mutable (id, val) VALUES (1, 'val-1')").execute(ctx.asyncAssertSuccess(res1 -> {
            u1.addAndGet(res1.rowCount());
            exec.execute(() -> {

            });
            conn.query("INSERT INTO mutable (id, val) VALUES (2, 'val-2')").execute(ctx.asyncAssertSuccess(res2 -> {
              u2.addAndGet(res2.rowCount());
              exec.execute(() -> {
                tx.rollback(ctx.asyncAssertSuccess(v -> {
                  ctx.assertEquals(1, u1.get());
                  ctx.assertEquals(1, u2.get());
                  conn.query("SELECT id FROM mutable WHERE id=1 OR id=2").execute(ctx.asyncAssertSuccess(result -> {
                    ctx.assertEquals(0, result.size());
                    done.complete();
                  }));
                }));
              });
            }));
          }));
        });
      });
    }));
  }

  @Test
  public void testTransactionAbort(TestContext ctx) {
    Async done = ctx.async();
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      deleteFromMutableTable(ctx, conn, () -> {
        Transaction tx = conn.begin();
        AtomicInteger failures = new AtomicInteger();
        tx.abortHandler(v -> ctx.assertEquals(0, failures.getAndIncrement()));
        AtomicReference<Boolean> queryAfterFailed = new AtomicReference<>();
        AtomicReference<Boolean> commit = new AtomicReference<>();
        conn.query("INSERT INTO mutable (id, val) VALUES (1, 'val-1')").execute(ar1 -> { });
        conn.query("INSERT INTO mutable (id, val) VALUES (1, 'val-2')").execute(ar2 -> {
          ctx.assertNotNull(queryAfterFailed.get());
          ctx.assertTrue(queryAfterFailed.get());
          ctx.assertNotNull(commit.get());
          ctx.assertTrue(commit.get());
          ctx.assertTrue(ar2.failed());
          ctx.assertEquals(1, failures.get());
          // This query won't be made in the same TX
          conn.query("SELECT id FROM mutable WHERE id=1").execute(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(0, result.size());
            done.complete();
          }));
        });
        conn.query("SELECT id FROM mutable").execute(result -> queryAfterFailed.set(result.failed()));
        tx.commit(result -> commit.set(result.failed()));
      });
    }));
  }
}
