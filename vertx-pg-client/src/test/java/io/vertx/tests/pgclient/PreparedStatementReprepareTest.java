package io.vertx.tests.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class PreparedStatementReprepareTest extends PgTestBase {

  private Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testReprepareDoesNotMakeInflightNegativeWithEnabledCache(TestContext ctx) {
    testReprepareDoesNotMakeInflightNegative(ctx, true);
  }

  @Test
  public void testReprepareDoesNotMakeInflightNegativeWithDisabledCache(TestContext ctx) {
    testReprepareDoesNotMakeInflightNegative(ctx, false);
  }

  private void testReprepareDoesNotMakeInflightNegative(TestContext ctx, boolean cachePreparedStatements) {
    Async async = ctx.async();

    PgConnectOptions options = new PgConnectOptions(this.options)
      .setCachePreparedStatements(cachePreparedStatements);

    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      PgSocketConnection socket = (PgSocketConnection) ((SqlConnectionInternal) conn).unwrap();

      conn
        .preparedQuery("SELECT CONCAT('HELLO ', $1)")
        .execute(Tuple.of("WORLD"))
        .map(rows -> {
          RowSet<Row> result = rows;

          ctx.assertEquals(1, result.size());
          ctx.assertEquals("HELLO WORLD", result.iterator().next().getString(0));

          ctx.assertEquals(
            0,
            socket.inflight(),
            "Inflight count should be zero after reprepare query completion " +
              "(cachePreparedStatements=" + cachePreparedStatements + ")"
          );

          return rows;
        })
        .eventually(() -> conn.close())
        .onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
    }));
  }
}
