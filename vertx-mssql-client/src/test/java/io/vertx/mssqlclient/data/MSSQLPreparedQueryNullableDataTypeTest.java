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
import io.vertx.sqlclient.data.Numeric;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.function.Consumer;

@RunWith(VertxUnitRunner.class)
public class MSSQLPreparedQueryNullableDataTypeTest extends MSSQLNullableDataTypeTestBase {

  @Test
  public void testEncodeTinyInt(TestContext ctx) {
    testEncodeTinyIntValue(ctx, (short) 255);
  }

  @Test
  public void testEncodeNullTinyInt(TestContext ctx) {
    testEncodeTinyIntValue(ctx, null);
  }

  private void testEncodeTinyIntValue(TestContext ctx, Short value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_tinyint", value, row -> {
      ctx.assertEquals(value, row.getValue("test_tinyint"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getShort("test_tinyint"));
      ctx.assertEquals(value, row.getShort(0));
      ctx.assertEquals(value, row.get(Short.class, "test_tinyint"));
      ctx.assertEquals(value, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeSmallInt(TestContext ctx) {
    testEncodeSmallIntValue(ctx, (short) -32768);
  }

  @Test
  public void testEncodeNullSmallInt(TestContext ctx) {
    testEncodeSmallIntValue(ctx, null);
  }

  private void testEncodeSmallIntValue(TestContext ctx, Short value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_smallint", value, row -> {
      ctx.assertEquals(value, row.getValue("test_smallint"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getShort("test_smallint"));
      ctx.assertEquals(value, row.getShort(0));
      ctx.assertEquals(value, row.get(Short.class, "test_smallint"));
      ctx.assertEquals(value, row.get(Short.class, 0));
    });
  }

  @Test
  public void testEncodeInt(TestContext ctx) {
    testEncodeIntValue(ctx, -2147483648);
  }

  @Test
  public void testEncodeNullInt(TestContext ctx) {
    testEncodeIntValue(ctx, null);
  }

  private void testEncodeIntValue(TestContext ctx, Integer value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_int", value, row -> {
      ctx.assertEquals(value, row.getValue("test_int"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getInteger("test_int"));
      ctx.assertEquals(value, row.getInteger(0));
      ctx.assertEquals(value, row.get(Integer.class, "test_int"));
      ctx.assertEquals(value, row.get(Integer.class, 0));
    });
  }

  @Test
  public void testEncodeBigInt(TestContext ctx) {
    testEncodeBigIntValue(ctx, -9223372036854775808L);
  }

  @Test
  public void testEncodeNullBigInt(TestContext ctx) {
    testEncodeBigIntValue(ctx, null);
  }

  private void testEncodeBigIntValue(TestContext ctx, Long value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_bigint", value, row -> {
      ctx.assertEquals(value, row.getValue("test_bigint"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getLong("test_bigint"));
      ctx.assertEquals(value, row.getLong(0));
      ctx.assertEquals(value, row.get(Long.class, "test_bigint"));
      ctx.assertEquals(value, row.get(Long.class, 0));
    });
  }

  @Test
  public void testEncodeFloat4(TestContext ctx) {
    testEncodeFloat4Value(ctx, (float) -3.40282E38);
  }

  @Test
  public void testEncodeNullFloat4(TestContext ctx) {
    testEncodeFloat4Value(ctx, null);
  }

