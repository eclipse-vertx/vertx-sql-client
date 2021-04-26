package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.lang.reflect.Array;
import java.math.BigInteger;

public class PreparedStatementParamCoercionTest extends DataTypeTestBase {

  private static final Object[] VALUES_TO_COERCE = {
    (byte)5, (short)5, 5, 5L, 5f, 5d, BigInteger.valueOf(5), Numeric.create(5)
  };
  private static final String[] SQL_TYPES_TO_COERCE_TO = {
    "SMALLINT", "INTEGER", "BIGINT", "DECIMAL", "NUMERIC", "REAL", "DOUBLE PRECISION"
  };

  @Test
  public void testCoerceSingleParam(TestContext ctx) {
    Async async = ctx.async(VALUES_TO_COERCE.length * SQL_TYPES_TO_COERCE_TO.length);
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      for (String sqlType : SQL_TYPES_TO_COERCE_TO) {
        for (Object value: VALUES_TO_COERCE) {
          assertCoerceParam(conn, ctx, "SELECT 1 \"result\" WHERE $1::" + sqlType + "=5", value, async::countDown);
        }
      }
    }));
  }

  @Test
  public void testCoerceArrayParam(TestContext ctx) {
    Async async = ctx.async(VALUES_TO_COERCE.length * SQL_TYPES_TO_COERCE_TO.length);
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      for (String sqlType : SQL_TYPES_TO_COERCE_TO) {
        for (Object value: VALUES_TO_COERCE) {
          Object array = Array.newInstance(value.getClass(), 1);
          Array.set(array, 0, value);
          assertCoerceParam(conn, ctx, "SELECT 1 \"result\" WHERE ($1::" + sqlType + "[])=ARRAY[5::" + sqlType + "]", array, async::countDown);
        }
      }
    }));
  }

  private void assertCoerceParam(PgConnection conn, TestContext ctx, String query, Object value, Runnable cont) {
    conn
      .preparedQuery(query).execute(Tuple.of(value),
        ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          ctx.assertEquals(1, result.iterator().next().getInteger(0));
          cont.run();
        }));
  }

  @Test
  public void testCoercionError(TestContext ctx) {
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1::UUID", ctx.asyncAssertSuccess(pq -> {
        pq.query().execute(Tuple.of("not-an-uuid"), ctx.asyncAssertFailure(res -> {
        }));
      }));
    }));
  }
}
