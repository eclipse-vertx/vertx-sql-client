/*
 * Copyright (C) 2017 Julien Viet
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package io.vertx.sqlclient.template;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.RowSet;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.Assert.assertEquals;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DataObjectParamsTest extends TemplateTestBase {

  private final LocalTime localTime = LocalTime.parse("19:35:58.237666");
  private final OffsetTime offsetTime = OffsetTime.of(localTime, ZoneOffset.UTC);
  private final LocalDate localDate = LocalDate.parse("2017-05-14");
  private final LocalDateTime localDateTime = LocalDateTime.of(localDate, localTime);
  private final OffsetDateTime offsetDateTime = OffsetDateTime.of(localDateTime, ZoneOffset.UTC);
  private final UUID uuid = UUID.randomUUID();
  private final String string = "some-text";
  private final Buffer buffer = Buffer.buffer(string);
  private final JsonObject jsonObject = new JsonObject().put("string", "str-value").put("number", 1234);
  private final JsonArray jsonArray = new JsonArray().add(1).add(2).add(3);
  protected Vertx vertx;
  protected PgConnection connection;

  @Before
  public void setup(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    Async async = ctx.async();
    PgConnection.connect(vertx, connectOptions(), ctx.asyncAssertSuccess(conn -> {
      connection = conn;
      async.complete();
    }));
    async.await(10000);
  }

  @After
  public void teardown(TestContext ctx) {
    if (connection != null) {
      connection.close();
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testString(TestContext ctx) {
    TestDataObject obj = new TestDataObject();
    obj.setString("the_string");;
    testGet(ctx, "VARCHAR", "the_string", obj, row -> {
      assertEquals("the_string", row.getValue("value"));
    });
  }

  private void testGet(TestContext ctx, String sqlType, String paramName, TestDataObject obj, Consumer<Row> checker) {
    Async async = ctx.async();
    SqlTemplate<Map<String, Object>, RowSet<Row>> template = SqlTemplate.forQuery(connection, "SELECT :" + paramName + " :: " + sqlType + " \"value\"");
    template.execute(TestDataObjectParamMapper.INSTANCE.apply(obj), ctx.asyncAssertSuccess(result -> {
      ctx.assertEquals(1, result.size());
      Row row = result.iterator().next();
      try {
        checker.accept(row);
      } catch (Throwable t) {
        ctx.fail(t);
        return;
      }
      async.complete();
    }));
    async.await(10000);
  }
}
