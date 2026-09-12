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
package io.vertx.tests.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

public abstract class TransactionTestBase {

  protected static class Result {
    public final SqlConnection client;
    public final Transaction tx;
    public Result(SqlConnection client, Transaction tx) {
      this.client = client;
      this.tx = tx;
    }
  }

  protected Pool pool;
  protected Vertx vertx;
  protected Consumer<Handler<AsyncResult<Result>>> connector;

  protected abstract Pool createPool();

  protected synchronized Pool getPool() {
    if (pool == null) {
      pool = createPool();
    }
    return pool;
  }

  protected void initConnector() {
    connector = handler -> {
      Pool pool = getPool();
      pool
        .getConnection()
        .onComplete(ar1 -> {
        if (ar1.succeeded()) {
          SqlConnection conn = ar1.result();
          conn
            .begin()
            .onComplete(ar2 -> {
            if (ar2.succeeded()) {
              Transaction tx = ar2.result();
              tx.completion().onComplete(ar3 -> {
                conn.close();
              });
              handler.handle(Future.succeededFuture(new Result(conn, tx)));
            } else {
              conn.close();
            }
          });
        } else {
          handler.handle(ar1.mapEmpty());
        }
      });
    };
  }

  protected abstract Pool nonTxPool();

  protected abstract String statement(String... parts);

