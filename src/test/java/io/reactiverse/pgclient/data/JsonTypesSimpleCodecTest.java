package io.reactiverse.pgclient.data;

import io.reactiverse.pgclient.PgClient;
import io.reactiverse.pgclient.Row;
import io.reactiverse.pgclient.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

public class JsonTypesSimpleCodecTest extends SimpleQueryDataTypeCodecTestBase {
  @Test
  public void testJSONB(TestContext ctx) {
    testJson(ctx, "JSONB");
  }

  @Test
  public void testJSON(TestContext ctx) {
    testJson(ctx, "JSON");
  }

  private void testJson(TestContext ctx, String type) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT " +
        "'  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }'::" + type + " \"JsonObject\"," +
        "'  [1,true,null,9.5,\"Hi\" ] '::" + type + " \"JsonArray\"," +
        "' true '::" + type + " \"TrueValue\"," +
        "' false '::" + type + " \"FalseValue\"," +
        "' null '::" + type + " \"NullValue\"," +
        "' 7.502 '::" + type + " \"Number1\"," +
        "' 8 '::" + type + " \"Number2\"," +
        "'\" Really Awesome! \"'::" + type + " \"Text\"", ctx.asyncAssertSuccess(result -> {
        JsonObject object =  new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
        JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ColumnChecker.checkColumn(0, "JsonObject")
          .returns(Tuple::getValue, Row::getValue, Json.create(object))
          .forRow(row);
        ColumnChecker.checkColumn(1, "JsonArray")
          .returns(Tuple::getValue, Row::getValue, Json.create(array))
          .forRow(row);
        ColumnChecker.checkColumn(2, "TrueValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(true))
          .returns(Tuple::getJson, Row::getJson, Json.create(true))
          .forRow(row);
        ColumnChecker.checkColumn(3, "FalseValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(false))
          .returns(Tuple::getJson, Row::getJson, Json.create(false))
          .forRow(row);
        ColumnChecker.checkColumn(4, "NullValue")
          .returns(Tuple::getValue, Row::getValue, Json.create(null))
          .forRow(row);
        ColumnChecker.checkColumn(5, "Number1")
          .returns(Tuple::getValue, Row::getValue, Json.create(7.502d))
          .returns(Tuple::getJson, Row::getJson, Json.create(7.502d))
          .forRow(row);
        ColumnChecker.checkColumn(6, "Number2")
          .returns(Tuple::getValue, Row::getValue, Json.create(8))
          .returns(Tuple::getJson, Row::getJson, Json.create(8))
          .forRow(row);
        ColumnChecker.checkColumn(7, "Text")
          .returns(Tuple::getValue, Row::getValue, Json.create(" Really Awesome! "))
          .returns(Tuple::getJson, Row::getJson, Json.create(" Really Awesome! "))
          .forRow(row);
        async.complete();
      }));
    }));
  }

  private Object[] expected = {Json.create(new JsonObject("{\"str\":\"blah\",\"int\":1,\"float\":3.5,\"object\":{},\"array\":[]}")),
    Json.create(new JsonArray("[1,true,null,9.5,\"Hi\"]")),
    Json.create(4),
    Json.create("Hello World"),
    Json.create(true),
    Json.create(false),
    Json.create(null)};

  @Test
  public void testDecodeJSONArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }' :: JSON, '[1,true,null,9.5,\"Hi\"]' :: JSON, '4' :: JSON, '\"Hello World\"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON]",
      "JSON", Tuple::getJsonArray, Row::getJsonArray, expected);
  }

  @Test
  public void testDecodeJSONBArray(TestContext ctx) {
    testDecodeGenericArray(ctx, "ARRAY ['  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }' :: JSON, '[1,true,null,9.5,\"Hi\"]' :: JSON, '4' :: JSON, '\"Hello World\"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON]",
      "JSONB", Tuple::getJsonArray, Row::getJsonArray, expected);
  }
}
