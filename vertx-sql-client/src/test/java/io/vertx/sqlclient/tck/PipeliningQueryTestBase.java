package io.vertx.sqlclient.tck;

import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Supplier;

public abstract class PipeliningQueryTestBase {

  protected Vertx vertx;
  protected SqlConnectOptions options;
  AtomicInteger orderCheckCounter;

  protected Connector<SqlConnection> connectionConnector;
  protected Connector<SqlConnection> pooledConnectionConnector;
  protected Supplier<SqlClient> pooledClientSupplier;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    init();
    orderCheckCounter = new AtomicInteger(0);
    cleanTestTable(ctx);
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  protected abstract void init();

  protected abstract String statement(String... parts);

  protected String buildCounterPreparedQueryWithoutTable() {
    return statement("SELECT ", "");
  }

  @Test
  public void testContinuousSimpleQueryUsingConn(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> testSequentialQueryWithConnection(ctx, currentIter -> conn.query("SELECT " + currentIter).execute())));
  }

  @Test
  public void testContinuousSimpleQueryUsingPoolWithSingleConn(TestContext ctx) {
    pooledConnectionConnector.connect(ctx.asyncAssertSuccess(pooledConn -> {
      testSequentialQueryWithConnection(ctx, currentIter -> pooledConn.query("SELECT " + currentIter).execute());
    }));
  }

  @Test
  public void testContinuousSimpleQueryUsingPool(TestContext ctx) {
    SqlClient client = pooledClientSupplier.get();
    testQueryWithPool(ctx, currentIter -> client.query("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotPreparedQueryUsingConn(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> testSequentialQueryWithConnection(ctx, currentIter -> conn.preparedQuery("SELECT " + currentIter).execute())));
  }


  @Test
  public void testContinuousOneShotPreparedQueryUsingPoolWithSingleConn(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    pooledConnectionConnector.connect(ctx.asyncAssertSuccess(pooledConn -> {
      testSequentialQueryWithConnection(ctx, currentIter -> pooledConn.preparedQuery("SELECT " + currentIter).execute());
    }));
  }

  @Test
  public void testContinuousOneShotPreparedQueryUsingPool(TestContext ctx) {
    // one-shot preparedQuery auto closing
    options.setCachePreparedStatements(false);
    SqlClient client = pooledClientSupplier.get();
    testQueryWithPool(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> testSequentialQueryWithConnection(ctx, currentIter -> conn.preparedQuery(buildCounterPreparedQueryWithoutTable()).execute(Tuple.of(currentIter)))));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingPoolWithSingleConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    pooledConnectionConnector.connect(ctx.asyncAssertSuccess(pooledConn -> {
      testSequentialQueryWithConnection(ctx, currentIter -> pooledConn.preparedQuery(buildCounterPreparedQueryWithoutTable()).execute(Tuple.of(currentIter)));
    }));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithSameSqlUsingPool(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = pooledClientSupplier.get();
    testQueryWithPool(ctx, currentIter -> client.preparedQuery(buildCounterPreparedQueryWithoutTable()).execute(Tuple.of(currentIter)));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> testSequentialQueryWithConnection(ctx, currentIter -> conn.preparedQuery("SELECT " + currentIter).execute())));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingPoolWithSingleConn(TestContext ctx) {
    options.setCachePreparedStatements(true);
    pooledConnectionConnector.connect(ctx.asyncAssertSuccess(pooledConn -> {
      testSequentialQueryWithConnection(ctx, currentIter -> pooledConn.preparedQuery("SELECT " + currentIter).execute());
    }));
  }

  @Test
  public void testContinuousOneShotCachedPreparedQueryWithDifferentSqlUsingPool(TestContext ctx) {
    options.setCachePreparedStatements(true);
    SqlClient client = pooledClientSupplier.get();
    testQueryWithPool(ctx, currentIter -> client.preparedQuery("SELECT " + currentIter).execute());
  }

  @Test
  public void testPrepareAndExecuteWithDifferentSql(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
      Async latch = ctx.async(1000);
      for (int i = 0; i < 1000; i++) {
        final int currentIter = i;
        conn.prepare("SELECT " + currentIter).onComplete(ctx.asyncAssertSuccess(ps -> {
          ps.query().execute().onComplete(ctx.asyncAssertSuccess(res -> {
            checkSequentialQueryResult(ctx, res, currentIter, orderCheckCounter);
            ps.close().onComplete(ctx.asyncAssertSuccess(v -> {
              latch.countDown();
            }));
          }));
        }));
      }
    }));
  }

  @Test
  public void testOneShotPreparedBatchQueryConn(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
      testOneShotPreparedBatchQuery(ctx, conn);
    }));
  }

  @Test
  public void testOneShotPreparedBatchQueryPool(TestContext ctx) {
    SqlClient client = pooledClientSupplier.get();
    testOneShotPreparedBatchQuery(ctx, client);
  }

  private void testOneShotPreparedBatchQuery(TestContext ctx, SqlClient client) {
    List<Tuple> batchParams = new ArrayList<>();
    Async latch = ctx.async(1000);
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i));
    }
    client.preparedQuery(buildCounterPreparedQueryWithoutTable())
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertSuccess(res -> {
        for (int i = 0; i < 1000; i++) {
          ctx.assertEquals(1, res.size());
          Row row = res.iterator().next();
          ctx.assertEquals(1, row.size());
          ctx.assertEquals(i, row.getInteger(0));
          latch.countDown();
          res = res.next();
        }
        client.close();
      }));
  }

  @Test
  public void testOneShotPreparedBatchInsertConn(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
      testOneShotPreparedBatchInsert(ctx, conn);
    }));
  }

  @Test
  public void testOneShotPreparedBatchInsertPool(TestContext ctx) {
    SqlClient client = pooledClientSupplier.get();
    testOneShotPreparedBatchInsert(ctx, client);
  }

  private void testOneShotPreparedBatchInsert(TestContext ctx, SqlClient client) {
    Async latch = ctx.async(1000);
    List<Tuple> batchParams = new ArrayList<>();
    for (int i = 0; i < 1000; i++) {
      batchParams.add(Tuple.of(i, String.format("val-%d", i)));
    }

    client.preparedQuery(statement("INSERT INTO mutable(id, val) VALUES (", ", ",")"))
      .executeBatch(batchParams)
      .onComplete(ctx.asyncAssertSuccess(res -> {
        for (int i = 0; i < 1000; i++) {
          ctx.assertEquals(1, res.rowCount());
          res = res.next();
          latch.countDown();
        }

        client.query("SELECT id, val FROM mutable")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> {
            ctx.assertEquals(1000, res2.size());
            int i = 0;
            for (Row row : res2) {
              ctx.assertEquals(2, row.size());
              ctx.assertEquals(i, row.getInteger(0));
              ctx.assertEquals(String.format("val-%d", i), row.getString(1));
              i++;
            }
            client.close();
          }));
      }));
  }

  private void cleanTestTable(TestContext ctx) {
    connectionConnector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("TRUNCATE TABLE mutable;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        conn.close();
      }));
    }));
  }

  private void testSequentialQueryWithConnection(TestContext ctx, Function<Integer, Future<RowSet<Row>>> resultExecution) {
    Async latch = ctx.async(1000);
    for (int i = 0; i < 1000; i++) {
      final int currentIter = i;
      resultExecution.apply(currentIter).onComplete(ctx.asyncAssertSuccess(res -> {
        checkSequentialQueryResult(ctx, res, currentIter, orderCheckCounter);
        latch.countDown();
      }));
    }
  }

  private void testQueryWithPool(TestContext ctx, Function<Integer, Future<RowSet<Row>>> resultExecution) {
    Async latch = ctx.async(1000);
    for (int i = 0; i < 1000; i++) {
      final int currentIter = i;
      resultExecution.apply(currentIter).onComplete(ctx.asyncAssertSuccess(res -> {
        checkQueryResult(ctx, res, currentIter);
        latch.countDown();
      }));
    }
  }

  private void checkSequentialQueryResult(TestContext ctx, RowSet<Row> result, int currentIter, AtomicInteger orderCheckCounter) {
    ctx.assertEquals(1, result.size());
    Row row = result.iterator().next();
    ctx.assertEquals(1, row.size());
    ctx.assertEquals(currentIter, row.getInteger(0));
    ctx.assertEquals(currentIter, orderCheckCounter.getAndIncrement());
  }

  private void checkQueryResult(TestContext ctx, RowSet<Row> result, int currentIter) {
    ctx.assertEquals(1, result.size());
    Row row = result.iterator().next();
    ctx.assertEquals(1, row.size());
    ctx.assertEquals(currentIter, row.getInteger(0));
  }
}
