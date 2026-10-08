/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */
package tests.oracleclient;

import io.vertx.core.json.Json;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.*;
import org.junit.runner.RunWith;
import tests.oracleclient.junit.OracleRule;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@RunWith(VertxUnitRunner.class)
public class OracleJsonDataTypeTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp(TestContext ctx) {
    pool = OracleBuilder.pool(builder -> builder.connectingTo(oracle.options()).using(vertx));
    pool.query("BEGIN EXECUTE IMMEDIATE 'DROP TABLE json_test'; EXCEPTION WHEN OTHERS THEN NULL; END;")
      .execute()
      .compose(v -> pool.query("CREATE TABLE json_test (id NUMBER PRIMARY KEY, data JSON)").execute())
      .onComplete(ctx.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext ctx) {
    pool.query("DROP TABLE json_test")
      .execute()
      .compose(v -> pool.close())
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testDecodeJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("name", "Alice")
      .put("age", 30);
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(1, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(1)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(expected, row.getJson(0));
        ctx.assertEquals(expected, row.getJsonObject(0));
      }));
  }

  @Test
  public void testDecodeJsonArray(TestContext ctx) {
    JsonArray expected = new JsonArray().add(1).add("two").add(true);
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(2, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(2)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(expected, row.getJson(0));
        ctx.assertEquals(expected, row.getJsonArray(0));
      }));
  }

  @Test
  public void testDecodeJsonLiteralNull(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(3, Tuple.JSON_NULL))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(3)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(Tuple.JSON_NULL, row.getJson(0));
      }));
  }

  @Test
  public void testDecodeSqlNull(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(4, (Object) null))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(4)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertNull(row.getJson(0));
        ctx.assertNull(row.getJsonObject(0));
        ctx.assertNull(row.getJsonArray(0));
      }));
  }

  @Test
  public void testDecodeUnicode(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("emoji", "😀")
      .put("japanese", "フレームワーク")
      .put("arabic", "مرحبا");
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(6, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(6)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(expected, row.getJsonObject(0));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testDecodeJsonString(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(7, "hello"))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(7)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals("hello", rows.iterator().next().getJson(0));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testDecodeJsonNumber(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(8, 42))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(8)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals(new BigDecimal("42"), rows.iterator().next().getJson(0));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testDecodeJsonBooleanTrue(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(9, true))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(9)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals(Boolean.TRUE, rows.iterator().next().getJson(0));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testDecodeJsonBooleanFalse(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(10, false))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(10)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals(Boolean.FALSE, rows.iterator().next().getJson(0));
      }));
  }

  @Test
  public void testDecodeNestedJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("address", new JsonObject()
        .put("city", "New York")
        .put("zip", "10001"))
      .put("tags", new JsonArray().add("admin").add("user"));
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(5, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(5)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(expected, row.getJsonObject(0));
      }));
  }

  @Test
  public void testDecodeJsonObjectUsingCursor(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("name", "Alice")
      .put("age", 30);
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(11, expected))
      .compose(v -> pool.getConnection())
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.prepare("SELECT data FROM json_test WHERE id = 11")
          .onComplete(ctx.asyncAssertSuccess(ps -> {
            ps.cursor().read(10).onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.assertEquals(1, rows.size());
              Row row = rows.iterator().next();
              ctx.assertEquals(expected, row.getJsonObject(0));
              conn.close();
            }));
          }));
      }));
  }

  @Test
  public void testDecodeJsonArrayUsingCursor(TestContext ctx) {
    JsonArray expected = new JsonArray().add(1).add("two").add(true);
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(12, expected))
      .compose(v -> pool.getConnection())
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.prepare("SELECT data FROM json_test WHERE id = 12")
          .onComplete(ctx.asyncAssertSuccess(ps -> {
            ps.cursor().read(10).onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.assertEquals(1, rows.size());
              Row row = rows.iterator().next();
              ctx.assertEquals(expected, row.getJsonArray(0));
              conn.close();
            }));
          }));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testBatchInsertJsonScalars(TestContext ctx) {
    List<Tuple> batch = new ArrayList<>();
    batch.add(Tuple.of(20, "hello"));
    batch.add(Tuple.of(21, 42));
    batch.add(Tuple.of(22, true));
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .executeBatch(batch)
      .compose(v -> pool.preparedQuery("SELECT id, data FROM json_test WHERE id >= 20 ORDER BY id").execute())
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(3, rows.size());
        List<Object> values = new ArrayList<>();
        for (Row row : rows) {
          values.add(row.getJson(1));
        }
        ctx.assertEquals("hello", values.get(0));
        ctx.assertEquals(new BigDecimal("42"), values.get(1));
        ctx.assertEquals(Boolean.TRUE, values.get(2));
      }));
  }

  @Ignore("Disabled until Oracle JDBC driver bug 40076713 is fixed")
  @Test
  public void testCursorInsertJsonScalar(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(30, "cursor-test"))
      .compose(v -> pool.getConnection())
      .onComplete(ctx.asyncAssertSuccess(conn -> {
        conn.prepare("SELECT data FROM json_test WHERE id = ?")
          .onComplete(ctx.asyncAssertSuccess(ps -> {
            ps.cursor(Tuple.of(30)).read(10).onComplete(ctx.asyncAssertSuccess(rows -> {
              ctx.assertEquals(1, rows.size());
              ctx.assertEquals("cursor-test", rows.iterator().next().getJson(0));
              conn.close();
            }));
          }));
      }));
  }

  @Test
  public void testInsertJsonStringWithSqlWorkaround(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, JSON(?))")
      .execute(Tuple.of(40, Json.encode("hello")))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(40)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals("hello", rows.iterator().next().getJson(0));
      }));
  }

  @Test
  public void testInsertJsonNumberWithSqlWorkaround(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, JSON(?))")
      .execute(Tuple.of(41, Json.encode(42)))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(41)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals(new BigDecimal("42"), rows.iterator().next().getJson(0));
      }));
  }

  @Test
  public void testInsertJsonBooleanWithSqlWorkaround(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, JSON(?))")
      .execute(Tuple.of(42, Json.encode(true)))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(42)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        ctx.assertEquals(Boolean.TRUE, rows.iterator().next().getJson(0));
      }));
  }

  @Test
  public void testDecodeNestedJsonNull(TestContext ctx) {
    JsonObject expected = new JsonObject().put("key", (String) null);
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(50, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(50)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject result = row.getJsonObject(0);
        ctx.assertTrue(result.containsKey("key"));
        ctx.assertNull(result.getValue("key"));
      }));
  }

  @Test
  public void testDecodeTopLevelJsonNull(TestContext ctx) {
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(51, Tuple.JSON_NULL))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(51)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        ctx.assertEquals(Tuple.JSON_NULL, row.getJson(0));
      }));
  }

  @Test
  public void testDecodeOsonTimestamp(TestContext ctx) {
    pool.query("INSERT INTO json_test (id, data) VALUES (52, JSON_OBJECT('ts' VALUE TIMESTAMP '2023-09-21 10:00:00' RETURNING JSON))")
      .execute()
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(52)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject obj = row.getJsonObject(0);
        ctx.assertTrue(obj.getValue("ts") instanceof String);
        ctx.assertEquals("2023-09-21T10:00", obj.getString("ts"));
      }));
  }

  @Test
  public void testDecodeOsonTimestampTZ(TestContext ctx) {
    pool.query("INSERT INTO json_test (id, data) VALUES (53, JSON_OBJECT('ts' VALUE TIMESTAMP '2023-09-21 10:00:00 +02:00' RETURNING JSON))")
      .execute()
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(53)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject obj = row.getJsonObject(0);
        ctx.assertTrue(obj.getValue("ts") instanceof String);
        java.time.OffsetDateTime expected = java.time.OffsetDateTime.parse("2023-09-21T10:00:00+02:00");
        java.time.OffsetDateTime actual = java.time.OffsetDateTime.parse(obj.getString("ts"));
        ctx.assertEquals(expected, actual);
      }));
  }

  @Test
  public void testDecodeOsonDate(TestContext ctx) {
    pool.query("INSERT INTO json_test (id, data) VALUES (54, JSON_OBJECT('d' VALUE DATE '2023-09-21' RETURNING JSON))")
      .execute()
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(54)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject obj = row.getJsonObject(0);
        ctx.assertTrue(obj.getValue("d") instanceof String);
        java.time.LocalDateTime expected = java.time.LocalDateTime.parse("2023-09-21T00:00");
        java.time.LocalDateTime actual = java.time.LocalDateTime.parse(obj.getString("d"));
        ctx.assertEquals(expected, actual);
      }));
  }

  @Test
  public void testDecodeOsonIntervalYearToMonth(TestContext ctx) {
    pool.query("INSERT INTO json_test (id, data) VALUES (55, JSON_OBJECT('iv' VALUE TO_YMINTERVAL('P2Y3M') RETURNING JSON))")
      .execute()
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(55)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject obj = row.getJsonObject(0);
        ctx.assertTrue(obj.getValue("iv") instanceof String);
        java.time.Period expected = java.time.Period.parse("P2Y3M");
        java.time.Period actual = java.time.Period.parse(obj.getString("iv"));
        ctx.assertEquals(expected, actual);
      }));
  }

  @Test
  public void testDecodeOsonIntervalDayToSecond(TestContext ctx) {
    pool.query("INSERT INTO json_test (id, data) VALUES (56, JSON_OBJECT('iv' VALUE TO_DSINTERVAL('P1DT2H3M4.5S') RETURNING JSON))")
      .execute()
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(56)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject obj = row.getJsonObject(0);
        ctx.assertTrue(obj.getValue("iv") instanceof String);
        Duration expected = Duration.parse("P1DT2H3M4.5S");
        Duration actual = Duration.parse(obj.getString("iv"));
        ctx.assertEquals(expected, actual);
      }));
  }

  @Test
  public void testJsonObjectRoundTrip(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("str", "hello")
      .put("num", 42)
      .put("dbl", 3.14)
      .put("bool", true)
      .putNull("nil")
      .put("nested", new JsonObject().put("key", "value"))
      .put("arr", new JsonArray().add(1).add("two").add(false));
    pool.preparedQuery("INSERT INTO json_test (id, data) VALUES (?, ?)")
      .execute(Tuple.of(57, expected))
      .compose(v -> pool.preparedQuery("SELECT data FROM json_test WHERE id = ?").execute(Tuple.of(57)))
      .onComplete(ctx.asyncAssertSuccess(rows -> {
        ctx.assertEquals(1, rows.size());
        Row row = rows.iterator().next();
        JsonObject actual = row.getJsonObject(0);
        ctx.assertEquals(expected.getString("str"), actual.getString("str"));
        ctx.assertEquals(expected.getBoolean("bool"), actual.getBoolean("bool"));
        ctx.assertNull(actual.getValue("nil"));
        ctx.assertEquals(expected.getJsonObject("nested"), actual.getJsonObject("nested"));
        ctx.assertEquals(expected.getJsonArray("arr").getString(1), actual.getJsonArray("arr").getString(1));
        // Verify encode() works without exception
        String encoded = actual.encode();
        ctx.assertNotNull(encoded);
      }));
  }
}
