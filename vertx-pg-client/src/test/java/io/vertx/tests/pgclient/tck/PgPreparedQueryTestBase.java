package io.vertx.tests.pgclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.sqlclient.Tuple;
import io.vertx.tests.sqlclient.tck.PreparedQueryTestBase;
import org.junit.ClassRule;
import org.junit.Test;

public abstract class PgPreparedQueryTestBase extends PreparedQueryTestBase {
  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  @Override
  protected String statement(String... parts) {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      if (i > 0) {
        sb.append("$").append((i));
      }
      sb.append(parts[i]);
    }
    return sb.toString();
  }

  @Test
  public void testMaximumParametersExceeded(TestContext ctx) {
    int cnt = 33000;
    StringBuilder valuesString = new StringBuilder(3 * 2 * cnt);
    Object[] values = new Object[2 * cnt];
    for (int i = 0; i < cnt; i++) {
      values[2 * i] = i;
      values[2 * i + 1] = "value-" + i;
      if (i > 0) {
        valuesString.append(',');
      }
      valuesString.append("($").append(2 * i + 1).append(',').append('$').append(2 * (i + 1)).append(')');
    }
    Tuple tuple = Tuple.wrap(values);
    String query = String.format("INSERT INTO mutable (id, val) VALUES %s", valuesString);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery(query)
        .execute(tuple)
        .onComplete(ar -> {
          ctx.assertTrue(ar.failed());
          ctx.assertTrue(ar.cause().getMessage().contains("exceeds the maximum of 65535"));
        });
    }));
  }
}
