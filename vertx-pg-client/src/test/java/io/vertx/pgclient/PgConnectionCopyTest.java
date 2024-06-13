package io.vertx.pgclient;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

public class PgConnectionCopyTest extends PgConnectionTestBase {
  public PgConnectionCopyTest() {
    connector = (handler) -> PgConnection.connect(vertx, options).onComplete(ar -> {
      handler.handle(ar.map(p -> p));
    });
  }

  @Test
  public void testCopyToRows(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        insertIntoTestTable(ctx, conn, 10, () -> {
          PgConnection pgConn = (PgConnection) conn;
          pgConn.copyToRows("COPY my_table TO STDOUT (FORMAT binary)")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result -> {
                for (int i = 0; i < 2; i++) {
                  ctx.assertEquals(1, result.rowCount());
                  result = result.next();
                }
                async.complete();
          }));
        });
      });
    }));
  }

  @Test
  public void testCopyToCsvBytes(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      deleteFromTestTable(ctx, conn, () -> {
        insertIntoTestTable(ctx, conn, 1, () -> {
          PgConnection pgConn = (PgConnection) conn;
          pgConn.copyToBytes("COPY Test TO STDOUT (FORMAT csv)")
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ctx.assertNull(result.columnDescriptors());
              ctx.assertEquals(10, result.rowCount());
              ctx.assertEquals(10, result.size());
              ctx.assertEquals(
                Buffer.buffer(
                  "0,Whatever-0\n" +
                  "1,Whatever-1\n" +
                  "2,Whatever-2\n" +
                  "3,Whatever-3\n" +
                  "4,Whatever-4\n" +
                  "5,Whatever-5\n" +
                  "6,Whatever-6\n" +
                  "7,Whatever-7\n" +
                  "8,Whatever-8\n" +
                  "9,Whatever-9\n"
                ),
                result.value()
              );
              async.complete();
            }));
        });
      });
    }));
  }

  /**
   * Just a thingy to eavesdrop protocol interactions.
   *
   * tips:
   * - frontend / backend protocol -> message flow -> binary
   *   - start with CommandBase, SimpleQueryCommandCodecBase, builder.executeSimpleQuery, QueryExecutor, QueryResultBuilder
   *   - PgDecoder
   *   - startup message
   *   - auth
   *   - Simple Query
   * - use wireshark - `tcp port 5432`
   *   - add VM option - port - such that it's fixed
   *
   * TODO: drop this.
   *
   * @param ctx
   */
  @Test
  public void testSimpleQuery(TestContext ctx) {
    Async async = ctx.async();
    connector.accept(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("select 1")
        .execute()
        // when does the result is transformed from bool to rows?
        .onComplete(ctx.asyncAssertSuccess(result1 -> {
          ctx.assertEquals(1, result1.size());
          async.complete();
        }));
    }));
  }
}
