/*
 * Copyright (C) 2017 Julien Viet
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
 *
 */
package io.vertx.pgclient;

import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class PgTransactionTest extends PgClientTestBase<Transaction> {

  private PgPool pool;

  public PgTransactionTest() {
    connector = handler -> {
      pool().begin(handler);
    };
  }

  private PgPool pool() {
    if (pool == null) {
      pool = PgPool.pool(vertx, new PgConnectOptions(options), new PoolOptions().setMaxSize(1));
    }
    return pool;
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
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO Fortune (id, message) VALUES ($1, $2);").execute(Tuple.of(13, "test message1"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, message from Fortune where id = 13").execute(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.rowCount());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(13, row.getInteger("id"));
            ctx.assertEquals("test message1", row.getString("message"));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testCommitWithQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("INSERT INTO Fortune (id, message) VALUES (14, 'test message2');").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, message from Fortune where id = 14").execute(ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.rowCount());
            Row row = rowSet.iterator().next();
            ctx.assertEquals(14, row.getInteger("id"));
            ctx.assertEquals("test message2", row.getString("message"));
            async.complete();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testRollbackPendingQueries(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(tx -> {
      tx.query("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure());
      tx.query("SELECT 1").execute(ctx.asyncAssertFailure(err -> {
        tx.rollback(ctx.asyncAssertFailure(v -> {
          // Already rolled back
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testLongTransaction(TestContext ctx) {
    Async async = ctx.async(2);
    pool().getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.exceptionHandler(err -> {
        PgException pgErr = (PgException) err;
        ctx.assertEquals("25P03", pgErr.getCode());
        async.countDown();
      });
      conn.closeHandler(v2 -> {
        async.countDown();
      });
      Transaction tx = conn.begin();
      tx.query("set idle_in_transaction_session_timeout = 500")
        .execute(ctx.asyncAssertSuccess(v -> {
        }));
    }));
  }
}
