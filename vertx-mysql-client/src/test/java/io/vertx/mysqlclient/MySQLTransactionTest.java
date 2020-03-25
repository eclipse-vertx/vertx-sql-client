package io.vertx.mysqlclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MySQLTransactionTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;
  MySQLPool pool;
  Consumer<Handler<AsyncResult<Transaction>>> connector;

  public MySQLTransactionTest() {
    connector = handler -> {
      if (pool == null) {
        pool = MySQLPool.pool(vertx, options, new PoolOptions().setMaxSize(1));
      }
      pool.begin(handler);
    };
  }

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @Test
  public void testReleaseConnectionOnCommit(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(transaction -> {
      deleteFromMutableTable(ctx, transaction, () -> {
        transaction.query("INSERT INTO mutable (id, val) VALUES (9, 'Whatever');").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.rowCount());
          transaction.commit(ctx.asyncAssertSuccess(v1 -> {
            // Try acquire a connection
            pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      });
    }));
  }

  @Test
  public void testReleaseConnectionOnRollback(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(transaction -> {
      deleteFromMutableTable(ctx, transaction, () -> {
        transaction.query("INSERT INTO mutable (id, val) VALUES (9, 'Whatever');").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.rowCount());
          transaction.rollback(ctx.asyncAssertSuccess(v1 -> {
            // Try acquire a connection
            pool.getConnection(ctx.asyncAssertSuccess(v2 -> {
              async.complete();
            }));
          }));
        }));
      });
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
    connector.accept(ctx.asyncAssertSuccess(transaction -> {
      deleteFromMutableTable(ctx, transaction, () -> {
        transaction.preparedQuery("INSERT INTO mutable (id, val) VALUES (?, ?)").execute(Tuple.of(13, "test message1"), ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.rowCount());
          transaction.commit(ctx.asyncAssertSuccess(v1 -> {
            pool.query("SELECT id, val from mutable where id = 13").execute(ctx.asyncAssertSuccess(rowSet -> {
              ctx.assertEquals(1, rowSet.size());
              Row row = rowSet.iterator().next();
              ctx.assertEquals(13, row.getInteger("id"));
              ctx.assertEquals("test message1", row.getString("val"));
              async.complete();
            }));
          }));
        }));
      });
    }));
  }

  @Test
  public void testCommitWithQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(transaction -> {
      deleteFromMutableTable(ctx, transaction, () -> {
        transaction.query("INSERT INTO mutable (id, val) VALUES (14, 'test message2');").execute(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.rowCount());
          transaction.commit(ctx.asyncAssertSuccess(v1 -> {
            pool.query("SELECT id, val from mutable where id = 14").execute(ctx.asyncAssertSuccess(rowSet -> {
              ctx.assertEquals(1, rowSet.size());
              Row row = rowSet.iterator().next();
              ctx.assertEquals(14, row.getInteger("id"));
              ctx.assertEquals("test message2", row.getString("val"));
              async.complete();
            }));
          }));
        }));
      });
    }));
  }
}
