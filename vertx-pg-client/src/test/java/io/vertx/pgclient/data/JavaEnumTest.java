package io.vertx.pgclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public class JavaEnumTest extends ExtendedQueryDataTypeCodecTestBase {

  public enum Mood {
    unhappy, ok, happy
  }

  @Test
  public void testJavaEnumToEnumColumn(TestContext ctx) {
    testJavaEnumToColumn(ctx, "'unhappy'", "Mood");
  }

  @Test
  public void testJavaEnumToTEXTColumn(TestContext ctx) {
    testJavaEnumToColumn(ctx, "'unhappy'", "TEXT");
  }

  @Test
  public void testJavaEnumToVARCHARColumn(TestContext ctx) {
    testJavaEnumToColumn(ctx, "'unhappy'", "VARCHAR");
  }

  @Test
  public void testJavaEnumToINT4Column(TestContext ctx) {
    testJavaEnumToColumn(ctx, "0", "INT4");
  }

  private void testJavaEnumToColumn(TestContext ctx, String value, String sqlType) {
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT * FROM (VALUES (" + value + " :: " + sqlType + ")) AS t (c)")
        .execute(ctx.asyncAssertSuccess(v -> {
          RowIterator<Row> it = v.iterator();
          ctx.assertTrue(it.hasNext());
          Row row = it.next();
          ctx.assertEquals(Mood.unhappy, row.get(Mood.class, 0));
        }));
    }));
  }

  @Test
  public void testJavaEnumToEnumArrayColumn(TestContext ctx) {
    testJavaEnumToArrayColumn(ctx, "ARRAY['unhappy']", "Mood[]");
  }

  @Test
  public void testJavaEnumToTEXTArrayColumn(TestContext ctx) {
    testJavaEnumToArrayColumn(ctx, "ARRAY['unhappy']", "TEXT[]");
  }

  @Test
  public void testJavaEnumToVARCHARArrayColumn(TestContext ctx) {
    testJavaEnumToArrayColumn(ctx, "ARRAY['unhappy']", "VARCHAR[]");
  }

  @Test
  public void testJavaEnumToINT4ArrayColumn(TestContext ctx) {
    testJavaEnumToArrayColumn(ctx, "ARRAY[0]", "INT4[]");
  }

  private void testJavaEnumToArrayColumn(TestContext ctx, String value, String sqlType) {
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT * FROM (VALUES (" + value + " :: " + sqlType + ")) AS t (c)")
        .execute(ctx.asyncAssertSuccess(v -> {
          RowIterator<Row> it = v.iterator();
          ctx.assertTrue(it.hasNext());
          Row row = it.next();
          Mood[] result = row.get(Mood[].class, "c");
          ctx.assertEquals(1, result.length);
          ctx.assertEquals(Mood.unhappy, result[0]);
        }));
    }));
  }

  @Test
  public void testJavaEnumToNullArrayColumn(TestContext ctx) {
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT NULL")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
          RowIterator<Row> it = v.iterator();
          ctx.assertTrue(it.hasNext());
          Row row = it.next();
          Mood[] result = row.get(Mood[].class, 0);
          ctx.assertNull(result);
        }));
    }));
  }

  @Test
  public void testJavaEnumToEnumParam(TestContext ctx) {
    testJavaEnumToParam(ctx, "happy", "Mood");
  }

  @Test
  public void testJavaEnumToVARCHARParam(TestContext ctx) {
    testJavaEnumToParam(ctx, "happy", "VARCHAR");
  }

  @Test
  public void testJavaEnumToTEXTParam(TestContext ctx) {
    testJavaEnumToParam(ctx, "happy", "TEXT");
  }

  @Test
  public void testJavaEnumToINT2Param(TestContext ctx) {
    testJavaEnumToParam(ctx, (short)Mood.happy.ordinal(), "INT2");
  }

  @Test
  public void testJavaEnumToINT4Param(TestContext ctx) {
    testJavaEnumToParam(ctx, Mood.happy.ordinal(), "INT4");
  }

  @Test
  public void testJavaEnumToINT8Param(TestContext ctx) {
    testJavaEnumToParam(ctx, (long)Mood.happy.ordinal(), "INT8");
  }

  private void testJavaEnumToParam(TestContext ctx, Object expected, String sqlType) {
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT $1 :: " + sqlType + " \"c\"")
        .execute(Tuple.of(Mood.happy), ctx.asyncAssertSuccess(v -> {
          RowIterator<Row> it = v.iterator();
          ctx.assertTrue(it.hasNext());
          Row row = it.next();
          ctx.assertEquals(expected, row.getValue(0));
        }));
    }));
  }

  @Test
  public void testJavaEnumToEnumArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, "happy", "Mood[]");
  }

  @Test
  public void testJavaEnumToVARCHARArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, "happy", "VARCHAR[]");
  }

  @Test
  public void testJavaEnumToTEXTArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, "happy", "TEXT[]");
  }

  @Test
  public void testJavaEnumToINT2ArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, (short)Mood.happy.ordinal(), "INT2[]");
  }

  @Test
  public void testJavaEnumToINT4ArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, Mood.happy.ordinal(), "INT4[]");
  }

  @Test
  public void testJavaEnumToINT8ArrayParam(TestContext ctx) {
    testJavaEnumToArrayParam(ctx, (long)Mood.happy.ordinal(), "INT8[]");
  }

  private void testJavaEnumToArrayParam(TestContext ctx, Object expected, String sqlType) {
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT $1 :: " + sqlType + " \"c\"")
        .execute(Tuple.of(new Mood[]{Mood.happy}), ctx.asyncAssertSuccess(v -> {
          RowIterator<Row> it = v.iterator();
          ctx.assertTrue(it.hasNext());
          Row row = it.next();
          Object value = row.getValue(0);
          ctx.assertTrue(value instanceof Object[]);
          Object[] array = (Object[]) value;
          ctx.assertEquals(1, array.length);
          ctx.assertEquals(expected, array[0]);
        }));
    }));
  }
}
