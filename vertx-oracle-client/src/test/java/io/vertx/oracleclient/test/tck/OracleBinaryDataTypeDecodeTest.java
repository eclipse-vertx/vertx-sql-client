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

package io.vertx.oracleclient.test.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.test.junit.OracleRule;
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;

@RunWith(VertxUnitRunner.class)
public class OracleBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {

  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected JDBCType getNumericJDBCType() {
    return JDBCType.NUMERIC;
  }

  @Override
  protected Class<? extends Number> getNumericClass() {
    return BigDecimal.class;
  }

  @Override
  protected Number getNumericValue(Number value) {
    return getNumericValue(value.toString());
  }

  @Override
  protected Number getNumericValue(String value) {
    return new BigDecimal(value);
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  public void testSmallInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_2", BigDecimal.class, JDBCType.NUMERIC, BigDecimal.valueOf(32767));
  }

  @Override
  public void testInteger(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_4", BigDecimal.class, JDBCType.NUMERIC, BigDecimal.valueOf(2147483647));
  }

  @Override
  public void testBigInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_8", BigDecimal.class, JDBCType.NUMERIC, BigDecimal.valueOf(9223372036854775807L));
  }

  @Override
  public void testFloat4(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_4", Double.class, JDBCType.FLOAT, 3.40282e38D);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testDouble(TestContext ctx) {
    super.testDouble(ctx);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testBoolean(TestContext ctx) {
    super.testBoolean(ctx);
  }

  @Override
  public void testChar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_char", String.class, JDBCType.CHAR, "testchar");
  }

  @Override
  public void testDate(TestContext ctx) {
    LocalDateTime expected = LocalDate.of(2019, 1, 1).atStartOfDay();
    testDecodeGeneric(ctx, "test_date", LocalDate.class, JDBCType.TIMESTAMP, expected);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testTime(TestContext ctx) {
    super.testTime(ctx);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testNullValues(TestContext ctx) {
    super.testNullValues(ctx);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testSelectAll(TestContext ctx) {
    super.testSelectAll(ctx);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testToJsonObject(TestContext ctx) {
    super.testToJsonObject(ctx);
  }
}
