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

package tests.oracleclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import tests.oracleclient.junit.OracleRule;
import io.vertx.tests.sqlclient.tck.TextDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@RunWith(VertxUnitRunner.class)
public class OracleTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {

  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  protected JDBCType getNumericJDBCType() {
    return JDBCType.DECIMAL;
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
    BigDecimal bd = new BigDecimal(value);
    return bd;
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Override
  public void testSmallInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_2", BigDecimal.class, BigDecimal.valueOf(32767));
  }

  @Override
  public void testInteger(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_4", BigDecimal.class, BigDecimal.valueOf(2147483647));
  }

  @Override
  public void testBigInt(TestContext ctx) {
    testDecodeGeneric(ctx, "test_int_8", BigDecimal.class, BigDecimal.valueOf(9223372036854775807L));
  }

  @Override
  public void testFloat4(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_4", Double.class, 3.40282e38D);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testDouble(TestContext ctx) {
    super.testDouble(ctx);
  }

  @Override
  public void testNumeric(TestContext ctx) {
    testDecodeGeneric(ctx, "test_numeric", getNumericClass(), getNumericValue("999.99"));
  }

  @Override
  public void testDecimal(TestContext ctx) {
    testDecodeGeneric(ctx, "test_decimal", getNumericClass(), getNumericValue("12345"));
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testBoolean(TestContext ctx) {
    super.testBoolean(ctx);
  }

  @Override
  public void testDate(TestContext ctx) {
    LocalDateTime expected = LocalDate.of(2019, 1, 1).atStartOfDay();
    testDecodeGeneric(ctx, "test_date", OffsetDateTime.class, expected);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testTime(TestContext ctx) {
    super.testTime(ctx);
  }
}
