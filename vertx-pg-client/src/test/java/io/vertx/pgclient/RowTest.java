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
package io.vertx.pgclient;

import io.vertx.pgclient.data.Box;
import io.vertx.pgclient.data.Circle;
import io.vertx.pgclient.data.Interval;
import io.vertx.pgclient.data.Line;
import io.vertx.pgclient.data.LineSegment;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.pgclient.data.Path;
import io.vertx.pgclient.data.Point;
import io.vertx.pgclient.data.Polygon;
import io.vertx.sqlclient.Row;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
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
    vertx.close(ctx.asyncAssertSuccess());
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
      return row.getValues(type, idx);
    };
  }

  @Test
  public void testGetNonExistingRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 1 \"foo\"").execute(
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          List<Function<String, ?>> functions = Arrays.asList(
            row::getValue,

            row::getBoolean,
            row::getDouble,
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
            row::getLocalDateTime,
            row::getOffsetTime,
            row::getOffsetDateTime,
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
            row::getBooleanArray,
            row::getDoubleArray,
            row::getShortArray,
            row::getIntegerArray,
            row::getLongArray,
            row::getFloatArray,
            row::getDoubleArray,
            row::getBigDecimalArray,
            row::getNumericArray,
            row::getStringArray,
            row::getBufferArray,
            row::getLocalDateArray,
            row::getLocalTimeArray,
            row::getLocalDateTimeArray,
            row::getOffsetTimeArray,
            row::getOffsetDateTimeArray,
            row::getTemporalArray,
            row::getUUIDArray,
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
              ctx.fail("Was expecting NSEE");
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
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"").execute(
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertEquals("foo",row.getColumnName(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNotEqualGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"").execute(
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNotEquals("bar",row.getColumnName(0));
          async.complete();
        }));
    }));
  }

  @Test
  public void testNegativeGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"").execute(
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNull(row.getColumnName(-1));
          async.complete();
        }));
    }));
  }

  @Test
  public void testPreventLengthMaxIndexOutOfBoundGetColumnNameRows(TestContext ctx) {
    Async async = ctx.async();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"").execute(
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNull(row.getColumnName(1));
          async.complete();
        }));
    }));
  }


}
