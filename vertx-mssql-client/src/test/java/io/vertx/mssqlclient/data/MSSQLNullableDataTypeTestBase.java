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
import org.junit.Test;

import java.util.function.Consumer;

public abstract class MSSQLNullableDataTypeTestBase extends MSSQLDataTypeTestBase {
  @Test
  public void testDecodeAllColumns(TestContext ctx) {
    testDecodeNotNullValue(ctx, "*", row -> {
      ctx.assertEquals((byte) 127, row.getValue("test_tinyint"));
      ctx.assertEquals((short) 32767, row.getValue("test_smallint"));
      ctx.assertEquals(2147483647, row.getValue("test_int"));
      ctx.assertEquals(9223372036854775807L, row.getValue("test_bigint"));
    });
  }

  @Test
  public void testDecodeNullAllColumns(TestContext ctx) {
    testDecodeNullValue(ctx, "*", row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue("test_smallint"));
      ctx.assertEquals(null, row.getValue("test_int"));
      ctx.assertEquals(null, row.getValue("test_bigint"));
    });
  }

  @Test
  public void testDecodeTinyInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_tinyint", row -> {
      ctx.assertEquals((byte) 127, row.getValue("test_tinyint"));
      ctx.assertEquals((byte) 127, row.getValue(0));
      ctx.assertEquals((byte) 127, row.get(Byte.class, "test_tinyint"));
      ctx.assertEquals((byte) 127, row.get(Byte.class, 0));
    });
  }

  @Test
  public void testDecodeNullTinyInt(TestContext ctx) {
    testDecodeNullValue(ctx, "test_tinyint", row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.get(Byte.class, "test_tinyint"));
      ctx.assertEquals(null, row.get(Byte.class, 0));
    });
  }

  @Test
  public void testDecodeSmallIntInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_smallint", row -> {
      ctx.assertEquals((short) 32767, row.getValue("test_smallint"));
      ctx.assertEquals((short) 32767, row.getValue(0));
      ctx.assertEquals((short) 32767, row.getShort("test_smallint"));
      ctx.assertEquals((short) 32767, row.getShort(0));
      ctx.assertEquals((short) 32767, row.get(Short.class, "test_smallint"));
      ctx.assertEquals((short) 32767, row.get(Short.class, 0));
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
  public void testDecodeInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_int", row -> {
      ctx.assertEquals(2147483647, row.getValue("test_int"));
      ctx.assertEquals(2147483647, row.getValue(0));
      ctx.assertEquals(2147483647, row.getInteger("test_int"));
      ctx.assertEquals(2147483647, row.getInteger(0));
      ctx.assertEquals(2147483647, row.get(Integer.class, "test_int"));
      ctx.assertEquals(2147483647, row.get(Integer.class, 0));
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
  public void testDecodeBigInt(TestContext ctx) {
    testDecodeNotNullValue(ctx, "test_bigint", row -> {
      ctx.assertEquals(9223372036854775807L, row.getValue("test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.getValue(0));
      ctx.assertEquals(9223372036854775807L, row.getLong("test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.getLong(0));
      ctx.assertEquals(9223372036854775807L, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(9223372036854775807L, row.get(Long.class, 0));
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

  protected abstract void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker);

  private void testDecodeNotNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, false, columnName, checker);
  }

  private void testDecodeNullValue(TestContext ctx, String columnName, Consumer<Row> checker) {
    testDecodeValue(ctx, true, columnName, checker);
  }

}
