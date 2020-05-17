/*
 * Copyright (C) 2020 IBM Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.sqlclient.tck;

import java.util.function.Consumer;

import io.vertx.core.Future;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

public abstract class TransactionTestBase {

  protected static class Result {
    public final SqlClient client;
    public final Transaction tx;
    public Result(SqlClient client, Transaction tx) {
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
      pool.getConnection(ar1 -> {
        if (ar1.succeeded()) {
          SqlConnection conn = ar1.result();
          conn.begin(ar2 -> {
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
    vertx.close(ctx.asyncAssertSuccess());
  }

  protected void cleanTestTable(TestContext ctx) {
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        res.tx.commit(ctx.asyncAssertSuccess());
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        res.tx.commit(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
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
      res.client.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        res.tx.rollback(ctx.asyncAssertSuccess(v1 -> {
          // Try acquire a connection
          pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnSetRollback(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.tx.completion().onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(TransactionRollbackException.INSTANCE, err);
        async.complete();
      }));
      // Failure will abort
      res.client.query("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(result -> { }));
    }));
  }

  @Test
  public void testCommitWithPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(res -> {
      res.client.preparedQuery(statement("INSERT INTO mutable (id, val) VALUES (", ",", ");"))
        .execute(Tuple.of(13, "test message1"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx.commit(ctx.asyncAssertSuccess(v1 -> {
            res.client.query("SELECT id, val from mutable where id = 13").execute(ctx.asyncAssertSuccess(rowSet -> {
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
      res.client.query("INSERT INTO mutable (id, val) VALUES (14, 'test message2');")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx.commit(ctx.asyncAssertSuccess(v1 -> {
          res.client.query("SELECT id, val from mutable where id = 14")
            .execute(ctx.asyncAssertSuccess(rowSet -> {
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
      res.client.query("UPDATE immutable SET message = 'roll me back' WHERE id = 7")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
          res.tx.rollback(ctx.asyncAssertSuccess(v1 -> {
          res.client.query("SELECT id, message from immutable where id = 7")
            .execute(ctx.asyncAssertSuccess(rowSet -> {
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
      res.client.query("INSERT INTO mutable (id, val) VALUES (15, 'wait for it...')")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        // Should find the data within the same transaction
          res.client.query("SELECT id, val from mutable WHERE id = 15")
          .execute(ctx.asyncAssertSuccess(txRows -> {
          ctx.assertEquals(1, txRows.size());
          Row r = txRows.iterator().next();
          ctx.assertEquals(15, r.getInteger("id"));
          ctx.assertEquals("wait for it...", r.getString("val"));
          // Should NOT find the data from outside of the transaction
          nonTxPool.query("SELECT id, val from mutable WHERE id = 15")
            .execute(ctx.asyncAssertSuccess(notFound -> {
            ctx.assertEquals(0, notFound.size());
              res.tx.commit(ctx.asyncAssertSuccess(nonTxRows -> {
              nonTxPool.query("SELECT id, val from mutable WHERE id = 15")
                .execute(ctx.asyncAssertSuccess(nonTxFound -> {
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
          .execute(ctx.asyncAssertSuccess(rows -> {
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
      .onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertEquals(failure, err);
        pool
          .query("SELECT id, val FROM mutable")
          .execute(ctx.asyncAssertSuccess(rows -> {
            ctx.assertEquals(0, rows.size());
            async.complete();
          }));
      })));
  }

  @Test
  public void testWithTransactionImplicitRollback(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool();
    pool.withTransaction(client -> client
      .query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
      .execute()
      .mapEmpty()
      .flatMap(v -> client
        .query("INVALID")
        .execute())
      .onComplete(ctx.asyncAssertFailure(err-> {
        pool
          .query("SELECT id, val FROM mutable")
          .execute(ctx.asyncAssertSuccess(rows -> {
            ctx.assertEquals(0, rows.size());
            async.complete();
          }));
      })));
  }


  @Test
  public void testStartReadOnlyTransaction(TestContext ctx) {
    Async async = ctx.async();
    getPool().getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.begin(new TransactionOptions().setTransactionAccessMode(TransactionAccessMode.READ_ONLY), ctx.asyncAssertSuccess(transaction -> {
        conn.query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
          .execute(ctx.asyncAssertFailure(error -> {
            // read-only transactions
            transaction.rollback();
            conn.close();
            async.complete();
          }));
      }));
    }));
  }

  @Test
  public void testWithReadOnlyTransactionStart(TestContext ctx) {
    Async async = ctx.async();
    getPool().withTransaction(new TransactionOptions().setTransactionAccessMode(TransactionAccessMode.READ_ONLY), client -> client
    .query("INSERT INTO mutable (id, val) VALUES (1, 'hello-1')")
    .execute()
    .onComplete(ctx.asyncAssertFailure(error -> {
      // read-only transactions
      async.complete();
    })));
  }
}
