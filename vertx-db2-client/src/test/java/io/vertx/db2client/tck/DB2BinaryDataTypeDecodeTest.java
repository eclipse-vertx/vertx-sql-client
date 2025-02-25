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

package io.vertx.db2client.tck;

import io.vertx.db2client.junit.DB2Resource;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import io.vertx.tests.sqlclient.tck.BinaryDataTypeDecodeTestBase;
import org.junit.*;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.sql.JDBCType;
import java.time.LocalTime;

import static org.junit.Assume.assumeFalse;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
  @ClassRule
  public static DB2Resource rule = DB2Resource.SHARED_INSTANCE;

  @Rule
  public TestName testName = new TestName();

  @Before
  public void printTestName(TestContext ctx) throws Exception {
    System.out.println(">>> BEGIN " + getClass().getSimpleName() + "." + testName.getMethodName());
  }

  @Override
  protected JDBCType getNumericJDBCType() {
    return JDBCType.DECIMAL;
  }

  @Override
  protected Class<? extends Number> getNumericClass() {
    return Numeric.class;
  }

  @Override
  protected Number getNumericValue(Number value) {
    return Numeric.create(value);
  }

  @Override
  protected Number getNumericValue(String value) {
    return Numeric.parse(value);
  }

  @Override
  protected void initConnector() {
    connector = ClientConfig.CONNECT.connect(vertx, rule.options());
  }

  @Test
  @Override
  public void testBoolean(TestContext ctx) {
    // DB2/Z does not support BOOLEAN column type, use TINYINT instead
    // DB2/LUW has a BOOLEAN column type but it is just an alias for TINYINT
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals((short) 1, row.getValue(0));
        ctx.assertEquals((short) 1, row.getValue("test_boolean"));
        ctx.assertEquals(true, row.getBoolean(0));
        conn.close();
      }));
    }));
  }

  @Test
  @Override
  public void testDouble(TestContext ctx) {
    if (!rule.isZOS()) {
      super.testDouble(ctx);
      return;
    }

    // For DB2/z the largest value that can be stored in a DOUBLE column is 7.2E75
    testDecodeGeneric(ctx, "test_float_8", Double.class, JDBCType.DOUBLE, (double) 7.2E75);
  }

  @Override
  public void testChar(TestContext ctx) {
    // Override to expecting JDBCType.CHAR instead of VARCHAR
    testDecodeGeneric(ctx, "test_char", String.class, JDBCType.CHAR, "testchar");
  }

  @Override
  public void testTime(TestContext ctx) {
    // Override to expecting JDBCType.TIME instead of DATE
    testDecodeGeneric(ctx, "test_time", LocalTime.class, JDBCType.TIME, LocalTime.of(18, 45, 2));
  }

  @Test
  @Override
  public void testSelectAll(TestContext ctx) {
    assumeFalse(rule.isZOS());
    super.testSelectAll(ctx);
  }

  @Test
  @Ignore
  @Override
  public void testToJsonObject(TestContext ctx) {
    super.testToJsonObject(ctx);
  }

  @Override
  protected void verifyTypeName(TestContext ctx, ColumnDescriptor columnDescriptor) {
    ctx.assertNull(columnDescriptor.typeName());
  }
}
