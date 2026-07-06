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
package io.vertx.tests.mssqlclient.data;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mssqlclient.MSSQLConnectOptions;
import io.vertx.mssqlclient.MSSQLConnection;
import io.vertx.mssqlclient.MSSQLException;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.tests.mssqlclient.MSSQLTestBase;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assume.assumeTrue;

@RunWith(VertxUnitRunner.class)
public class MSSQLJsonDataTypeTest extends MSSQLTestBase {

  private Vertx vertx;
  private MSSQLConnectOptions connectOptions;

  @BeforeClass
  public static void beforeClass() {
    assumeTrue("JSON type requires SQL Server 2025+", rule.supportsJsonType());
  }

  @Before
  public void setUp(TestContext ctx) {
    vertx = Vertx.vertx();
    connectOptions = new MSSQLConnectOptions(MSSQLTestBase.options);
    Async async = ctx.async();
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.query("DROP TABLE IF EXISTS json_test").execute()
        .compose(v -> conn.query("CREATE TABLE json_test (id INT PRIMARY KEY, data json)").execute())
        .onComplete(ctx.asyncAssertSuccess(v -> {
          conn.close();
          async.complete();
        }));
    }));
  }

  @After
  public void tearDown(TestContext ctx) {
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.query("DROP TABLE IF EXISTS json_test").execute()
        .onComplete(ctx.asyncAssertSuccess(v -> {
          conn.close();
          vertx.close().onComplete(ctx.asyncAssertSuccess());
        }));
    }));
  }

  @Test
  public void testDecodeJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("name", "Alice")
      .put("age", 30);
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(1, expected))
        .compose(v -> conn.preparedQuery("SELECT data FROM json_test WHERE id = @p1").execute(Tuple.of(1)))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertEquals(expected, row.getJson(0));
          ctx.assertEquals(expected, row.getJsonObject(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testDecodeJsonArray(TestContext ctx) {
    JsonArray expected = new JsonArray().add(1).add(2).add(3);
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(2, expected))
        .compose(v -> conn.preparedQuery("SELECT data FROM json_test WHERE id = @p1").execute(Tuple.of(2)))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertEquals(expected, row.getJson(0));
          ctx.assertEquals(expected, row.getJsonArray(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testDecodeSqlNull(TestContext ctx) {
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(3, (Object) null))
        .compose(v -> conn.preparedQuery("SELECT data FROM json_test WHERE id = @p1").execute(Tuple.of(3)))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertNull(row.getJson(0));
          ctx.assertNull(row.getJsonObject(0));
          ctx.assertNull(row.getJsonArray(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testDecodeNestedJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("address", new JsonObject()
        .put("city", "New York")
        .put("zip", "10001"))
      .put("tags", new JsonArray().add("admin").add("user"));
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(4, expected))
        .compose(v -> conn.preparedQuery("SELECT data FROM json_test WHERE id = @p1").execute(Tuple.of(4)))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertEquals(expected, row.getJsonObject(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testScalarRejection(TestContext ctx) {
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(5, 42))
        .onComplete(ctx.asyncAssertFailure(err -> {
          conn.close();
          if (err instanceof MSSQLException) {
            MSSQLException exception = (MSSQLException) err;
            ctx.assertEquals("S0002", exception.getSqlState());
            ctx.assertEquals(206, exception.getErrorCode());
          } else {
            ctx.fail(err);
          }
        }));
    }));
  }

  @Test
  public void testDecodeUnicode(TestContext ctx) {
    JsonObject expected = new JsonObject()
      .put("emoji", "😀")
      .put("japanese", "フレームワーク")
      .put("arabic", "مرحبا");
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(7, expected))
        .compose(v -> conn.preparedQuery("SELECT data FROM json_test WHERE id = @p1").execute(Tuple.of(7)))
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertEquals(expected, row.getJsonObject(0));
          conn.close();
        }));
    }));
  }

  @Test
  public void testSimpleQueryDecodeJsonObject(TestContext ctx) {
    JsonObject expected = new JsonObject().put("key", "value");
    MSSQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.preparedQuery("INSERT INTO json_test (id, data) VALUES (@p1, @p2)")
        .execute(Tuple.of(6, expected))
        .compose(v -> conn.query("SELECT data FROM json_test WHERE id = 6").execute())
        .onComplete(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(1, rows.size());
          Row row = rows.iterator().next();
          ctx.assertEquals(expected, row.getJsonObject(0));
          conn.close();
        }));
    }));
  }
}
