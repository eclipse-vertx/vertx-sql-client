package io.vertx.mysqlclient;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

@RunWith(VertxUnitRunner.class)
public class MySQLPoolTest extends MySQLTestBase {

  Vertx vertx;
  MySQLConnectOptions options;
  MySQLPool pool;

  @Rule
  public RepeatRule rule = new RepeatRule();

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
    pool = MySQLPool.pool(vertx, options, new PoolOptions());
  }

  @After
  public void tearDown(TestContext ctx) {
    if (pool != null) {
      pool.close();
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testContinuouslyConnecting(TestContext ctx) {
    Async async = ctx.async(3);
    pool.getConnection(ctx.asyncAssertSuccess(conn1 -> async.countDown()));
    pool.getConnection(ctx.asyncAssertSuccess(conn2 -> async.countDown()));
    pool.getConnection(ctx.asyncAssertSuccess(conn3 -> async.countDown()));
    async.await();
  }

  @Test
  public void testContinuouslyQuery(TestContext ctx) {
    Async async = ctx.async(3);
    pool.preparedQuery("SELECT 1").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool.preparedQuery("SELECT 2").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    pool.preparedQuery("SELECT 3").execute(ctx.asyncAssertSuccess(res1 -> {
      ctx.assertEquals(1, res1.size());
      async.countDown();
    }));
    async.await();
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    PoolOptions poolOptions = new PoolOptions().setMaxSize(2);
    MySQLPool pool = MySQLPool.pool(vertx, new MySQLConnectOptions(this.options).setCachePreparedStatements(false), poolOptions);
    try {
      int numRequests = 1500;
      Async async = ctx.async(numRequests);
      for (int i = 0;i < numRequests;i++) {
        pool.preparedQuery("SELECT * FROM Fortune WHERE id=?").execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
          ctx.assertEquals(1, results.size());
          Tuple row = results.iterator().next();
          ctx.assertEquals(1, row.getInteger(0));
          ctx.assertEquals("fortune: No such file or directory", row.getString(1));
          async.countDown();
        }));
      }
      async.awaitSuccess(10_000);
    } finally {
      pool.close();
    }
  }

  @Test
  @Repeat(50)
  public void testNoConnectionLeaks(TestContext ctx) {
    Tuple params = Tuple.of(options.getUser(), options.getDatabase());

    Async killConnections = ctx.async();
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      String sql = "SELECT ID FROM INFORMATION_SCHEMA.PROCESSLIST WHERE ID <> CONNECTION_ID() AND User = ? AND db = ?";
      Collector<Row, ?, List<Integer>> collector = mapping(row -> row.getInteger(0), toList());
      conn.preparedQuery(sql).collecting(collector).execute(params, ctx.asyncAssertSuccess(ids -> {
        CompositeFuture killAll = ids.value().stream()
          .map(connId -> {
            Promise prom = Promise.promise();
            conn.query("KILL " + connId).execute(prom);
            return prom.future();
          })
          .collect(Collectors.collectingAndThen(toList(), CompositeFuture::all));
        killAll.onComplete(ctx.asyncAssertSuccess(v -> {
          conn.close();
          killConnections.complete();
        }));
      }));
    }));
    killConnections.awaitSuccess();

    String sql = "SELECT CONNECTION_ID() AS cid, (SELECT count(*) FROM INFORMATION_SCHEMA.PROCESSLIST WHERE User = ? AND db = ?) AS cnt";

    int idleTimeout = 50;
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(1)
      .setIdleTimeout(idleTimeout)
      .setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    pool = MySQLPool.pool(options, poolOptions);

    Async async = ctx.async();
    AtomicInteger cid = new AtomicInteger();
    vertx.getOrCreateContext().runOnContext(v -> {
      pool.preparedQuery(sql).execute(params, ctx.asyncAssertSuccess(rs1 -> {
        Row row1 = rs1.iterator().next();
        cid.set(row1.getInteger("cid"));
        ctx.assertEquals(1, row1.getInteger("cnt"));
        vertx.setTimer(2 * idleTimeout, l -> {
          pool.preparedQuery(sql).execute(params, ctx.asyncAssertSuccess(rs2 -> {
            Row row2 = rs2.iterator().next();
            ctx.assertEquals(1, row2.getInteger("cnt"));
            ctx.assertNotEquals(cid.get(), row2.getInteger("cid"));
            async.complete();
          }));
        });
      }));
    });
    async.awaitSuccess();
  }
}
