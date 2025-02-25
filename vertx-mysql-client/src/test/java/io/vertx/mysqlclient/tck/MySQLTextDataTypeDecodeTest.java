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
import io.vertx.tests.sqlclient.tck.TextDataTypeDecodeTestBase;
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
      conn
        .query("SELECT test_boolean FROM basicdatatype WHERE id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
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

  @Test
  public void test_jdbc_type(TestContext ctx) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT * from datatype where id = 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());

          ctx.assertEquals(result.columnDescriptors().get(0).name(), "id");
          ctx.assertEquals(result.columnDescriptors().get(0).jdbcType(), JDBCType.INTEGER);
          ctx.assertEquals(result.columnDescriptors().get(1).name(), "Binary");
          ctx.assertEquals(result.columnDescriptors().get(1).jdbcType(), JDBCType.BINARY);
          ctx.assertEquals(result.columnDescriptors().get(2).name(), "VarBinary");
          ctx.assertEquals(result.columnDescriptors().get(2).jdbcType(), JDBCType.VARBINARY);
          ctx.assertEquals(result.columnDescriptors().get(3).name(), "TinyBlob");
          ctx.assertEquals(result.columnDescriptors().get(3).jdbcType(), JDBCType.BLOB);
          ctx.assertEquals(result.columnDescriptors().get(4).name(), "Blob");
          ctx.assertEquals(result.columnDescriptors().get(4).jdbcType(), JDBCType.BLOB);
          ctx.assertEquals(result.columnDescriptors().get(5).name(), "MediumBlob");
          ctx.assertEquals(result.columnDescriptors().get(5).jdbcType(), JDBCType.BLOB);
          ctx.assertEquals(result.columnDescriptors().get(6).name(), "LongBlob");
          ctx.assertEquals(result.columnDescriptors().get(6).jdbcType(), JDBCType.BLOB);
          ctx.assertEquals(result.columnDescriptors().get(7).name(), "TinyText");
          ctx.assertEquals(result.columnDescriptors().get(7).jdbcType(), JDBCType.CLOB);
          ctx.assertEquals(result.columnDescriptors().get(8).name(), "Text");
          ctx.assertEquals(result.columnDescriptors().get(8).jdbcType(), JDBCType.CLOB);
          ctx.assertEquals(result.columnDescriptors().get(9).name(), "MediumText");
          ctx.assertEquals(result.columnDescriptors().get(9).jdbcType(), JDBCType.CLOB);
          ctx.assertEquals(result.columnDescriptors().get(10).name(), "LongText");
          ctx.assertEquals(result.columnDescriptors().get(10).jdbcType(), JDBCType.CLOB);
          ctx.assertEquals(result.columnDescriptors().get(11).name(), "test_enum");
          ctx.assertEquals(result.columnDescriptors().get(11).jdbcType(), JDBCType.CHAR);
          ctx.assertEquals(result.columnDescriptors().get(12).name(), "test_set");
          ctx.assertEquals(result.columnDescriptors().get(12).jdbcType(), JDBCType.CHAR);
          ctx.assertEquals(result.columnDescriptors().get(13).name(), "test_year");
          ctx.assertEquals(result.columnDescriptors().get(13).jdbcType(), JDBCType.SMALLINT);
          ctx.assertEquals(result.columnDescriptors().get(14).name(), "test_timestamp");
          ctx.assertEquals(result.columnDescriptors().get(14).jdbcType(), JDBCType.TIMESTAMP);
          ctx.assertEquals(result.columnDescriptors().get(15).name(), "test_datetime");
          ctx.assertEquals(result.columnDescriptors().get(15).jdbcType(), JDBCType.TIMESTAMP);
          ctx.assertEquals(result.columnDescriptors().get(16).name(), "test_bit");
          ctx.assertEquals(result.columnDescriptors().get(16).jdbcType(), JDBCType.BIT);
          ctx.assertEquals(result.columnDescriptors().get(17).name(), "test_unsigned_tinyint");
          ctx.assertEquals(result.columnDescriptors().get(17).jdbcType(), JDBCType.SMALLINT);
          ctx.assertEquals(result.columnDescriptors().get(18).name(), "test_unsigned_smallint");
          ctx.assertEquals(result.columnDescriptors().get(18).jdbcType(), JDBCType.INTEGER);
          ctx.assertEquals(result.columnDescriptors().get(19).name(), "test_unsigned_mediumint");
          ctx.assertEquals(result.columnDescriptors().get(19).jdbcType(), JDBCType.INTEGER);
          ctx.assertEquals(result.columnDescriptors().get(20).name(), "test_unsigned_int");
          ctx.assertEquals(result.columnDescriptors().get(20).jdbcType(), JDBCType.BIGINT);
          ctx.assertEquals(result.columnDescriptors().get(21).name(), "test_unsigned_bigint");
          ctx.assertEquals(result.columnDescriptors().get(21).jdbcType(), JDBCType.NUMERIC);
          ctx.assertEquals(result.columnDescriptors().get(22).name(), "test_varchar_binary");
          ctx.assertEquals(result.columnDescriptors().get(22).jdbcType(), JDBCType.VARCHAR);
          ctx.assertEquals(result.columnDescriptors().get(23).name(), "test_varchar_with_binary_collation");
          ctx.assertEquals(result.columnDescriptors().get(23).jdbcType(), JDBCType.VARBINARY);
          ctx.assertEquals(result.columnDescriptors().get(24).name(), "test_text_binary");
          ctx.assertEquals(result.columnDescriptors().get(24).jdbcType(), JDBCType.CLOB);
        }));
    }));
  }
}
