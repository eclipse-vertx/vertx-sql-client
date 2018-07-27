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
package io.reactiverse.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class RowTest extends PgTestBase {

  Vertx vertx;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
  }

  @After
  public void teardown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testGetNonExistingRows(TestContext ctx) {
    Async async = ctx.async();
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 1 \"foo\"",
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          List<Function<String, ?>> functions = Arrays.asList(
            row::getValue,
            row::getString,
            row::getBuffer,
            row::getDouble,
            row::getShort,
            row::getInteger,
            row::getLong,
            row::getBigDecimal,
            row::getNumeric,
            row::getFloat,
            row::getLocalDate,
            row::getLocalTime,
            row::getOffsetDateTime,
            row::getLocalDateTime,
            row::getOffsetTime,
            row::getTemporal,
            row::getUUID,
            row::getPoint,
            row::getInterval,
            row::getBooleanArray,
            row::getShortArray,
            row::getIntegerArray,
            row::getLongArray,
            row::getFloatArray,
            row::getDoubleArray,
            row::getStringArray,
            row::getLocalDateArray,
            row::getLocalTimeArray,
            row::getOffsetTimeArray,
            row::getLocalDateTimeArray,
            row::getOffsetDateTimeArray,
            row::getBufferArray,
            row::getUUIDArray,
            row::getPointArray,
            row::getIntervalArray
          );
          functions.forEach(f -> {
            ctx.assertEquals(null, f.apply("bar"));
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"",
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"",
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"",
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
    PgClient.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT 2 \"foo\"",
        ctx.asyncAssertSuccess(result -> {
          Row row = result.iterator().next();
          ctx.assertNull(row.getColumnName(1));
          async.complete();
        }));
    }));
  }


}
