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
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import org.junit.ClassRule;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.math.BigDecimal;
import java.sql.JDBCType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.function.BiFunction;

@RunWith(VertxUnitRunner.class)
public class OracleBinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {

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
  protected String statement(String... parts) {
    return String.join(" ?", parts);
  }

  @Override
  public void testSmallInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_2", BigDecimal.class, null, BigDecimal.valueOf(Short.MIN_VALUE));
  }

  @Override
  public void testInteger(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_4", BigDecimal.class, null, BigDecimal.valueOf(Integer.MIN_VALUE));
  }

  @Override
  public void testBigInt(TestContext ctx) {
    testEncodeGeneric(ctx, "test_int_8", BigDecimal.class, null, BigDecimal.valueOf(Long.MIN_VALUE));
  }

  @Override
  public void testFloat4(TestContext ctx) {
    testEncodeGeneric(ctx, "test_float_4", Double.class, null, -3.402823e38D);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testDouble(TestContext ctx) {
    super.testDouble(ctx);
  }

  @Override
  public void testChar(TestContext ctx) {
    super.testChar(ctx);
  }

  @Override
  public void testVarchar(TestContext ctx) {
    super.testVarchar(ctx);
  }

  @Test
  @Ignore("unsupported")
  @Override
  public void testBoolean(TestContext ctx) {
    super.testBoolean(ctx);
  }

  @Override
  public void testDate(TestContext ctx) {
    LocalDateTime expected = LocalDate.parse("1999-12-31").atStartOfDay();
    testEncodeGeneric(ctx, "test_date", LocalDateTime.class, null, expected);
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

  protected <T> void testEncodeGeneric(TestContext ctx,
                                       String columnName,
                                       Class<? extends T> clazz,
                                       BiFunction<Row, String, T> getter,
                                       T expected) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery(statement("UPDATE basicdatatype SET " + columnName + " = ", " WHERE id = 2"))
        .execute(Tuple.tuple().addValue(expected), ctx.asyncAssertSuccess(updateResult -> {
          conn
            .preparedQuery("SELECT " + columnName + " FROM basicdatatype WHERE id = 2")
            .execute(ctx.asyncAssertSuccess(result -> {
              ctx.assertEquals(1, result.size());
              Row row = result.iterator().next();
              ctx.assertEquals(1, row.size());
              ctx.assertEquals(expected, row.getValue(0));
              ctx.assertEquals(expected, row.getValue(columnName));
              if (getter != null) {
                ctx.assertEquals(expected, getter.apply(row, columnName));
              }
//        ctx.assertEquals(expected, row.get(clazz, 0));
//        ColumnChecker.checkColumn(0, columnName)
//          .returns(Tuple::getValue, Row::getValue, expected)
//          .returns(byIndexGetter, byNameGetter, expected)
//          .forRow(row);
              conn.close();
            }));
        }));
    }));
  }
}
