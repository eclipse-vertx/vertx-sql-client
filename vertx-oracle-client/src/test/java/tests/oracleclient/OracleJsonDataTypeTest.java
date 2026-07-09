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

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.math.BigDecimal;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import tests.oracleclient.junit.OracleRule;

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
}
