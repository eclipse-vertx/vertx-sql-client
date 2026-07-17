/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package io.vertx.tests.pgclient.tck;

import io.vertx.core.Future;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgException;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.TransactionRollbackException;
import io.vertx.sqlclient.Tuple;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.tests.sqlclient.tck.TransactionTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;

@RunWith(VertxUnitRunner.class)
public class PgTransactionTest extends TransactionTestBase {

  @ClassRule
  public static final ContainerPgRule rule = ContainerPgRule.SHARED_INSTANCE;

  @Override
  protected Pool createPool() {
    return PgBuilder.pool().connectingTo(rule.options()).with(new PoolOptions().setMaxSize(1)).using(vertx).build();
  }

  @Override
  protected Pool nonTxPool() {
    return PgBuilder.pool().connectingTo(rule.options()).with(new PoolOptions().setMaxSize(1)).using(vertx).build();
  }

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  /**
   * PostgreSQL specific behavior that rolls back the transaction when a query fails.
   */
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
        .onComplete(ctx.asyncAssertFailure(err -> {
        res.tx
          .rollback()
          .onComplete(ctx.asyncAssertSuccess(v -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testDeferredConstraintTriggersRollbackOnCommit(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("INSERT INTO deferred_constraints (name, parent) values ('john', 'mike')")
        .execute()
        .compose(ok -> res.tx.commit())
        .onComplete(ctx.asyncAssertFailure(failure -> {
          PgException pgEx = (PgException) failure;
          // foreign key constraint violation
          ctx.assertEquals("23503", pgEx.getSqlState());
        }));
    }));
  }

  @Test
  public void testLongTransaction(TestContext ctx) {
    Async async = ctx.async(2);
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("set idle_in_transaction_session_timeout = 500")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
          res.client.exceptionHandler(err -> {
            PgException pgErr = (PgException) err;
            ctx.assertEquals("25P03", pgErr.getSqlState());
            async.countDown();
          });
          res.client.closeHandler(v2 -> {
            async.countDown();
          });
        }));
    }));
  }

  @Test
  public void testRollbackToSavepointRestoresTransaction(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> insertMutable(res.client, 2, "rolled-back")
          .compose(v -> insertMutable(res.client, 1, "duplicate"))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return sp.rollback()
              .compose(v -> sp.release())
              .compose(v -> insertMutable(res.client, 3, "after"))
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 1, 3))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackInnerSavepointKeepsOuterWork(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "base")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp1 -> insertMutable(res.client, 2, "outer")
          .compose(v -> res.tx.createSavepoint())
          .compose(sp2 -> insertMutable(res.client, 3, "inner")
            .compose(v -> sp2.rollback())
            .compose(v -> insertMutable(res.client, 4, "after-inner-rollback"))
            .compose(v -> sp1.release())
            .compose(v -> res.tx.commit())))
        .compose(v -> assertMutableIds(ctx, 1, 2, 4))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackInnerThenOuterSavepointKeepsOnlyWorkBeforeOuter(TestContext ctx) {
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
  public void testReleaseSavepointKeepsWork(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "released-scope")
          .compose(v -> sp.release())
          .compose(v -> insertMutable(res.client, 2, "after-release"))
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 1, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCommitCleansUpUnreleasedSavepoint(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "unreleased")
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 1))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testReleaseOuterSavepointInvalidatesInnerSavepoint(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "base")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp1 -> insertMutable(res.client, 2, "outer")
          .compose(v -> res.tx.createSavepoint())
          .compose(sp2 -> insertMutable(res.client, 3, "inner")
            .compose(v -> sp1.release())
            .compose(v -> sp2.rollback())
            .compose(v -> Future.<Void>failedFuture("Expected inner savepoint to be invalidated"))
            .recover(err -> {
              assertSqlState(ctx, err, "3B001");
              return res.tx.commit();
            })))
        .onComplete(ctx.asyncAssertFailure(err -> {
          assertTransactionRollback(ctx, err);
          assertMutableIds(ctx).onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
    }));
  }

  @Test
  public void testRollbackOuterSavepointInvalidatesInnerSavepointAndCanRecover(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "base")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp1 -> insertMutable(res.client, 2, "outer")
          .compose(v -> res.tx.createSavepoint())
          .compose(sp2 -> insertMutable(res.client, 3, "inner")
            .compose(v -> sp1.rollback())
            .compose(v -> sp2.release())
            .compose(v -> Future.<Void>failedFuture("Expected inner savepoint to be invalidated"))
            .recover(err -> {
              assertSqlState(ctx, err, "3B001");
              return sp1.rollback()
                .compose(v -> insertMutable(res.client, 4, "recovered"))
                .compose(v -> res.tx.commit());
            })))
        .compose(v -> assertMutableIds(ctx, 1, 4))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackReleasedSavepointFailsButTransactionCanCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> sp.release()
          .compose(v -> sp.rollback())
          .compose(v -> Future.<Void>failedFuture("Expected rollback on released savepoint to fail"))
          .recover(err -> {
            ctx.assertEquals("Savepoint already released", err.getMessage());
            return insertMutable(res.client, 1, "still-usable")
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 1))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testReleaseReleasedSavepointFailsButTransactionCanCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> sp.release()
          .compose(v -> sp.release())
          .compose(v -> Future.<Void>failedFuture("Expected second release to fail"))
          .recover(err -> {
            ctx.assertEquals("Savepoint already released", err.getMessage());
            return insertMutable(res.client, 1, "still-usable")
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 1))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCreateSavepointFailsWhileTransactionIsFailed(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "before-failure")
          .compose(v -> insertMutable(res.client, 1, "duplicate"))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return res.tx.createSavepoint()
              .compose(v -> Future.<Void>failedFuture("Expected create savepoint to fail in failed transaction"))
              .recover(err2 -> {
                assertSqlState(ctx, err2, "25P02");
                return sp.rollback()
                  .compose(v -> insertMutable(res.client, 2, "after-recovery"))
                  .compose(v -> res.tx.commit());
              });
          }))
        .compose(v -> assertMutableIds(ctx, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testReleaseSavepointFailsWhileTransactionIsFailed(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "before-failure")
          .compose(v -> insertMutable(res.client, 1, "duplicate"))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return sp.release()
              .compose(v -> Future.<Void>failedFuture("Expected release to fail in failed transaction"))
              .recover(err2 -> {
                assertSqlState(ctx, err2, "25P02");
                return sp.rollback()
                  .compose(v -> insertMutable(res.client, 2, "after-recovery"))
                  .compose(v -> res.tx.commit());
              });
          }))
        .compose(v -> assertMutableIds(ctx, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCanCreateNewSavepointAfterRollbackToSavepoint(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp1 -> insertMutable(res.client, 1, "before-failure")
          .compose(v -> insertMutable(res.client, 1, "duplicate"))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return sp1.rollback()
              .compose(v -> res.tx.createSavepoint())
              .compose(sp2 -> insertMutable(res.client, 2, "after-recovery")
                .compose(x -> sp2.release()))
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackToSavepointAfterRepeatedFailedTransactionStatusRestoresTransaction(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> insertMutable(res.client, 1, "duplicate")
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return res.client.query("SELECT 1")
              .execute()
              .compose(v -> Future.<Void>failedFuture("Expected failed transaction error"))
              .recover(err2 -> {
                assertSqlState(ctx, err2, "25P02");
                return sp.rollback()
                  .compose(v -> insertMutable(res.client, 2, "after-recovery"))
                  .compose(v -> res.tx.commit());
              });
          }))
        .compose(v -> assertMutableIds(ctx, 1, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackToSavepointAfterPreparedQueryFailureRestoresTransaction(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> res.client.preparedQuery("INSERT INTO mutable (id, val) VALUES ($1, $2)")
          .execute(Tuple.of(1, "duplicate"))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return sp.rollback()
              .compose(v -> insertMutable(res.client, 2, "after-recovery"))
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 1, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testRollbackToSavepointAfterPreparedBatchFailureRestoresTransaction(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      insertMutable(res.client, 1, "before")
        .compose(v -> res.tx.createSavepoint())
        .compose(sp -> res.client.preparedQuery("INSERT INTO mutable (id, val) VALUES ($1, $2)")
          .executeBatch(Arrays.asList(
            Tuple.of(2, "batch-before-error"),
            Tuple.of(1, "batch-duplicate"),
            Tuple.of(3, "batch-after-error")
          ))
          .compose(v -> Future.<Void>failedFuture("Expected duplicate key failure"))
          .recover(err -> {
            assertSqlState(ctx, err, "23505");
            return sp.rollback()
              .compose(v -> insertMutable(res.client, 4, "after-recovery"))
              .compose(v -> res.tx.commit());
          }))
        .compose(v -> assertMutableIds(ctx, 1, 4))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testSavepointCommandAlreadyInProgress(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        Future<Void> first = sp.release();
        Future<Void> second = sp.release();

        second.onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Savepoint command already in progress", err.getMessage());
          first
            .compose(v -> res.tx.commit())
            .compose(v -> assertMutableIds(ctx))
            .onComplete(ctx.asyncAssertSuccess(x -> async.complete()));
        }));
      }));
    }));
  }

  @Test
  public void testCreateSavepointAfterCommitRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("SELECT pg_sleep(0.2)")
        .execute();

      Future<Void> commit = res.tx.commit();

      res.tx.createSavepoint()
        .onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          commit.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
    }));
  }

  @Test
  public void testCreateSavepointAfterRollbackRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("SELECT pg_sleep(0.2)")
        .execute();

      Future<Void> rollback = res.tx.rollback();

      res.tx.createSavepoint()
        .onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          rollback.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
    }));
  }

  @Test
  public void testRollbackSavepointAfterCommitRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.client.query("SELECT pg_sleep(0.2)")
          .execute();

        Future<Void> commit = res.tx.commit();

        sp.rollback().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          commit.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
      }));
    }));
  }

  @Test
  public void testRollbackSavepointAfterRollbackRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.client.query("SELECT pg_sleep(0.2)")
          .execute();

        Future<Void> rollback = res.tx.rollback();

        sp.rollback().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          rollback.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseSavepointAfterCommitRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.client.query("SELECT pg_sleep(0.2)")
          .execute();

        Future<Void> commit = res.tx.commit();

        sp.release().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          commit.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseSavepointAfterRollbackRequestedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.client.query("SELECT pg_sleep(0.2)")
          .execute();

        Future<Void> rollback = res.tx.rollback();

        sp.release().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          rollback.onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
        }));
      }));
    }));
  }

  @Test
  public void testWholeTransactionRollbackWithSavepoint(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "before-whole-rollback")
          .compose(v -> insertMutable(res.client, 2, "still-rolled-back"))
          .compose(v -> res.tx.rollback()))
        .compose(v -> assertMutableIds(ctx))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  @Test
  public void testCreateSavepointAfterRollbackCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.rollback().onComplete(ctx.asyncAssertSuccess(v -> {
        res.tx.createSavepoint().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testCreateSavepointAfterCommitCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.commit().onComplete(ctx.asyncAssertSuccess(v -> {
        res.tx.createSavepoint().onComplete(ctx.asyncAssertFailure(err -> {
          ctx.assertEquals("Transaction already completed", err.getMessage());
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testRollbackSavepointAfterCommitCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.tx.commit().onComplete(ctx.asyncAssertSuccess(v -> {
          sp.rollback().onComplete(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Transaction already completed", err.getMessage());
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testRollbackSavepointAfterRollbackCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.tx.rollback().onComplete(ctx.asyncAssertSuccess(v -> {
          sp.rollback().onComplete(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Transaction already completed", err.getMessage());
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseSavepointAfterCommitCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.tx.commit().onComplete(ctx.asyncAssertSuccess(v -> {
          sp.release().onComplete(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Transaction already completed", err.getMessage());
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseSavepointAfterRollbackCompletedFails(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint().onComplete(ctx.asyncAssertSuccess(sp -> {
        res.tx.rollback().onComplete(ctx.asyncAssertSuccess(v -> {
          sp.release().onComplete(ctx.asyncAssertFailure(err -> {
            ctx.assertEquals("Transaction already completed", err.getMessage());
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseAfterRollbackToSameSavepoint(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.createSavepoint()
        .compose(sp -> insertMutable(res.client, 1, "one")
          .compose(v -> sp.rollback())
          .compose(v -> insertMutable(res.client, 2, "two"))
          .compose(v -> sp.release())
          .compose(v -> res.tx.commit()))
        .compose(v -> assertMutableIds(ctx, 2))
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }

  private Future<RowSet<Row>> insertMutable(SqlConnection client, int id, String val) {
    return client.query("INSERT INTO mutable (id, val) VALUES (" + id + ", '" + val + "')").execute();
  }

  private Future<Void> assertMutableIds(TestContext ctx, int... expectedIds) {
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

  private void assertSqlState(TestContext ctx, Throwable err, String sqlState) {
    ctx.assertTrue(err instanceof PgException);
    ctx.assertEquals(sqlState, ((PgException) err).getSqlState());
  }

  private void assertTransactionRollback(TestContext ctx, Throwable err) {
    ctx.assertTrue(err instanceof TransactionRollbackException);
  }
}
