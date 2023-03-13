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

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class DateTimeTextCodecTest extends DateTimeCodecTest {
  @Test
  public void testTextDecodeAll(TestContext ctx) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT `test_year`, `test_timestamp`, `test_datetime` FROM datatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(3, row.size());
        ctx.assertEquals((short) 2019, row.getValue(0));
        ctx.assertEquals(LocalDateTime.of(2000, 1, 1, 10, 20, 30), row.getValue(1));
        ctx.assertEquals(LocalDateTime.of(2000, 1, 1, 10, 20, 30, 123456000), row.getValue(2));
        conn.close();
      }));
    }));
  }

  @Test
  public void testDecodeYear(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_year", (short) 2019);
  }

  @Test
  public void testDecodeTimestamp(TestContext ctx) {
    testTextDecodeGenericWithTable(ctx, "test_timestamp", LocalDateTime.of(2000, 1, 1, 10, 20, 30));
  }

  @Override
  protected <T> void testDecodeGeneric(TestContext ctx, String data, String dataType, String columnName, T expected) {
    testDecodeGeneric(ctx, data, dataType, row -> {
      ctx.assertEquals(expected, row.getValue(0));
      ctx.assertEquals(expected, row.getValue(columnName));
    }, columnName);
  }

  @Override
  protected void testDecodeGeneric(TestContext ctx, String data, String dataType, Consumer<Row> valueAccessor, String columnName) {
    MySQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT CAST(\'" + data + "\' AS " + dataType + ") " + columnName)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        valueAccessor.accept(row);
        conn.close();
      }));
    }));
  }
}