  @Before
  public void setUp(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
    cleanTestTable(ctx);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected void cleanTestTable(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("TRUNCATE TABLE mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        res.tx.commit().onComplete(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        res.tx
          .commit()
          .onComplete(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool
            .getConnection()
            .onComplete(ctx.asyncAssertSuccess(v2 -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnRollback(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.completion().onComplete(ctx.asyncAssertFailure(err -> ctx.assertEquals(TransactionRollbackException.INSTANCE, err)));
      res.client
        .query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        res.tx
          .rollback()
          .onComplete(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool
            .getConnection()
            .onComplete(ctx.asyncAssertSuccess(v2 -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCommitWithPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .preparedQuery(statement("INSERT INTO mutable (id, val) VALUES (", ",", ")"))
        .execute(Tuple.of(13, "test message1"))
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx
            .commit()
            .onComplete(ctx.asyncAssertSuccess(v1 -> {
            res.client
              .query("SELECT id, val from mutable where id = 13")
              .execute()
              .onComplete(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.size());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(13, row.getInteger("id"));
            ctx.assertEquals("test message1", row.getString("val"));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCommitWithQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("INSERT INTO mutable (id, val) VALUES (14, 'test message2')")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx
            .commit()
            .onComplete(ctx.asyncAssertSuccess(v1 -> {
          res.client
            .query("SELECT id, val from mutable where id = 14")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.size());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(14, row.getInteger("id"));
            ctx.assertEquals("test message2", row.getString("val"));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testRollbackData(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("UPDATE immutable SET message = 'roll me back' WHERE id = 7")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx
            .rollback()
            .onComplete(ctx.asyncAssertSuccess(v1 -> {
          res.client
            .query("SELECT id, message from immutable where id = 7")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.size());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(7, row.getInteger("id"));
            ctx.assertEquals("Any program that runs right is obsolete.", row.getString("message"));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testDelayedCommit(TestContext ctx) {
    Pool nonTxPool = nonTxPool();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("INSERT INTO mutable (id, val) VALUES (15, 'wait for it...')")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        // Should find the data within the same transaction
          res.client
            .query("SELECT id, val from mutable WHERE id = 15")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(txRows -> {
          ctx.assertEquals(1, txRows.size());
          Row r = txRows.iterator().next();
          ctx.assertEquals(15, r.getInteger("id"));
          ctx.assertEquals("wait for it...", r.getString("val"));
          // Should NOT find the data from outside of the transaction
          nonTxPool
            .query("SELECT id, val from mutable WHERE id = 15")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(notFound -> {
            ctx.assertEquals(0, notFound.size());
              res.tx
                .commit()
                .onComplete(ctx.asyncAssertSuccess(nonTxRows -> {
              nonTxPool
                .query("SELECT id, val from mutable WHERE id = 15")
                .execute()
                .onComplete(ctx.asyncAssertSuccess(nonTxFound -> {
                // After commiting the transaction, the data should be visible from other connections
                ctx.assertEquals(1, nonTxFound.size());
                Row nonTxRow = nonTxFound.iterator().next();
                ctx.assertEquals(15, nonTxRow.getInteger("id"));
                ctx.assertEquals("wait for it...", nonTxRow.getString("val"));
                async.complete();
              }));
            }));
          }));
        }));
      }));
    }));
  }

  @Test
  public void testFailureWithPendingQueries(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client
        .query("SELECT whatever from DOES_NOT_EXIST")
        .execute()
        .onComplete(ctx.asyncAssertFailure(v -> {
      }));
      res.client
        .query("SELECT id, val FROM mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(err -> {
        res.tx.commit()
          .onComplete(ctx.asyncAssertSuccess(v -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testWithTransactionCommit(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool();
    pool.withTransaction(client -> client
      .query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
      .execute()
      .mapEmpty()
      .flatMap(v -> client
        .query("INSERT INTO mutable (id, val) VALUES (2, 'hello-2')")
        .execute()
        .mapEmpty()))
      .onComplete(ctx.asyncAssertSuccess(v -> {
        pool
          .query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(rows -> {
            ctx.assertEquals(2, rows.size());
            async.complete();
        }));
      }));
  }

  @Test
  public void testWithTransactionRollback(TestContext ctx) {
    Async async = ctx.async();
    Throwable failure = new Throwable();
    Pool pool = createPool();
    pool.withTransaction(client -> client
      .query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
      .execute()
      .mapEmpty()
      .flatMap(v -> Future.failedFuture(failure))
    )
      .onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(failure, err);
        pool
          .query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(rows -> {
            ctx.assertEquals(0, rows.size());
            async.complete();
          }));
      }));
  }

  @Test
  public void testWithTransactionImplicitRollback(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool();
    AtomicReference<Throwable> failure = new AtomicReference<>();
    pool.withTransaction(client -> client
      .query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
      .execute()
      .mapEmpty()
      .flatMap(v -> client
        .query("INVALID")
        .execute())
      .onFailure(failure::set)
    ).onComplete(ctx.asyncAssertFailure(err -> {
      ctx.assertEquals(err, failure.get());
      pool
        .query("SELECT id, val FROM mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(0, rows.size());
          async.complete();
        }));
    }));
  }

  @Test
  public void testWithPropagatableConnectionTransactionCommit(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool();
    vertx.runOnContext(handler -> {
    pool.withTransaction(TransactionPropagation.CONTEXT, c ->
      pool.withTransaction(TransactionPropagation.CONTEXT, conn ->
        conn.query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')").execute().mapEmpty()).flatMap(v ->
        pool.withTransaction(TransactionPropagation.CONTEXT, conn ->
          conn.query("INSERT INTO mutable (id, val) VALUES (2, 'hello-2')").execute().mapEmpty())).flatMap(v2 ->
        c.query("INSERT INTO mutable (id, val) VALUES (3, 'hello-3')").execute().mapEmpty())
    ).onComplete(ctx.asyncAssertSuccess(v -> pool
      .query("SELECT id, val FROM mutable")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(3, rows.size());
        ctx.assertNull(((ContextInternal)Vertx.currentContext()).getLocal("propagatable_connection"));
        async.complete();
      }))));
    });
  }

  @Test
  public void testWithPropagatableConnectionTransactionRollback(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool();
    Throwable failure = new Throwable();
    vertx.runOnContext(handler -> {
      pool.withTransaction(TransactionPropagation.CONTEXT, c ->
        pool.withTransaction(TransactionPropagation.CONTEXT, conn ->
          conn.query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')").execute().mapEmpty().flatMap(
            v -> Future.failedFuture(failure)))
      ).onComplete(ctx.asyncAssertFailure(v -> pool
        .query("SELECT id, val FROM mutable")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(0, rows.size());
          ctx.assertNull(((ContextInternal)Vertx.currentContext()).getLocal("propagatable_connection"));
          async.complete();
        }))));
    });
  }

  // ---------------------------------------------------------------------------
  // Savepoints
  //
  // Only the behaviour every database agrees on lives here. Whether a failed
  // statement also fails the surrounding transaction is database specific and is
  // covered by the driver test classes.
  // ---------------------------------------------------------------------------

  /**
   * Overridden by the drivers that implement savepoints.
   */
  protected boolean supportsSavepoints() {
    return false;
  }

  /**
   * Overridden by the drivers that create savepoints but cannot release one,
   * such as Microsoft SQL Server and Oracle.
   */
  protected boolean supportsSavepointRelease() {
    return true;
  }

  private void assumeSavepoints() {
    Assume.assumeTrue("driver does not support savepoints", supportsSavepoints());
  }

  private void assumeSavepointRelease() {
    assumeSavepoints();
    Assume.assumeTrue("driver cannot release a savepoint", supportsSavepointRelease());
  }

