package io.reactiverse.pgclient.codec.simple;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.reactiverse.pgclient.codec.ColumnChecker;
import io.reactiverse.pgclient.codec.SimpleQueryDataTypeCodecTestBase;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class CharacterTypesTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testName(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X & VERT.X'::NAME \"Name\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Name")
            .returns(Tuple::getValue, Row::getValue, "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ")
            .returns(Tuple::getString, Row::getString, "VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X VERT.X ")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::CHAR(15) \"Char\" ", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Char")
            .returns(Tuple::getValue, Row::getValue, "pgClient       ")
            .returns(Tuple::getString, Row::getString, "pgClient       ")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleBlankPaddedChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'V'::CHAR \"Char\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Char")
            .returns(Tuple::getValue, Row::getValue, "V")
            .returns(Tuple::getString, Row::getString, "V")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testSingleChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'X'::\"char\" \"Character\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Character")
            .returns(Tuple::getValue, Row::getValue, "X")
            .returns(Tuple::getString, Row::getString, "X")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testVarChar(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'pgClient'::VARCHAR(15) \"Driver\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Driver")
            .returns(Tuple::getValue, Row::getValue, "pgClient")
            .returns(Tuple::getString, Row::getString, "pgClient")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testText(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 'Vert.x PostgreSQL Client'::TEXT \"Text\"", ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "Text")
            .returns(Tuple::getValue, Row::getValue, "Vert.x PostgreSQL Client")
            .returns(Tuple::getString, Row::getString, "Vert.x PostgreSQL Client")
            .forRow(row);
          async.complete();
        }));
    }));
  }

  @Test
  public void testDecodeCHARArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Char", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "01234567");
  }

  @Test
  public void testDecodeTEXTArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Text", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeVARCHARArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Varchar", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }

  @Test
  public void testDecodeNAMEArray(TestContext ctx) {
    testDecodeXXXArray(ctx, "Name", "ArrayDataType", Tuple::getStringArray, Row::getStringArray, "Knock, knock.Who’s there?very long pause….Java.");
  }
}
