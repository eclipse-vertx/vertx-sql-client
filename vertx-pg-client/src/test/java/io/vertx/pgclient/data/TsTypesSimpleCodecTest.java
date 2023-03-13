package io.vertx.pgclient.data;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TsTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {

  @Test
  public void test_ts_query(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'fat & rat'::tsquery as \"TsQuery\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        String expected = "'fat' & 'rat'";
        ColumnChecker.checkColumn(0, "TsQuery")
          .returns(Tuple::getString, Row::getString, expected)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(String.class, expected)
          .forRow(row);
          async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_query_null(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT null::tsquery as \"TsQuery\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        String expected = null;
        ColumnChecker.checkColumn(0, "TsQuery")
          .returnsNull()
          .forRow(row);
          async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_query_array(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT ARRAY ['fat & rat'::tsquery ] as \"TsQuery\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        System.out.println(row.getValue(0));
        System.out.println("Array: " + Stream.of(row.getArrayOfStrings(0)).collect(Collectors.joining(", ")));
        String[] expected = new String[]{"'fat' & 'rat'"};
        ColumnChecker.checkColumn(0, "TsQuery")
          .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, expected)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(String.class, expected)
          .forRow(row);
        async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_vector_simple(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'a fat cat sat on a mat and ate a fat rat'::tsvector as \"TsVector\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        String expected = "'a' 'and' 'ate' 'cat' 'fat' 'mat' 'on' 'rat' 'sat'";
        ColumnChecker.checkColumn(0, "TsVector")
          .returns(Tuple::getString, Row::getString, expected)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(String.class, expected)
          .forRow(row);
          async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_vector_null(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT null::tsvector as \"TsVector\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, "TsVector")
          .returnsNull()
          .forRow(row);
          async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_vector_parsed(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT to_tsvector('english', 'The Fat Rats') as \"TsVector\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        String expected = "'fat':2 'rat':3";
        ColumnChecker.checkColumn(0, "TsVector")
          .returns(Tuple::getString, Row::getString, expected)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(String.class, expected)
          .forRow(row);
          async.complete();
      }));
    }));
  }

  @Test
  public void test_ts_vector_array(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT ARRAY['a fat cat sat on a mat and ate a fat rat'::tsvector] as \"TsVector\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        String[] expected = new String[]{"'a' 'and' 'ate' 'cat' 'fat' 'mat' 'on' 'rat' 'sat'"};
        ColumnChecker.checkColumn(0, "TsVector")
          .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, expected)
          .returns(Tuple::getValue, Row::getValue, expected)
          .returns(String.class, expected)
          .forRow(row);
          async.complete();
      }));
    }));
  }

}