  protected Future<RowSet<Row>> insertMutable(SqlConnection client, int id, String val) {
    return client.query("INSERT INTO mutable (id, val) VALUES (" + id + ", '" + val + "')").execute();
  }

  protected Future<Void> assertMutableIds(TestContext ctx, int... expectedIds) {
    return getPool()
      .query("SELECT id FROM mutable ORDER BY id")
      .execute()
      .map(rows -> {
        ctx.assertEquals(expectedIds.length, rows.size());
        int index = 0;
        for (Row row : rows) {
          ctx.assertEquals(expectedIds[index++], row.getInteger("id").intValue());
        }
        return null;
      });
  }

  @Test
  public void testRollbackToSavepointUndoesWorkAfterIt(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> insertMutable(res.client, 2, "rolled-back")
          .compose(v -> sp.rollback())
          .compose(v -> insertMutable(res.client, 3, "after"))
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 1, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackInnerThenOuterSavepointKeepsOnlyWorkBeforeOuter(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before-sp1")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp1 -> insertMutable(res.client, 2, "between-sp1-sp2")
          .compose(v -> res.tx.createSavepoint())
          .compose(sp2 -> insertMutable(res.client, 3, "after-sp2")
            .compose(v -> sp2.rollback())
            .compose(v -> insertMutable(res.client, 4, "after-sp2-rollback"))
            .compose(v -> sp1.rollback())
            .compose(v -> insertMutable(res.client, 5, "after-sp1-rollback"))
            .compose(v -> res.tx.commit())))
        .compose(v -> assertMutableIds(ctx, 1, 5))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackToSameSavepointTwice(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "first")
          .compose(v -> sp.rollback())
          .compose(v -> insertMutable(res.client, 2, "second"))
          .compose(v -> sp.rollback())
          .compose(v -> insertMutable(res.client, 3, "third"))
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCanCreateNewSavepointAfterRollbackToSavepoint(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp1 -> insertMutable(res.client, 1, "first")
          .compose(v -> sp1.rollback())
          .compose(v -> res.tx.createSavepoint())
          .compose(sp2 -> insertMutable(res.client, 2, "second")
            .compose(v -> sp2.rollback())
            .compose(v -> insertMutable(res.client, 3, "third"))
            .compose(v -> res.tx.commit())))
        .compose(v -> assertMutableIds(ctx, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testWholeTransactionRollbackDiscardsSavepointWork(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> insertMutable(res.client, 2, "after")
          .compose(v -> res.tx.rollback()))
        .compose(v -> assertMutableIds(ctx))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testReleaseSavepointKeepsWork(TestContext ctx) {
    assumeSavepointRelease();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "released-scope")
          .compose(v -> sp.release())
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 1))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testReleaseReleasedSavepointFails(TestContext ctx) {
    assumeSavepointRelease();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> sp.release().compose(v -> sp.release()))
        .onComplete(ctx.asyncAssertFailure(err -> {
          res.tx.commit().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
    }));
  }

  /**
   * A driver that cannot release a savepoint rejects the call rather than pretending
   * it worked, and the savepoint stays usable for a rollback.
   */
  @Test
  public void testReleaseIsRejectedWhenUnsupported(TestContext ctx) {
    assumeSavepoints();
    Assume.assumeFalse("driver releases savepoints", supportsSavepointRelease());
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "kept")
          .compose(v -> sp.release())
          .transform(ar -> {
            ctx.assertTrue(ar.failed(), "release should have been rejected");
            ctx.assertTrue(ar.cause() instanceof UnsupportedOperationException,
              "expected an UnsupportedOperationException but got " + ar.cause());
            return insertMutable(res.client, 2, "also-kept").compose(v -> sp.rollback());
          })
          .compose(v -> insertMutable(res.client, 3, "after-rollback"))
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCreateSavepointIsRejectedWhenUnsupported(TestContext ctx) {
    Assume.assumeFalse("driver supports savepoints", supportsSavepoints());
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertTrue(err instanceof UnsupportedOperationException,
            "expected an UnsupportedOperationException but got " + err);
          async.complete();
        }));
    }));
  }

  @Test
  public void testCreateSavepointAfterCommitFails(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.commit()
        .compose(v -> res.tx.createSavepoint())
        .onComplete(ctx.asyncAssertFailure(err -> async.complete()));
    }));
  }

  @Test
  public void testRollbackSavepointAfterCommitFails(TestContext ctx) {
    assumeSavepoints();
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> res.tx.commit().compose(v -> sp.rollback()))
        .onComplete(ctx.asyncAssertFailure(err -> async.complete()));
    }));
  }
}
