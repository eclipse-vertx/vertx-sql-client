/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.mssqlclient.data;

import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import org.junit.Test;

import java.util.function.Consumer;

public abstract class MSSQLNullableDataTypeTestBase extends MSSQLFullDataTypeTestBase {

  @Test
  public void testDecodeNullAllColumns(TestContext ctx) {
    testDecodeNullValue(ctx, "*", row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue("test_smallint"));
      ctx.assertEquals(null, row.getValue("test_int"));
      ctx.assertEquals(null, row.getValue("test_bigint"));
      ctx.assertEquals(null, row.getValue("test_float_4"));
      ctx.assertEquals(null, row.getValue("test_float_8"));
      ctx.assertEquals(null, row.getValue("test_numeric"));
      ctx.assertEquals(null, row.getValue("test_decimal"));
      ctx.assertEquals(null, row.getValue("test_boolean"));
      ctx.assertEquals(null, row.getValue("test_char"));
      ctx.assertEquals(null, row.getValue("test_varchar"));
      ctx.assertEquals(null, row.getValue("test_date"));
      ctx.assertEquals(null, row.getValue("test_time"));
    });
  }

  @Test
  public void testDecodeNullTinyInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_tinyint", row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.get(Short.class, "test_tinyint"));
      ctx.assertEquals(null, row.get(Short.class, 0));
    });
  }

  @Test
  public void testDecodeNullSmallIntInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_smallint", row -> {
      ctx.assertEquals(null, row.getValue("test_smallint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getShort("test_smallint"));
      ctx.assertEquals(null, row.getShort(0));
      ctx.assertEquals(null, row.get(Short.class, "test_smallint"));
      ctx.assertEquals(null, row.get(Short.class, 0));
    });
  }

  @Test
  public void testDecodeNullInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_int", row -> {
      ctx.assertEquals(null, row.getValue("test_int"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getInteger("test_int"));
      ctx.assertEquals(null, row.getInteger(0));
      ctx.assertEquals(null, row.get(Integer.class, "test_int"));
      ctx.assertEquals(null, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testDecodeNullBigInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_bigint", row -> {
      ctx.assertEquals(null, row.getValue("test_bigint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getLong("test_bigint"));
      ctx.assertEquals(null, row.getLong(0));
      ctx.assertEquals(null, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(null, row.get(Long.class, 0));
    });
  }

  @Test
  public void testDecodeNullFloat4(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_4", row -> {
      ctx.assertEquals(null, row.getValue("test_float_4"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getFloat("test_float_4"));
      ctx.assertEquals(null, row.getFloat(0));
      ctx.assertEquals(null, row.get(Float.class, "test_float_4"));
      ctx.assertEquals(null, row.get(Float.class, 0));
    });
  }

  @Test
  public void testDecodeNullFloat8(TestContext ctx) {
    testDecodeNullValue(ctx, "test_float_8", row -> {
      ctx.assertEquals(null, row.getValue("test_float_8"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getFloat("test_float_8"));
      ctx.assertEquals(null, row.getFloat(0));
      ctx.assertEquals(null, row.get(Float.class, "test_float_8"));
      ctx.assertEquals(null, row.get(Float.class, 0));
    });
  }

  @Test
  public void testDecodeNullNumeric(TestContext ctx) {
    testDecodeNullValue(ctx, "test_numeric", row -> {
      ctx.assertEquals(null, row.getValue("test_numeric"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.get(Numeric.class, "test_numeric"));
      ctx.assertEquals(null, row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testDecodeNullDecimal(TestContext ctx) {
    testDecodeNullValue(ctx, "test_decimal", row -> {
      ctx.assertEquals(null, row.getValue("test_decimal"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.get(Numeric.class, "test_decimal"));
      ctx.assertEquals(null, row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testDecodeNullBit(TestContext ctx) {
    testDecodeNullValue(ctx, "test_boolean", row -> {
      ctx.assertEquals(null, row.getValue("test_boolean"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getBoolean("test_boolean"));
      ctx.assertEquals(null, row.getBoolean(0));
      ctx.assertEquals(null, row.get(Boolean.class, "test_boolean"));
      ctx.assertEquals(null, row.get(Boolean.class, 0));
    });
  }

  private void testDecodeNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, true, columnName, checker);
  }
}