  private void testEncodeFloat4Value(TestContext ctx, Float value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_float_4", value, row -> {
      ctx.assertEquals(value, row.getValue("test_float_4"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getFloat("test_float_4"));
      ctx.assertEquals(value, row.getFloat(0));
      ctx.assertEquals(value, row.get(Float.class, "test_float_4"));
      ctx.assertEquals(value, row.get(Float.class, 0));
    });
  }

  @Test
  public void testEncodeFloat8(TestContext ctx) {
    testEncodeFloat8Value(ctx, -1.7976931348623157E308);
  }

  @Test
  public void testEncodeNullFloat8(TestContext ctx) {
    testEncodeFloat8Value(ctx, null);
  }

  private void testEncodeFloat8Value(TestContext ctx, Double value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_float_8", value, row -> {
      ctx.assertEquals(value, row.getValue("test_float_8"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getDouble("test_float_8"));
      ctx.assertEquals(value, row.getDouble(0));
      ctx.assertEquals(value, row.get(Double.class, "test_float_8"));
      ctx.assertEquals(value, row.get(Double.class, 0));
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeNumeric(TestContext ctx) {
    testEncodeNumericValue(ctx, Numeric.create(123456789.13));
  }

  @Test
  public void testEncodeNullNumeric(TestContext ctx) {
    testEncodeNumericValue(ctx, null);
  }

  private void testEncodeNumericValue(TestContext ctx, Numeric value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_numeric", value, row -> {
      ctx.assertEquals(value, row.getValue("test_numeric"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.get(Numeric.class, "test_numeric"));
      ctx.assertEquals(value, row.get(Numeric.class, 0));
    });
  }

  @Test
  @Ignore //FIXME
  public void testEncodeDecimal(TestContext ctx) {
    testEncodeDecimalValue(ctx, Numeric.create(123456789));
  }

  @Test
  public void testEncodeNullDecimal(TestContext ctx) {
    testEncodeDecimalValue(ctx, null);
  }

  private void testEncodeDecimalValue(TestContext ctx, Numeric value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_decimal", value, row -> {
      ctx.assertEquals(value, row.getValue("test_decimal"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.get(Numeric.class, "test_decimal"));
      ctx.assertEquals(value, row.get(Numeric.class, 0));
    });
  }

  @Test
  public void testEncodeBit(TestContext ctx) {
    testEncodeBitValue(ctx, false);
  }

  @Test
  public void testEncodeNullBit(TestContext ctx) {
    testEncodeBitValue(ctx, null);
  }

  private void testEncodeBitValue(TestContext ctx, Boolean value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_boolean", value, row -> {
      ctx.assertEquals(value, row.getValue("test_boolean"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getBoolean("test_boolean"));
      ctx.assertEquals(value, row.getBoolean(0));
      ctx.assertEquals(value, row.get(Boolean.class, "test_boolean"));
      ctx.assertEquals(value, row.get(Boolean.class, 0));
    });
  }

  @Test
  public void testEncodeChar(TestContext ctx) {
    testEncodeCharValue(ctx, "chartest");
  }

  @Test
  public void testEncodeNullChar(TestContext ctx) {
    testEncodeCharValue(ctx, null);
  }

  private void testEncodeCharValue(TestContext ctx, String value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_char", value, row -> {
      ctx.assertEquals(value, row.getValue("test_char"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getString("test_char"));
      ctx.assertEquals(value, row.getString(0));
      ctx.assertEquals(value, row.get(String.class, "test_char"));
      ctx.assertEquals(value, row.get(String.class, 0));
    });
  }

  @Test
  public void testEncodeVarChar(TestContext ctx) {
    testEncodeVarCharValue(ctx, "testedvarchar");
  }

  @Test
  public void testEncodeNullVarChar(TestContext ctx) {
    testEncodeVarCharValue(ctx, null);
  }

  private void testEncodeVarCharValue(TestContext ctx, String value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_varchar", value, row -> {
      ctx.assertEquals(value, row.getValue("test_varchar"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getString("test_varchar"));
      ctx.assertEquals(value, row.getString(0));
      ctx.assertEquals(value, row.get(String.class, "test_varchar"));
      ctx.assertEquals(value, row.get(String.class, 0));
    });
  }

  @Test
  public void testEncodeDate(TestContext ctx) {
    testEncodeDateValue(ctx, LocalDate.of(1999, 12, 31));
  }

  @Test
  public void testEncodeNullDate(TestContext ctx) {
    testEncodeDateValue(ctx, null);
  }

  private void testEncodeDateValue(TestContext ctx, LocalDate value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_date", value, row -> {
      ctx.assertEquals(value, row.getValue("test_date"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getLocalDate("test_date"));
      ctx.assertEquals(value, row.getLocalDate(0));
      ctx.assertEquals(value, row.get(LocalDate.class, "test_date"));
      ctx.assertEquals(value, row.get(LocalDate.class, 0));
    });
  }

  @Test
  public void testEncodeTime(TestContext ctx) {
    testEncodeTimeValue(ctx, LocalTime.of(23, 10, 45));
  }

  @Test
  public void testEncodeNullTime(TestContext ctx) {
    testEncodeTimeValue(ctx, null);
  }

  private void testEncodeTimeValue(TestContext ctx, LocalTime value) {
    testPreparedQueryEncodeGeneric(ctx, "nullable_datatype", "test_time", value, row -> {
      ctx.assertEquals(value, row.getValue("test_time"));
      ctx.assertEquals(value, row.getValue(0));
      ctx.assertEquals(value, row.getLocalTime("test_time"));
      ctx.assertEquals(value, row.getLocalTime(0));
      ctx.assertEquals(value, row.get(LocalTime.class, "test_time"));
      ctx.assertEquals(value, row.get(LocalTime.class, 0));
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
