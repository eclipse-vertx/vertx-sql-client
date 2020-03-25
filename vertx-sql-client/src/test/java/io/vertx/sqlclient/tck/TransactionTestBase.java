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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

public abstract class TransactionTestBase {

  protected Pool pool;
  protected Vertx vertx;
  protected Consumer<Handler<AsyncResult<Transaction>>> connector;

  protected abstract void initConnector();

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
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.query("TRUNCATE TABLE mutable;").execute(ctx.asyncAssertSuccess(result -> {
        tx.commit();
      }));
    }));
  }

  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
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
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.rollback(ctx.asyncAssertSuccess(v1 -> {
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
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.abortHandler(v -> {
        // Try acquire the same connection on rollback
        pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      });
      // Failure will abort
      conn.query("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(result -> { }));
    }));
  }

  @Test
  public void testCommitWithPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.preparedQuery(statement("INSERT INTO mutable (id, val) VALUES (", ",", ");"))
        .execute(Tuple.of(13, "test message1"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        tx.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, val from mutable where id = 13").execute(ctx.asyncAssertSuccess(rowSet -> {
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
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.query("INSERT INTO mutable (id, val) VALUES (14, 'test message2');")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        tx.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, val from mutable where id = 14")
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
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.query("UPDATE immutable SET message = 'roll me back' WHERE id = 7")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        tx.rollback(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, message from immutable where id = 7")
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
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.query("INSERT INTO mutable (id, val) VALUES (15, 'wait for it...')")
        .execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        // Should find the data within the same transaction
        tx.query("SELECT id, val from mutable WHERE id = 15")
          .execute(ctx.asyncAssertSuccess(txRows -> {
          ctx.assertEquals(1, txRows.size());
          Row r = txRows.iterator().next();
          ctx.assertEquals(15, r.getInteger("id"));
          ctx.assertEquals("wait for it...", r.getString("val"));
          // Should NOT find the data from outside of the transaction
          nonTxPool.query("SELECT id, val from mutable WHERE id = 15")
            .execute(ctx.asyncAssertSuccess(notFound -> {
            ctx.assertEquals(0, notFound.size());
            tx.commit(ctx.asyncAssertSuccess(nonTxRows -> {
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
}
