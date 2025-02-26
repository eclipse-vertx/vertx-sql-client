package io.vertx.tests.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.tests.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CharacterTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {

  @Test
  public void testDecodeName(TestContext ctx) {
    testDecode(ctx, "SELECT 'What is my name ?'::NAME", Tuple::getString, "What is my name ?");
  }

  @Test
  public void testEncodeName(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"Name\" = upper($1) WHERE \"id\" = $2 RETURNING \"Name\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addString("vert.x").addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String name = "VERT.X";
            ColumnChecker.checkColumn(0, "Name")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeChar(TestContext ctx) {
    testDecode(ctx, "SELECT 'A'::CHAR", Tuple::getString, "A");
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"SingleChar\" = upper($1) WHERE \"id\" = $2 RETURNING \"SingleChar\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addString("b").addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String singleChar = "B";
            ColumnChecker.checkColumn(0, "SingleChar")
              .returns(Tuple::getValue, Row::getValue, singleChar)
              .returns(Tuple::getString, Row::getString, singleChar)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeFixedChar(TestContext ctx) {
    testDecode(ctx, "SELECT 'YES'::CHAR(3)", Tuple::getString, "YES");
  }

  @Test
  public void testEncodeFixedChar(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"FixedChar\" = upper($1) WHERE \"id\" = $2 RETURNING \"FixedChar\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addString("no").addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String name = "NO ";
            ColumnChecker.checkColumn(0, "FixedChar")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeText(TestContext ctx) {
    testDecode(ctx, "SELECT 'Hello World'::TEXT", Tuple::getString, "Hello World");
  }

  @Test
  public void testEncodeText(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"Text\" = upper($1) WHERE \"id\" = $2 RETURNING \"Text\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addString("Hello World").addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String name = "HELLO WORLD";
            ColumnChecker.checkColumn(0, "Text")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeVarchar(TestContext ctx) {
    testDecode(ctx, "SELECT 'Great!'::VARCHAR", Tuple::getString, "Great!");
  }

  @Test
  public void testEncodeVarCharacter(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"CharacterDataType\" SET \"VarCharacter\" = upper($1) WHERE \"id\" = $2 RETURNING \"VarCharacter\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addString("Great!").addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            String name = "GREAT!";
            ColumnChecker.checkColumn(0, "VarCharacter")
              .returns(Tuple::getValue, Row::getValue, name)
              .returns(Tuple::getString, Row::getString, name)
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeLargeVarchar(TestContext ctx) {
    int len = 2048;
    StringBuilder builder = new StringBuilder();
    for (int i = 0; i < len; i++) {
      builder.append((char) ('A' + (i % 26)));
    }
    String value = builder.toString();
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT $1::VARCHAR(" + len + ")").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.of(value))
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(value, result.iterator().next().getString(0));
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testDecodeTextArray(TestContext ctx) {
    testDecode(ctx, "SELECT '{ \"Knock, knock.Who’s there?very long pause... Java.\" }'::TEXT[]", Tuple::getArrayOfStrings, (Object) new String[] {"Knock, knock.Who’s there?very long pause... Java."});
  }

  @Test
  public void testEncodeStringArray(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"ArrayDataType\" SET \"Text\" = $1  WHERE \"id\" = $2 RETURNING \"Text\"").onComplete(
        ctx.asyncAssertSuccess(p -> {
          p.query()
            .execute(Tuple.tuple().addArrayOfString(new String[]{"Knock, knock.Who’s there?"}).addInteger(2))
            .onComplete(ctx.asyncAssertSuccess(result -> {
              ColumnChecker.checkColumn(0, "Text")
                .returns(Tuple::getValue, Row::getValue, new String[]{"Knock, knock.Who’s there?"})
                .returns(Tuple::getArrayOfStrings, Row::getArrayOfStrings, new String[]{"Knock, knock.Who’s there?"})
                .forRow(result.iterator().next());
              async.complete();
            }));
        }));
    }));
  }
}
