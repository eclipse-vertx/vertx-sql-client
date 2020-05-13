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

package io.vertx.sqlclient.templates;

import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class DataObjectParamsTest extends PgTemplateTestBase {

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

  @Test
  public void testString(TestContext ctx) {
    TestDataObject obj = new TestDataObject();
    obj.setString("the_string");;
    testGet(ctx, "VARCHAR", "the_string", obj, "the_string");
  }

  @Test
  public void testEnum(TestContext ctx) {
    TestDataObject obj = new TestDataObject();
    obj.setTimeUnit(TimeUnit.MICROSECONDS);;
    testGet(ctx, "VARCHAR", "timeUnit", obj, "MICROSECONDS");
  }

  private void testGet(TestContext ctx, String sqlType, String paramName, TestDataObject obj, Object expected) {
    super.<TestDataObject, Row, Object>testGet(
      ctx,
      sqlType,
      Function.identity(),
      TestDataObjectParametersMapper.INSTANCE,
      paramName,
      obj,
      expected,
      row -> row.getValue("value"),
      "value");
  }
}
