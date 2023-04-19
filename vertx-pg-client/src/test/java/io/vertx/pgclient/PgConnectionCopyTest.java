package io.vertx.pgclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

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
          pgConn.copyTo("COPY my_table TO STDOUT (FORMAT binary)")
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
}
