package io.vertx.pgclient.data;

import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.ColumnChecker;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.Test;

import java.math.BigDecimal;

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
      new Object[]{
        10,
        true,
        "hello",
        new JsonObject().put("foo", "bar"),
        new JsonArray().add(0).add(1).add(2)
      }, Object.class);
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
    testGenericArray(ctx,
      "SELECT c FROM (VALUES ($1 :: " + jsonType + "[])) AS t (c)",
      new Object[][]{
        new Object[]{10,
          true,
          "hello",
          new JsonObject().put("foo", "bar"),
          new JsonArray().add(0).add(1).add(2)}
      }, Object.class);
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
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("SELECT \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"NullValue\", \"Null\" FROM \"" + tableName + "\" WHERE \"id\" = $1",
        ctx.asyncAssertSuccess(p -> {
          p.query().execute(Tuple.tuple().addInteger(1), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
            JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, object)
              .returns(Tuple::getJsonObject, Row::getJsonObject, object)
              .returns(JsonObject.class, object)
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, array)
              .returns(Tuple::getJsonArray, Row::getJsonArray, array)
              .returns(JsonArray.class, array)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, 4)
              .returns(Tuple::getShort, Row::getShort, (short) 4)
              .returns(Tuple::getInteger, Row::getInteger, 4)
              .returns(Tuple::getLong, Row::getLong, (long)4)
              .returns(Tuple::getFloat, Row::getFloat, (float)4)
              .returns(Tuple::getDouble, Row::getDouble, (double)4)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(4))
              .returns(Object.class, 4)
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, "Hello World")
              .returns(Tuple::getString, Row::getString, "Hello World")
              .returns(String.class, "Hello World")
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, true)
              .returns(Tuple::getBoolean, Row::getBoolean, true)
              .returns(Boolean.class, true)
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, false)
              .returns(Tuple::getBoolean, Row::getBoolean, false)
              .returns(Boolean.class, false)
              .forRow(row);
            ColumnChecker.checkColumn(6, "NullValue")
              .returns(Tuple::getValue, Row::getValue, Tuple.JSON_NULL)
              .returns(Object.class, Tuple.JSON_NULL)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Null")
              .returnsNull()
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
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.prepare("UPDATE \"" + tableName + "\" SET " +
          "\"JsonObject\" = $1, " +
          "\"JsonArray\" = $2, " +
          "\"Number\" = $3, " +
          "\"String\" = $4, " +
          "\"BooleanTrue\" = $5, " +
          "\"BooleanFalse\" = $6, " +
          "\"NullValue\" = $7, " +
          "\"Null\" = $8 " +
          "WHERE \"id\" = $9 RETURNING \"JsonObject\", \"JsonArray\", \"Number\", \"String\", \"BooleanTrue\", \"BooleanFalse\", \"NullValue\", \"Null\"",
        ctx.asyncAssertSuccess(p -> {
          JsonObject object = new JsonObject("{\"str\":\"blah\", \"int\" : 1, \"float\" : 3.5, \"object\": {}, \"array\" : []}");
          JsonArray array = new JsonArray("[1,true,null,9.5,\"Hi\"]");
          p.query().execute(Tuple.tuple()
            .addValue(object)
            .addValue(array)
            .addValue(4)
            .addValue("Hello World")
            .addValue(true)
            .addValue(false)
            .addValue(Tuple.JSON_NULL)
            .addValue(null)
            .addInteger(2), ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            ctx.assertEquals(1, result.rowCount());
            Row row = result.iterator().next();
            ColumnChecker.checkColumn(0, "JsonObject")
              .returns(Tuple::getValue, Row::getValue, object)
              .returns(Tuple::getJsonObject, Row::getJsonObject, object)
              .returns(JsonObject.class, object)
              .forRow(row);
            ColumnChecker.checkColumn(1, "JsonArray")
              .returns(Tuple::getValue, Row::getValue, array)
              .returns(Tuple::getJsonArray, Row::getJsonArray, array)
              .returns(JsonArray.class, array)
              .forRow(row);
            ColumnChecker.checkColumn(2, "Number")
              .returns(Tuple::getValue, Row::getValue, 4)
              .returns(Tuple::getShort, Row::getShort, (short)4)
              .returns(Tuple::getInteger, Row::getInteger, 4)
              .returns(Tuple::getLong, Row::getLong, (long)4)
              .returns(Tuple::getFloat, Row::getFloat, (float)4)
              .returns(Tuple::getDouble, Row::getDouble, (double)4)
              .returns(Tuple::getBigDecimal, Row::getBigDecimal, new BigDecimal(4))
              .returns(Object.class, 4)
              .forRow(row);
            ColumnChecker.checkColumn(3, "String")
              .returns(Tuple::getValue, Row::getValue, "Hello World")
              .returns(Tuple::getString, Row::getString, "Hello World")
              .returns(String.class, "Hello World")
              .forRow(row);
            ColumnChecker.checkColumn(4, "BooleanTrue")
              .returns(Tuple::getValue, Row::getValue, true)
              .returns(Tuple::getBoolean, Row::getBoolean, true)
              .returns(Boolean.class, true)
              .forRow(row);
            ColumnChecker.checkColumn(5, "BooleanFalse")
              .returns(Tuple::getValue, Row::getValue, false)
              .returns(Tuple::getBoolean, Row::getBoolean, false)
              .returns(Boolean.class, false)
              .forRow(row);
            ColumnChecker.checkColumn(6, "NullValue")
              .returns(Tuple::getValue, Row::getValue, Tuple.JSON_NULL)
              .returns(Object.class, Tuple.JSON_NULL)
              .forRow(row);
            ColumnChecker.checkColumn(7, "Null")
              .returnsNull()
              .forRow(row);
            async.complete();
          }));
        }));
    }));
  }
}
