package io.vertx.sqlclient;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public abstract class PreparedQueryCachedTestBase extends PreparedQueryTestBase {
  @Override
  public void setUp(TestContext ctx) throws Exception {
    super.setUp(ctx);
    options.setCachePreparedStatements(true);
  }

  @Test
  public void testConcurrent(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      Async[] asyncs = new Async[10];
      for (int i = 0; i < 10; i++) {
        asyncs[i] = ctx.async();
      }
      for (int i = 0; i < 10; i++) {
        Async async = asyncs[i];
        conn.prepare(statement("SELECT * FROM Fortune WHERE id=", ""), ctx.asyncAssertSuccess(ps -> {
          ps.execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
            ctx.assertEquals(1, results.size());
            Tuple row = results.iterator().next();
            ctx.assertEquals(1, row.getInteger(0));
            ctx.assertEquals("fortune: No such file or directory", row.getString(1));
            async.complete();
          }));
        }));
      }
    }));
  }
}
