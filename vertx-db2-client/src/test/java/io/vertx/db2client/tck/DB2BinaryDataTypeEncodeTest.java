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
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.tests.sqlclient.tck.BinaryDataTypeEncodeTestBase;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import java.sql.JDBCType;

@RunWith(VertxUnitRunner.class)
public class DB2BinaryDataTypeEncodeTest extends BinaryDataTypeEncodeTestBase {
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
    return JDBCType.NUMERIC;
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

  @Override
  protected String statement(String... parts) {
    return String.join("?", parts);
  }

  @Test
  @Override
  public void testDouble(TestContext ctx) {
    // The smallest positive value supported by the DOUBLE column type in DB2 is 5.4E-079
    testEncodeGeneric(ctx, "test_float_8", Double.class, Row::getDouble, Double.valueOf("5.4E-079"));
  }

  @Override
  public void testBoolean(TestContext ctx) {
    // DB2/Z doesn't have a BOOLEAN column type and uses TINYINT instead
  // DB2/LUW has a BOOLEAN column type but it is just an alias for TINYINT
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .preparedQuery("UPDATE basicdatatype SET test_boolean = ? WHERE id = 2")
        .execute(Tuple.tuple().addValue(false))
        .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        conn
          .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals((short) 0, row.getValue(0));
          ctx.assertEquals((short) 0, row.getValue("test_boolean"));
          ctx.assertEquals(false, row.getBoolean(0));
          conn.close();
        }));
      }));
    }));
  }

}
