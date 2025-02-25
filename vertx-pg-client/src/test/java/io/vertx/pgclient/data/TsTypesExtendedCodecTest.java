package io.vertx.pgclient.data;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.tests.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

public class TsTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {

  @Test
  public void test_tsquery_and_tsvector(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT to_tsvector('english', $1 ) @@ to_tsquery('english', $2 ) as \"TsQuery\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addString("postgraduate")
            .addString("postgres:*"))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "TsQuery")
              .returns(Tuple::getBoolean, Row::getBoolean, Boolean.TRUE)
              .returns(Tuple::getValue, Row::getValue, Boolean.TRUE)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void test_tsvector(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT c \"TsVector\" FROM ( VALUES ( to_tsvector ('english', $1 ) )) as t (c)").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addString("postgraduate"))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String expected = "'postgradu':1";
            ColumnChecker.checkColumn(0, "TsVector")
              .returns(Tuple::getString, Row::getString, expected)
              .returns(Tuple::getValue, Row::getValue, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void test_tsvector_array(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT c \"TsVector\" FROM ( VALUES ( ARRAY[to_tsvector ('english', $1 ), to_tsvector ('english', $2 )] )) as t (c) GROUP BY c").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addString("postgraduate")
            .addString("a fat cat sat on a mat and ate a fat rat"))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String[] expected = new String[]{"'postgradu':1", "'ate':9 'cat':3 'fat':2,11 'mat':7 'rat':12 'sat':4"};
            ColumnChecker.checkColumn(0, "TsVector")
              .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, expected)
              .returns(Tuple::getValue, Row::getValue, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }


  @Test
  public void test_tsquery(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT c \"TsQuery\" FROM ( VALUES ( to_tsquery ('english', $1 ) )) as t (c)").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addString("Fat:ab & Cats"))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String expected = "'fat':AB & 'cat'";
            ColumnChecker.checkColumn(0, "TsQuery")
              .returns(Tuple::getString, Row::getString, expected)
              .returns(Tuple::getValue, Row::getValue, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void test_tsquery_array(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT c \"TsQuery\" FROM ( VALUES ( ARRAY[to_tsquery ('english', $1 ), to_tsquery ('english', $2 )] )) as t (c)").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p
            .query()
            .execute(Tuple.tuple()
            .addString("Fat:ab & Cats")
            .addString("super:*"))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String[] expected = new String[]{"'fat':AB & 'cat'", "'super':*"};
            ColumnChecker.checkColumn(0, "TsQuery")
              .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, expected)
              .returns(Tuple::getValue, Row::getValue, expected)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

}
