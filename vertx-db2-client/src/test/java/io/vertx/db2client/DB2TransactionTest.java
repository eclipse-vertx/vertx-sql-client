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
package io.vertx.db2client;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlClient;
import io.vertx.sqlclient.Transaction;
import io.vertx.sqlclient.Tuple;

@RunWith(VertxUnitRunner.class)
public class DB2TransactionTest {
  
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;
  
  private Vertx vertx;
  private DB2Pool pool;
  private Consumer<Handler<AsyncResult<Transaction>>> connector;
  
  @BeforeClass
  public static void beforeAll(TestContext ctx) throws Exception {
    DB2Pool pool = DB2Pool.pool(Vertx.vertx(), new DB2ConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
    pool.query("DELETE FROM mutable", ctx.asyncAssertSuccess(result -> {}));
  }
  
  @Before
  public void setup() throws Exception {
    vertx = Vertx.vertx();
    connector = handler -> {
      if (pool == null) {
        pool = DB2Pool.pool(vertx, new DB2ConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
      }
      pool.begin(handler);
    };
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }
  
  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
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
      conn.query("UPDATE Fortune SET message = 'Whatever' WHERE id = 9", ctx.asyncAssertSuccess(result -> {
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
      conn.query("SELECT whatever from DOES_NOT_EXIST", ctx.asyncAssertFailure(result -> { }));
    }));
  }

  @Test
  public void testCommitWithPreparedQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO mutable (id, val) VALUES (?, ?);", Tuple.of(13, "test message1"), ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, val from mutable where id = 13", ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.rowCount());
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
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("INSERT INTO mutable (id, val) VALUES (14, 'test message2');", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.commit(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, val from mutable where id = 14", ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.rowCount());
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
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("UPDATE immutable SET message = 'roll me back' WHERE id = 7", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        conn.rollback(ctx.asyncAssertSuccess(v1 -> {
          pool.query("SELECT id, message from immutable where id = 7", ctx.asyncAssertSuccess(rowSet -> {
            ctx.assertEquals(1, rowSet.rowCount());
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
    DB2Pool nonTxPool = DB2Pool.pool(vertx, new DB2ConnectOptions(rule.options()), new PoolOptions().setMaxSize(1));
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn.query("INSERT INTO mutable (id, val) VALUES (15, 'wait for it...')", ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.rowCount());
        // Should find the data within the same transaction
        conn.query("SELECT id, val from mutable WHERE id = 15", ctx.asyncAssertSuccess(txRows -> {
          ctx.assertEquals(1, txRows.rowCount());
          Row r = txRows.iterator().next();
          ctx.assertEquals(15, r.getInteger("id"));
          ctx.assertEquals("wait for it...", r.getString("val"));
          // Should NOT find the data from outside of the transaction
          nonTxPool.query("SELECT id, val from mutable WHERE id = 15", ctx.asyncAssertSuccess(notFound -> {
            ctx.assertEquals(0, notFound.rowCount());
            conn.commit(ctx.asyncAssertSuccess(nonTxRows -> {
              nonTxPool.query("SELECT id, val from mutable WHERE id = 15", ctx.asyncAssertSuccess(nonTxFound -> {
                // After commiting the transaction, the data should be visible from other connections
                ctx.assertEquals(1, nonTxFound.rowCount());
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
