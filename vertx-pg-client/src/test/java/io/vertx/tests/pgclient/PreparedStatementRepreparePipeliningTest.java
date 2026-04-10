package io.vertx.tests.pgclient;

import io.vertx.core.Future;
import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import io.vertx.sqlclient.Tuple;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntConsumer;

public class PreparedStatementRepreparePipeliningTest extends PreparedStatementTestBase {

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options)
      .setPipeliningLimit(1);
  }

  @Test
  public void testReprepareDoesNotBypassPipeliningLimitWithEnabledCache(TestContext ctx) {
    testReprepareDoesNotBypassPipeliningLimit(ctx, true);
  }

  @Test
  public void testReprepareDoesNotBypassPipeliningLimitWithDisabledCache(TestContext ctx) {
    testReprepareDoesNotBypassPipeliningLimit(ctx, false);
  }

  private void testReprepareDoesNotBypassPipeliningLimit(TestContext ctx, boolean cachePreparedStatements) {
    Async async = ctx.async();

    PgConnectOptions backend = options().setCachePreparedStatements(cachePreparedStatements);
    ProxyServer proxy = ProxyServer.create(vertx, backend.getPort(), backend.getHost());

    AtomicBoolean observe = new AtomicBoolean();
    AtomicInteger readyForQueryCount = new AtomicInteger();
    AtomicBoolean sawSecondQuery = new AtomicBoolean();
    AtomicBoolean secondQueryTooEarly = new AtomicBoolean();

    TaggedMessageScanner frontendScanner = new TaggedMessageScanner();
    TaggedMessageScanner backendScanner = new TaggedMessageScanner();

    proxy.proxyHandler(conn -> {
      conn.clientHandler(buff -> {
        if (observe.get()) {
          frontendScanner.handle(buff, tag -> {
            if (tag == 'Q') {
              sawSecondQuery.set(true);
              if (readyForQueryCount.get() < 3) {
                secondQueryTooEarly.set(true);
              }
            }
          });
        }
        conn.serverSocket().write(buff);
      });

      conn.serverHandler(buff -> {
        if (observe.get()) {
          backendScanner.handle(buff, tag -> {
            if (tag == 'Z') {
              readyForQueryCount.incrementAndGet();
            }
          });
        }
        conn.clientSocket().write(buff);
      });

      conn.connect();
    });

    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxied = new PgConnectOptions(backend)
        .setHost("localhost")
        .setPort(8080);

      PgConnection.connect(vertx, proxied).onComplete(ctx.asyncAssertSuccess(conn -> {
        observe.set(true);

        Future
          .all(
            conn.preparedQuery("WITH s AS (SELECT pg_sleep(1)) SELECT CONCAT('HELLO ', $1) FROM s")
              .execute(Tuple.of("WORLD")),
            conn.query("SELECT 1").execute()
          )
          .eventually(() -> conn.close())
          .onComplete(ctx.asyncAssertSuccess(ar -> {
            RowSet<Row> first = ar.result().resultAt(0);
            RowSet<Row> second = ar.result().resultAt(1);

            ctx.assertEquals(1, first.size());
            ctx.assertEquals("HELLO WORLD", first.iterator().next().getString(0));

            ctx.assertEquals(1, second.size());
            ctx.assertEquals(1, second.iterator().next().getInteger(0).intValue());

            ctx.assertTrue(
              sawSecondQuery.get(),
              "Test setup invalid: did not observe frontend simple-query ('Q') message for the second command"
            );

            ctx.assertFalse(
              secondQueryTooEarly.get(),
              "Second command was written too early before reprepare flow finished " +
                "(cachePreparedStatements=" + cachePreparedStatements +
                ", readyForQueryCount=" + readyForQueryCount.get() + ")"
            );

            async.complete();
          }));
      }));
    }));
  }

  private static final class TaggedMessageScanner {
    private Buffer pending = Buffer.buffer();

    void handle(Buffer incoming, IntConsumer tagHandler) {
      pending.appendBuffer(incoming);
      while (true) {
        if (pending.length() < 5) {
          return;
        }
        int len = pending.getInt(1);
        if (len < 4) {
          throw new IllegalStateException("Invalid PostgreSQL message length: " + len);
        }
        int frameLen = 1 + len;
        if (pending.length() < frameLen) {
          return;
        }
        int tag = pending.getByte(0) & 0xFF;
        tagHandler.accept(tag);
        if (pending.length() == frameLen) {
          pending = Buffer.buffer();
        } else {
          pending = pending.getBuffer(frameLen, pending.length());
        }
      }
    }
  }
}
