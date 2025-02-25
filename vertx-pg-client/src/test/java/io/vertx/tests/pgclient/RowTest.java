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
package io.vertx.tests.pgclient;

import io.vertx.core.Vertx;
import io.vertx.core.buffer.Buffer;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.data.*;
import io.vertx.sqlclient.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Array;
import java.time.*;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class RowTest extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() throws Exception {
    super.setup();
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  private static <T> Function<String, T> accessor(Row row, Class<T> type) {
    return name -> {
      int idx = row.getColumnIndex(name);
      if (idx == -1) {
        throw new NoSuchElementException();
      }
      return row.get(type, idx);
    };
  }

  private static <T> Function<String, T[]> arrayAccessor(Row row, Class<T> type) {
    return name -> {
      int idx = row.getColumnIndex(name);
      if (idx == -1) {
        throw new NoSuchElementException();
      }
      return (T[]) row.get(Array.newInstance(type, 0).getClass(), idx);
    };
  }

  @Test
  public void testGetNonExistingRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 1 \"foo\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          List<Function<String, ?>> functions = Arrays.asList(
            row::getValue,
            row::getShort,
            row::getInteger,
            row::getLong,
            row::getFloat,
            row::getDouble,
            row::getBigDecimal,
            row::getNumeric,
            row::getString,
            row::getBuffer,
            row::getLocalDate,
            row::getLocalTime,
            row::getOffsetDateTime,
            row::getLocalDateTime,
            row::getOffsetTime,
            row::getTemporal,
            row::getUUID,
            accessor(row, Point.class),
            accessor(row, Line.class),
            accessor(row, LineSegment.class),
            accessor(row, Box.class),
            accessor(row, Path.class),
            accessor(row, Polygon.class),
            accessor(row, Circle.class),
            accessor(row, Interval.class),
            row::getArrayOfShorts,
            row::getArrayOfIntegers,
            row::getArrayOfLongs,
            row::getArrayOfFloats,
            row::getArrayOfDoubles,
            row::getArrayOfBigDecimals,
            row::getArrayOfNumerics,
            row::getArrayOfStrings,
            row::getArrayOfBuffers,
            row::getArrayOfLocalDates,
            row::getArrayOfLocalTimes,
            row::getArrayOfOffsetDateTimes,
            row::getArrayOfLocalDateTimes,
            row::getArrayOfOffsetTimes,
            row::getArrayOfTemporals,
            row::getArrayOfUUIDs,
            arrayAccessor(row, Point.class),
            arrayAccessor(row, Line.class),
            arrayAccessor(row, LineSegment.class),
            arrayAccessor(row, Box.class),
            arrayAccessor(row, Path.class),
            arrayAccessor(row, Polygon.class),
            arrayAccessor(row, Circle.class),
            arrayAccessor(row, Interval.class)
          );
          functions.forEach(f -> {
            try {
              f.apply("bar");
              ctx.fail("Was expecting an NSEE");
            } catch (NoSuchElementException ignore) {
            }
            try {
              f.apply(null);
              ctx.fail("Was expecting an NPE");
            } catch (NullPointerException ignore) {
            }
          });
          async.complete();
        }));
    }));
  }

  @Test
  public void testGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2 \"foo\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertEquals("foo",row.getColumnName(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNotEqualGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2 \"foo\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNotEquals("bar",row.getColumnName(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNegativeGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2 \"foo\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNull(row.getColumnName(-1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testPreventLengthMaxIndexOutOfBoundGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 2 \"foo\"")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNull(row.getColumnName(1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testToJsonObject(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT " +
        "2::smallint \"small_int\"," +
        "2::integer \"integer\"," +
        "2::bigint \"bigint\"," +
        "2::real \"real\"," +
        "2::double precision \"double\"," +
        "'str' \"string\"," +
        "true \"boolean\"," +
        "'null'::json \"json_null\"," +
        "'7'::json \"json_number\"," +
        "'\"baz\"'::json \"json_string\"," +
        "'false'::json \"json_boolean\"," +
        "'{\"bar\": \"baz\", \"balance\": 7, \"active\": false}'::json \"json_object\"," +
        "'[\"baz\",7,false]'::json \"json_array\"," +
        "E'\\\\x010203'::bytea \"buffer\"," +
        "'a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11'::uuid \"uuid\"," +
          "ARRAY[1, 2, 3] \"array\"," +
          "'2020-01-01'::TIMESTAMPTZ \"timestamp\""
      )
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          JsonObject json = row.toJson();
          OffsetDateTime tz = OffsetDateTime.of(LocalDateTime.of(LocalDate.of(2020, 1, 1), LocalTime.MIDNIGHT), ZoneOffset.UTC);
          ctx.assertEquals((short)2, json.getValue("small_int"));
          ctx.assertEquals(2, json.getValue("integer"));
          ctx.assertEquals(2L, json.getValue("bigint"));
          ctx.assertEquals(2F, json.getValue("real"));
          ctx.assertEquals(2D, json.getValue("double"));
          ctx.assertEquals("str", json.getValue("string"));
          ctx.assertEquals(true, json.getValue("boolean"));
          ctx.assertEquals(null, json.getValue("json_null"));
          ctx.assertEquals(7, json.getValue("json_number"));
          ctx.assertEquals("baz", json.getValue("json_string"));
          ctx.assertEquals(false, json.getValue("json_boolean"));
          ctx.assertEquals(new JsonObject().put("bar", "baz").put("balance", 7).put("active", false), json.getValue("json_object"));
          ctx.assertEquals(new JsonArray().add("baz").add(7).add(false), json.getValue("json_array"));
          ctx.assertEquals(Buffer.buffer().appendByte((byte)1).appendByte((byte)2).appendByte((byte)3), json.getMap().get("buffer"));
          ctx.assertEquals(new String(Base64.getEncoder().encode(new byte[]{1,2,3})), json.getValue("buffer"));
          ctx.assertEquals("a0eebc99-9c0b-4ef8-bb6d-6bb9bd380a11", json.getValue("uuid"));
          ctx.assertEquals(new JsonArray().add(1).add(2).add(3), json.getValue("array"));
          ctx.assertEquals(tz, json.getInstant("timestamp").atOffset(ZoneOffset.UTC));
          async.complete();
        }));
    }));
  }
}
