package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class JsonTypesExtendedCodecTest extends ExtendedQueryDataTypeCodecTestBase {
  @Test
  public void testJSON(TestContext ctx) {
    testJson(ctx, "JSON");
  }

  @Test
  public void testJSONB(TestContext ctx) {
    testJson(ctx, "JSONB");
  }

  private void testJson(TestContext ctx, String jsonType) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: " + jsonType + ")) AS t (c)",
      new Json[]{
        Json.create(10),
        Json.create(true),
        Json.create("hello"),
        Json.create(new JsonObject().put("foo", "bar")),
        Json.create(new JsonArray().add(0).add(1).add(2))
      }, Tuple::getJson);
  }

  @Test
  public void testJSONArray(TestContext ctx) {
    testJsonArray(ctx, "JSON");
  }

  @Test
  public void testJSONBArray(TestContext ctx) {
    testJsonArray(ctx, "JSONB");
  }

  private void testJsonArray(TestContext ctx, String jsonType) {
    testGeneric(ctx,
      "SELECT c FROM (VALUES ($1 :: " + jsonType + "[])) AS t (c)",
      new Json[][]{
        new Json[]{Json.create(10),
          Json.create(true),
          Json.create("hello"),
          Json.create(new JsonObject().put("foo", "bar")),
          Json.create(new JsonArray().add(0).add(1).add(2))}
      }, Tuple::getJsonArray);
  }

  @Test
  public void testDecodeJson(TestContext ctx) {
    testDecodeJson(ctx, "JsonDataType");
  }

  @Test
  public void testDecodeJsonb(TestContext ctx) {
    testDecodeJson(ctx, "JsonbDataType");
  }

  private void testDecodeJson(TestContext ctx, String tableName) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"Null\" FROM \"" + tableName + "\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
            JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJson, Row::getJson, Json.create(object))
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJson, Row::getJson, Json.create(array))
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getJson, Row::getJson, Json.create(4))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getJson, Row::getJson, Json.create("Hello World"))
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getJson, Row::getJson, Json.create(true))
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getJson, Row::getJson, Json.create(false))
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
              .returns(Tuple::getJson, Row::getJson, Json.create(null))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }

  @Test
  public void testEncodeJson(TestContext ctx) {
    testEncodeJson(ctx, "JsonDataType");
  }

  @Test
  public void testEncodeJsonb(TestContext ctx) {
    testEncodeJson(ctx, "JsonbDataType");
  }

  private void testEncodeJson(TestContext ctx, String tableName) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"" + tableName + "\" SET " +
          "\"JsonObject\" = $1, " +
          "\"JsonArray\" = $2, " +
          "\"Number\" = $3, " +
          "\"String\" = $4, " +
          "\"BooleanTrue\" = $5, " +
          "\"BooleanFalse\" = $6, " +
          "\"Null\" = $7 " +
          "WHERE \"id\" = $8 RETURNING \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"Null\"",
        ctx.asyncAssertSuccess(p -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
          JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
          p.execute(Tuple.tuple()
            .addJson(Json.create(object))
            .addJson(Json.create(array))
            .addJson(Json.create(4))
            .addJson(Json.create("Hello World"))
            .addJson(Json.create(true))
            .addJson(Json.create(false))
            .addJson(Json.create(null))
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, Json.create(object))
              .returns(Tuple::getJson, Row::getJson, Json.create(object))
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, Json.create(array))
              .returns(Tuple::getJson, Row::getJson, Json.create(array))
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, Json.create(4))
              .returns(Tuple::getJson, Row::getJson, Json.create(4))
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, Json.create("Hello World"))
              .returns(Tuple::getJson, Row::getJson, Json.create("Hello World"))
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, Json.create(true))
              .returns(Tuple::getJson, Row::getJson, Json.create(true))
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, Json.create(false))
              .returns(Tuple::getJson, Row::getJson, Json.create(false))
              .forRow(row);
            ColumnChecker.checkColumn(6, "Null")
              .returns(Tuple::getValue, Row::getValue, Json.create(null))
              .returns(Tuple::getJson, Row::getJson, Json.create(null))
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}
