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

import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowIterator;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class JsonTextCodecTest extends JsonDataTypeTest {

  @Test
  public void testDecodeJsonUsingTable(TestContext ctx) {
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("CREATE TEMPORARY TABLE json_test(test_json JSON);").execute(ctx.asyncAssertSuccess(c -> {
        conn.query("INSERT INTO json_test VALUE ('{\"phrase\": \"à tout à l''heure\"}');\n" +
          "INSERT INTO json_test VALUE ('{\"emoji\": \"\uD83D\uDE00\uD83E\uDD23\uD83D\uDE0A\uD83D\uDE07\uD83D\uDE33\uD83D\uDE31\"}');").execute(ctx.asyncAssertSuccess(i -> {
          conn.query("SELECT test_json FROM json_test").execute(ctx.asyncAssertSuccess(res -> {
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
    MySQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query(script).execute(ctx.asyncAssertSuccess(result -> {
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
}
