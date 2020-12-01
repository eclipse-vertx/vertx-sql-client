package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Assert;
import org.junit.Test;

import java.math.BigDecimal;

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
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT " +
          "'  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }'::" + type + " \"JsonObject\"," +
          "'  [1,true,null,9.5,\"Hi\" ] '::" + type + " \"JsonArray\"," +
          "' true '::" + type + " \"TrueValue\"," +
          "' false '::" + type + " \"FalseValue\"," +
          "' null '::" + type + " \"NullValue\"," +
          "' 7.502 '::" + type + " \"Number1\"," +
          "' 8 '::" + type + " \"Number2\"," +
          "'\" Really Awesome! \"'::" + type + " \"Text\"," +
          "NULL::" + type + " \"Null\"").execute(
        ctx.asyncAssertSuccess(result -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
          JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ColumnChecker.checkColumn(0, "JsonObject")
            .returns(Tuple::getValue, Row::getValue, object)
            .returns(Tuple::getJsonObject, Row::getJsonObject, object)
            .returns(Tuple::getJson, Row::getJson, object)
            .forRow(row);
          ColumnChecker.checkColumn(1, "JsonArray")
            .returns(Tuple::getValue, Row::getValue, array)
            .returns(Tuple::getJsonArray, Row::getJsonArray, array)
            .returns(Tuple::getJson, Row::getJson, array)
            .forRow(row);
          ColumnChecker.checkColumn(2, "TrueValue")
            .returns(Tuple::getValue, Row::getValue, true)
            .returns(Tuple::getBoolean, Row::getBoolean, true)
            .returns(Tuple::getJson, Row::getJson, true)
            .returns(Object.class, true)
            .forRow(row);
          ColumnChecker.checkColumn(3, "FalseValue")
            .returns(Tuple::getValue, Row::getValue, false)
            .returns(Tuple::getBoolean, Row::getBoolean, false)
            .returns(Tuple::getJson, Row::getJson, false)
            .returns(Object.class, false)
            .forRow(row);
          ColumnChecker.checkColumn(4, "NullValue")
            .returns(Tuple::getValue, Row::getValue, Tuple.JSON_NULL)
            .returns(Tuple::getJson, Row::getJson, Tuple.JSON_NULL)
            .forRow(row);
          ColumnChecker.checkColumn(5, "Number1")
            .returns(Tuple::getValue, Row::getValue, 7.502d)
            .returns(Tuple::getJson, Row::getJson, 7.502d)
            .returns(Tuple::getShort, Row::getShort, (short) 7)
            .returns(Tuple::getInteger, Row::getInteger, 7)
            .returns(Tuple::getLong, Row::getLong, (long) 7)
            .returns(Tuple::getFloat, Row::getFloat, 7.502f)
            .returns(Tuple::getDouble, Row::getDouble, 7.502d)
            .<BigDecimal>returns(Tuple::getBigDecimal, Row::getBigDecimal, val -> Assert.assertEquals(val.doubleValue(), 7.502d, 0.1))
            .returns(Object.class, 7.502d)
            .forRow(row);
          ColumnChecker.checkColumn(6, "Number2")
            .returns(Tuple::getValue, Row::getValue, 8)
            .returns(Tuple::getJson, Row::getJson, 8)
            .returns(Tuple::getShort, Row::getShort, (short) 8)
            .returns(Tuple::getInteger, Row::getInteger, 8)
            .returns(Tuple::getLong, Row::getLong, (long) 8)
            .returns(Tuple::getFloat, Row::getFloat, (float) 8)
            .returns(Tuple::getDouble, Row::getDouble, (double) 8)
            .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(8))
            .returns(Object.class, 8)
            .forRow(row);
          ColumnChecker.checkColumn(7, "Text")
            .returns(Tuple::getValue, Row::getValue, " Really Awesome! ")
            .returns(Tuple::getJson, Row::getJson, " Really Awesome! ")
            .returns(Tuple::getString, Row::getString, " Really Awesome! ")
            .returns(Object.class, " Really Awesome! ")
            .forRow(row);
          ColumnChecker.checkColumn(8, "Null")
            .returnsNull()
            .forRow(row);
          async.complete();
        }));
    }));
  }

  private Object[] expected = {
    new JsonObject("{\"str\":\"blah\",\"int\":1,\"float\":3.5,\"object\":{},\"array\":[]}"),
    new JsonArray("[1,true,null,9.5,\"Hi\"]"),
    4,
    "Hello World",
    true,
    false,
    Tuple.JSON_NULL};

  @Test
  public void testDecodeJSONArray(TestContext ctx) {
    ColumnChecker checker = ColumnChecker.checkColumn(0, "JSON")
      .returns(Tuple::getValue, Row::getValue, expected)
      .skip(Tuple::getArrayOfBooleans, Row::getArrayOfBooleans)
      .skip(Tuple::getArrayOfShorts, Row::getArrayOfShorts)
      .skip(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers)
      .skip(Tuple::getArrayOfLongs, Row::getArrayOfLongs)
      .skip(Tuple::getArrayOfFloats, Row::getArrayOfFloats)
      .skip(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles)
      .skip(Tuple::getArrayOfStrings, Row::getArrayOfStrings)
      .skip(Tuple::getArrayOfJsonObjects, Row::getArrayOfJsonObjects)
      .skip(Tuple::getArrayOfJsonArrays, Row::getArrayOfJsonArrays)
      .skip(Tuple::getArrayOfJsons, Row::getArrayOfJsons);
    testDecodeGenericArray(ctx, "ARRAY ['  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }' :: JSON, '[1,true,null,9.5,\"Hi\"]' :: JSON, '4' :: JSON, '\"Hello World\"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON]",
      "JSON", checker);
  }

  @Test
  public void testDecodeJSONBArray(TestContext ctx) {
    ColumnChecker checker = ColumnChecker.checkColumn(0, "JSONB")
      .returns(Tuple::getValue, Row::getValue, expected)
      .skip(Tuple::getArrayOfBooleans, Row::getArrayOfBooleans)
      .skip(Tuple::getArrayOfShorts, Row::getArrayOfShorts)
      .skip(Tuple::getArrayOfIntegers, Row::getArrayOfIntegers)
      .skip(Tuple::getArrayOfLongs, Row::getArrayOfLongs)
      .skip(Tuple::getArrayOfFloats, Row::getArrayOfFloats)
      .skip(Tuple::getArrayOfDoubles, Row::getArrayOfDoubles)
      .skip(Tuple::getArrayOfStrings, Row::getArrayOfStrings)
      .skip(Tuple::getArrayOfJsonObjects, Row::getArrayOfJsonObjects)
      .skip(Tuple::getArrayOfJsonArrays, Row::getArrayOfJsonArrays)
      .skip(Tuple::getArrayOfJsons, Row::getArrayOfJsons);
    testDecodeGenericArray(ctx, "ARRAY ['  {\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []   }' :: JSON, '[1,true,null,9.5,\"Hi\"]' :: JSON, '4' :: JSON, '\"Hello World\"' :: JSON, 'true' :: JSON, 'false' :: JSON, 'null' :: JSON]",
      "JSONB", checker);
  }
}
