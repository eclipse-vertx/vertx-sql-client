/*
 * Copyright (c) 2011-2021 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import org.junit.Test;

import java.time.LocalDate;
import java.time.LocalTime;

public abstract class TextDataTypeDecodeTestBase extends DataTypeTestBase {
  @Test
  public void testSmallInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_2", Short.class, (short) 32767);
  }

  @Test
  public void testInteger(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_4", Integer.class, 2147483647);
  }

  @Test
  public void testBigInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_8", Long.class, 9223372036854775807L);
  }

  @Test
  public void testFloat4(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_4", Float.class, 3.40282e38F);
  }

  @Test
  public void testDouble(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_8", Double.class, 1.7976931348623157E308);
  }

  @Test
  public void testNumeric(TestContext ctx) {
    testDecodeGeneric(ctx, "test_numeric", getNumericClass(), getNumericValue("999.99"));
  }

  @Test
  public void testDecimal(TestContext ctx) {
    testDecodeGeneric(ctx, "test_decimal", getNumericClass(), getNumericValue("12345"));
  }

  @Test
  public void testBoolean(TestContext ctx) {
    testDecodeGeneric(ctx, "test_boolean", Boolean.class, true);
  }

  @Test
  public void testChar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_char", String.class, "testchar");
  }

  @Test
  public void testVarchar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_varchar", String.class, "testvarchar");
  }

  @Test
  public void testDate(TestContext ctx) {
    testDecodeGeneric(ctx, "test_date", LocalDate.class, LocalDate.of(2019, 1, 1));
  }

  @Test
  public void testTime(TestContext ctx) {
    testDecodeGeneric(ctx, "test_time", LocalTime.class, LocalTime.of(18, 45, 2));
  }

  protected <T> void testDecodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<? extends T> clazz,
                                       T expected) {
    Async async = ctx.async();
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT " + columnName + " FROM basicdatatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
        async.complete();
      }));
    }));
  }
}
