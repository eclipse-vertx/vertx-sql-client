package io.vertx.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public class MySQLPreparedStatementTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuousPreparedQueriesWithSameTypeParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT id, message FROM immutable WHERE id = ? AND message = ?", ctx.asyncAssertSuccess(preparedQuery -> {
        preparedQuery.query().execute(Tuple.of(1, "fortune: No such file or directory"), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          preparedQuery.query().execute(Tuple.of(4, "After enough decimal places, nobody gives a damn."), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(0, res2.size());
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testContinuousPreparedQueriesWithDifferentTypeParameters(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT id, message FROM immutable WHERE id = ? AND message = ?", ctx.asyncAssertSuccess(preparedQuery -> {
        preparedQuery.query().execute(Tuple.of("1", "fortune: No such file or directory"), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          preparedQuery.query().execute(Tuple.of(4, "A bad random number generator: 1, 1, 1, 1, 1, 4.33e+67, 1, 1, 1"), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1, res2.size());
            conn.close();
          }));
        }));
      }));
    }));
  }

  @Test
  public void testContinuousOneShotPreparedQueriesWithDifferentTypeParameters(TestContext ctx) {
    options.setCachePreparedStatements(true);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT id, message FROM immutable WHERE id = ?")
        .execute(Tuple.of(1), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          Row row = res1.iterator().next();
          ctx.assertEquals("fortune: No such file or directory", row.getString("message"));

          conn.preparedQuery("SELECT id, message FROM immutable WHERE id = ?")
            .execute(Tuple.of("3"), ctx.asyncAssertSuccess(res2 -> {
              ctx.assertEquals(1, res2.size());
              Row row2 = res2.iterator().next();
              ctx.assertEquals("After enough decimal places, nobody gives a damn.", row2.getString("message"));
              conn.close();
            }));
        }));
    }));
  }

  @Test
  public void testContinuousOneShotPreparedQueriesWithBindingFailure(TestContext ctx) {
    options.setCachePreparedStatements(true);
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("SELECT id, message FROM immutable WHERE id = ?")
        .execute(Tuple.of(1), ctx.asyncAssertSuccess(res1 -> {
          ctx.assertEquals(1, res1.size());
          Row row = res1.iterator().next();
          ctx.assertEquals("fortune: No such file or directory", row.getString("message"));

          conn.preparedQuery("SELECT id, message FROM immutable WHERE id = ?")
            .execute(Tuple.of(3, "USELESS PARAM"), ctx.asyncAssertFailure(err -> {
              ctx.assertEquals("The number of parameters to execute should be consistent with the expected number of parameters = [1] but the actual number is [2].", err.getMessage());
              conn.close();
            }));
        }));
    }));
  }

  @Test
  public void testMaxPreparedStatementEviction(TestContext ctx) {
    testPreparedStatements(ctx, new MySQLConnectOptions(options).setCachePreparedStatements(true).setPreparedStatementCacheMaxSize(16), 128, 16);
  }

  @Test
  public void testOneShotPreparedStatements(TestContext ctx) {
    testPreparedStatements(ctx, new MySQLConnectOptions(options).setCachePreparedStatements(false), 128, 0);
  }

  private void testPreparedStatements(TestContext ctx, MySQLConnectOptions options, int num, int expected) {
    Assume.assumeFalse(MySQLTestBase.rule.isUsingMySQL5_6() || MySQLTestBase.rule.isUsingMariaDB());
    Async async = ctx.async();
    MySQLConnection.connect(vertx, options.setUser("root").setPassword("password"), ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT * FROM performance_schema.prepared_statements_instances").execute(ctx.asyncAssertSuccess(res1 -> {
        ctx.assertEquals(0, res1.size());
        AtomicInteger count = new AtomicInteger(num);
        for (int i = 0;i < num;i++) {
          int val = i;
          conn.preparedQuery("SELECT " + i).execute(Tuple.tuple(), ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1, res2.size());
            ctx.assertEquals(val, res2.iterator().next().getInteger(0));
            if (count.decrementAndGet() == 0) {
              ctx.assertEquals(num - 1, val);
              conn.query("SELECT * FROM performance_schema.prepared_statements_instances").execute(ctx.asyncAssertSuccess(res3 -> {
                ctx.assertEquals(expected, res3.size());
                conn.close();
                async.complete();
              }));
            }
          }));
        }
      }));
    }));
  }
}
