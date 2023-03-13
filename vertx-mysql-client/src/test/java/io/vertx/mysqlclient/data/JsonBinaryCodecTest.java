/*
 * Copyright (c) 2011-2019 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mysqlclient.data;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import io.vertx.sqlclient.Tuple;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class JsonBinaryCodecTest extends JsonDataTypeTest {
  private static final String CREATE_TABLE = "CREATE TEMPORARY TABLE test_json\n" +
    "(\n" +
    "    json JSON\n" +
    ");";
  private static final String INSERT_JSON_WITHOUT_CAST = "INSERT INTO test_json VALUES (?);";
  private static final String INSERT_JSON_WITH_CAST = "INSERT INTO test_json VALUES (CAST(? AS JSON));";
  private static final String QUERY_JSON = "SELECT json FROM test_json";

  @Test
  public void testEncodeNumber(TestContext ctx) {
    testEncodeJsonWithCast(ctx, Tuple.of(12345), 12345, row -> {
      ctx.assertEquals(12345, row.getInteger(0));
      ctx.assertEquals(12345, row.getInteger("json"));
    });
  }

  @Test
  public void testEncodeString(TestContext ctx) {
    testEncodeJsonWithCast(ctx, Tuple.of("\"hello, world\""), "hello, world", row -> {
      ctx.assertEquals("hello, world", row.getString(0));
      ctx.assertEquals("hello, world", row.getString("json"));
    });
  }

  @Test
  public void testEncodeJsonLiteralNull(TestContext ctx) {
    testEncodeJson(ctx, Tuple.of(Tuple.JSON_NULL), Tuple.JSON_NULL, null, INSERT_JSON_WITHOUT_CAST);
  }

  @Test
  public void testEncodeSqlNull(TestContext ctx) {
    testEncodeJsonWithCast(ctx, Tuple.of(null), null, null);
  }

  @Test
  public void testEncodeBoolean(TestContext ctx) {
    testEncodeJsonWithCast(ctx, Tuple.of(true), 1, row -> {
      ctx.assertEquals(true, row.getBoolean(0));
      ctx.assertEquals(true, row.getBoolean("json"));
    });
  }

  @Test
  public void testEncodeFullJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("test_string", "hello")
      .put("test_number", 12345)
      .put("test_boolean", true)
      .put("test_null", (Object) null)
      .put("test_json_object", new JsonObject().put("key", "value"))
      .put("test_json_array", new JsonArray().add(1).add(2).add(3));
    testEncodeJson(ctx, Tuple.of(expected), expected, row -> ctx.assertEquals(expected, row.get(JsonObject.class, 0)),
      INSERT_JSON_WITHOUT_CAST);
  }

  @Test
  public void testEncodeJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("test_string", "hello")
      .put("test_number", 12345)
      .put("test_boolean", true)
      .put("test_null", (Object) null)
      .put("test_json_object", new JsonObject().put("key", "value"))
      .put("test_json_array", new JsonArray().add(1).add(2).add(3));
    testEncodeJson(ctx, Tuple.of("hello", 12345, null), expected, row -> ctx.assertEquals(expected, row.get(JsonObject.class, 0)),
      "INSERT INTO test_json VALUES (JSON_OBJECT(\n" +
        "               'test_string', ?,\n" +
        "               'test_number', ?,\n" +
        "               'test_boolean', true,\n" + // MySQL does not know it's a boolean
        "               'test_null', ?,\n" +
        "               'test_json_object', JSON_OBJECT('key', 'value'),\n" +
        "               'test_json_array', JSON_ARRAY(1, 2, 3)\n" +
        "           ))");
  }

  @Test
  public void testEncodeFullJsonArray(TestContext ctx) {
    JsonArray expected = new JsonArray()
      .add("hello")
      .add(12345)
      .add(true)
      .add((Object) null)
      .add(new JsonObject().put("key", "value"))
      .add(new JsonArray().add(1).add(2).add(3));
    testEncodeJson(ctx, Tuple.of(expected), expected, row -> ctx.assertEquals(expected, row.get(JsonArray.class, 0)),
      INSERT_JSON_WITHOUT_CAST);
  }

  @Test
  public void testEncodeJsonArray(TestContext ctx) {
    JsonArray expected = new JsonArray()
      .add("hello")
      .add(12345)
      .add(true)
      .add((Object) null)
      .add(new JsonObject().put("key", "value"))
      .add(new JsonArray().add(1).add(2).add(3));
    testEncodeJson(ctx, Tuple.of("hello", 12345, null), expected, row -> ctx.assertEquals(expected, row.get(JsonArray.class, 0)),
      "INSERT INTO test_json VALUES (JSON_ARRAY(\n" +
        "               ?, ?, true, ?, JSON_OBJECT('key', 'value'), JSON_ARRAY(1, 2, 3)\n" +
        "           ))");
  }

  @Test
  public void testDecodeJsonUsingTable(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .query("CREATE TEMPORARY TABLE json_test(test_json JSON);")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(c -> {
        conn
          .query("INSERT INTO json_test VALUE ('{\"phrase\": \"à tout à l''heure\"}');\n" +
          "INSERT INTO json_test VALUE ('{\"emoji\": \"\uD83D\uDE00\uD83E\uDD23\uD83D\uDE0A\uD83D\uDE07\uD83D\uDE33\uD83D\uDE31\"}');")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(i -> {
          conn
            .preparedQuery("SELECT test_json FROM json_test")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res -> {
            ctx.assertEquals(2, res.size());
            RowIterator<Row> iterator = res.iterator();
            Row row1 = iterator.next();
            JsonObject phrase = new JsonObject()
              .put("phrase", "à tout à l'heure");
            ctx.assertEquals(phrase, row1.getJsonObject(0));
            ctx.assertEquals(phrase, row1.getValue(0));
            ctx.assertEquals(phrase, row1.getJson(0));
            Row row2 = iterator.next();
            JsonObject emoji = new JsonObject()
              .put("emoji", "\uD83D\uDE00\uD83E\uDD23\uD83D\uDE0A\uD83D\uDE07\uD83D\uDE33\uD83D\uDE31");
            ctx.assertEquals(emoji, row2.getJsonObject(0));
            ctx.assertEquals(emoji, row2.getValue(0));
            ctx.assertEquals(emoji, row2.getJson(0));
          }));
        }));
      }));
    }));
  }

  @Override
  protected void testDecodeJson(TestContext ctx, String script, Object expected, Consumer<Row> checker) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(script)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue("json"));
        ctx.assertEquals(expected, row.getJson(0));
        ctx.assertEquals(expected, row.getJson("json"));
        if (checker != null) {
          checker.accept(row);
        }
        conn.close();
      }));
    }));
  }

  private void testEncodeJsonWithCast(TestContext ctx, Tuple params, Object expected, Consumer<Row> checker) {
    testEncodeJson(ctx, params, expected, checker, INSERT_JSON_WITH_CAST);
  }

  private void testEncodeJson(TestContext ctx, Tuple params, Object expected, Consumer<Row> checker, String insertScript) {
    MySQLConnection.connect(vertx, options).onComplete( ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(CREATE_TABLE)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(createTable -> {
        conn
          .preparedQuery(insertScript)
          .execute(params)
          .onComplete(ctx.asyncAssertSuccess(insert -> {
          conn
            .preparedQuery(QUERY_JSON)
            .execute()
            .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(expected, row.getValue(0));
            ctx.assertEquals(expected, row.getValue("json"));
            ctx.assertEquals(expected, row.getJson(0));
            ctx.assertEquals(expected, row.getJson("json"));
            if (checker != null) {
              checker.accept(row);
            }
            conn.close();
          }));
        }));
      }));
    }));
  }
}
