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

package io.vertx.mysqlclient.tck;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.junit.MySQLRule;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.data.Numeric;
import io.vertx.sqlclient.tck.TextDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.JDBCType;
import java.time.Duration;

@RunWith(VertxUnitRunner.class)
public class MySQLTextDataTypeDecodeTest extends TextDataTypeDecodeTestBase {
  @ClassRule
  public static MySQLRule rule = MySQLRule.SHARED_INSTANCE;

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
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT test_boolean FROM basicdatatype WHERE id = 1").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(true, row.getBoolean(0));
        ctx.assertEquals(true, row.getBoolean("test_boolean"));
        ctx.assertEquals((byte) 1, row.getValue(0));
        ctx.assertEquals((byte) 1, row.getValue("test_boolean"));
      }));
    }));
  }

  @Test
  @Override
  public void testTime(TestContext ctx) {
    // MySQL TIME type is mapped to java.time.Duration so we need to override here
    testDecodeGeneric(ctx, "test_time", Duration.class, Duration.ofHours(18).plusMinutes(45).plusSeconds(2));
  }
}
