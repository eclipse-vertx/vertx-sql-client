package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.PgConnectOptions;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */
public abstract class ExtendedQueryDataTypeCodecTestBase extends DataTypeTestBase {
  private static <T> void compare(TestContext ctx, T expected, T actual) {
    if (expected != null && expected.getClass().isArray()) {
      ctx.assertNotNull(actual);
      ctx.assertTrue(actual.getClass().isArray());
      List expectedList = Arrays.asList((Object[]) expected);
      List actualList = Arrays.asList((Object[]) actual);
      ctx.assertEquals(expectedList, actualList);
    } else {
      ctx.assertEquals(expected, actual);
    }
  }

  @Override
  protected PgConnectOptions options() {
    return new PgConnectOptions(options).setCachePreparedStatements(false);
  }

  protected <T> void testGeneric(TestContext ctx, String sql, T[] expected, BiFunction<Row, Integer, T> getter) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      List<Tuple> batch = Stream.of(expected).map(Tuple::of).collect(Collectors.toList());
      conn.preparedBatch(sql, batch,
        ctx.asyncAssertSuccess(result -> {
          for (T n : expected) {
            ctx.assertEquals(result.size(), 1);
            Iterator<Row> it = result.iterator();
            Row row = it.next();
            compare(ctx, n, getter.apply(row, 0));
            compare(ctx, n, row.getValue(0));
            result = result.next();
          }
          ctx.assertNull(result);
          async.complete();
        }));
    }));
  }
}
