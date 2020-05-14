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
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNullableDataTypeTest extends MSSQLNullableDataTypeTestBase {

  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_tinyint", (byte) 255, row -> {
      ctx.assertEquals((byte) 255, row.getValue("test_tinyint"));
      ctx.assertEquals((byte) 255, row.getValue(0));
      ctx.assertEquals((byte) 255, row.get(Byte.class, "test_tinyint"));
      ctx.assertEquals((byte) 255, row.get(Byte.class, 0));
    });
  }

  @Test
  public void testEncodeNullTinyInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_tinyint", null, row -> {
      ctx.assertEquals(null, row.getValue("test_tinyint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.get(Byte.class, "test_tinyint"));
      ctx.assertEquals(null, row.get(Byte.class, 0));
    });
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_smallint", (short) -32768, row -> {
      ctx.assertEquals((short) -32768, row.getValue("test_smallint"));
      ctx.assertEquals((short) -32768, row.getValue(0));
      ctx.assertEquals((short) -32768, row.getShort("test_smallint"));
      ctx.assertEquals((short) -32768, row.getShort(0));
      ctx.assertEquals((short) -32768, row.get(Short.class, "test_smallint"));
      ctx.assertEquals((short) -32768, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeNullSmallInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_smallint", null, row -> {
      ctx.assertEquals(null, row.getValue("test_smallint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getShort("test_smallint"));
      ctx.assertEquals(null, row.getShort(0));
      ctx.assertEquals(null, row.get(Short.class, "test_smallint"));
      ctx.assertEquals(null, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_int", -2147483648, row -> {
      ctx.assertEquals(-2147483648, row.getValue("test_int"));
      ctx.assertEquals(-2147483648, row.getValue(0));
      ctx.assertEquals(-2147483648, row.getInteger("test_int"));
      ctx.assertEquals(-2147483648, row.getInteger(0));
      ctx.assertEquals(-2147483648, row.get(Integer.class, "test_int"));
      ctx.assertEquals(-2147483648, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testEncodeNullInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_int", null, row -> {
      ctx.assertEquals(null, row.getValue("test_int"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getInteger("test_int"));
      ctx.assertEquals(null, row.getInteger(0));
      ctx.assertEquals(null, row.get(Integer.class, "test_int"));
      ctx.assertEquals(null, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_bigint", -9223372036854775808L, row -> {
      ctx.assertEquals(-9223372036854775808L, row.getValue("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getValue(0));
      ctx.assertEquals(-9223372036854775808L, row.getLong("test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.getLong(0));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(-9223372036854775808L, row.get(Long.class, 0));
    });
  }

  @Test
  public void testEncodeNullBigInt(TestContext ctx) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_bigint", null, row -> {
      ctx.assertEquals(null, row.getValue("test_bigint"));
      ctx.assertEquals(null, row.getValue(0));
      ctx.assertEquals(null, row.getLong("test_bigint"));
      ctx.assertEquals(null, row.getLong(0));
      ctx.assertEquals(null, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(null, row.get(Long.class, 0));
    });
  }

  @Override
  protected void testDecodeValue(TestContext ctx, boolean isNull, String columnName, Consumer<Row> checker) {
    if (isNull) {
      testPreparedQueryDecodeGeneric(ctx, "nullable_datatype", columnName, "3", checker);
    } else {
      testPreparedQueryDecodeGeneric(ctx, "nullable_datatype", columnName, "1", checker);
    }
  }
}
