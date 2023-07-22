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
import io.vertx.sqlclient.tck.BinaryDataTypeDecodeTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.JDBCType;
import java.time.Duration;
import java.time.LocalDate;

@RunWith(VertxUnitRunner.class)
public class MySQLBinaryDataTypeDecodeTest extends BinaryDataTypeDecodeTestBase {
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
        .preparedQuery("SELECT test_boolean FROM basicdatatype WHERE id = 1")
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
    testDecodeGeneric(ctx, "test_time", Duration.class, JDBCType.TIME, Duration.ofHours(18).plusMinutes(45).plusSeconds(2));
  }

  @Test
  @Override
  public void testSelectAll(TestContext ctx) {
  // MySQL TIME type is mapped to java.time.Duration so we need to override here
    connector.connect(ctx.asyncAssertSuccess(conn -> {
        conn
          .preparedQuery("SELECT " +
          "test_int_2," +
          "test_int_4," +
          "test_int_8," +
          "test_float_4," +
          "test_float_8," +
          "test_numeric," +
          "test_decimal," +
          "test_boolean," +
          "test_char," +
          "test_varchar," +
          "test_date," +
          "test_time " +
          "from basicdatatype where id = 1")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
          ctx.assertEquals(1, result.size());
          Row row = result.iterator().next();
          ctx.assertEquals(12, row.size());
          ctx.assertEquals((short) 32767, row.getShort(0));
          ctx.assertEquals(Short.valueOf((short) 32767), row.getShort("test_int_2"));
          ctx.assertEquals(2147483647, row.getInteger(1));
          ctx.assertEquals(2147483647, row.getInteger("test_int_4"));
          ctx.assertEquals(9223372036854775807L, row.getLong(2));
          ctx.assertEquals(9223372036854775807L, row.getLong("test_int_8"));
          ctx.assertEquals(3.40282E38F, row.getFloat(3));
          ctx.assertEquals(3.40282E38F, row.getFloat("test_float_4"));
          ctx.assertEquals(1.7976931348623157E308, row.getDouble(4));
          ctx.assertEquals(1.7976931348623157E308, row.getDouble("test_float_8"));
          ctx.assertEquals(Numeric.create(999.99), row.get(Numeric.class, 5));
          ctx.assertEquals(Numeric.create(999.99), row.getValue("test_numeric"));
          ctx.assertEquals(Numeric.create(12345), row.get(Numeric.class, 6));
          ctx.assertEquals(Numeric.create(12345), row.getValue("test_decimal"));
          ctx.assertEquals(true, row.getBoolean(7));
          ctx.assertEquals(true, row.getBoolean("test_boolean"));
          ctx.assertEquals("testchar", row.getString(8));
          ctx.assertEquals("testchar", row.getString("test_char"));
          ctx.assertEquals("testvarchar", row.getString(9));
          ctx.assertEquals("testvarchar", row.getString("test_varchar"));
          ctx.assertEquals(LocalDate.parse("2019-01-01"), row.getValue(10));
          ctx.assertEquals(LocalDate.parse("2019-01-01"), row.getValue("test_date"));
          ctx.assertEquals(Duration.ofHours(18).plusMinutes(45).plusSeconds(2), row.getValue(11));
          ctx.assertEquals(Duration.ofHours(18).plusMinutes(45).plusSeconds(2), row.getValue("test_time"));
          conn.close();
        }));
    }));
  }

  @Test
  @Override
  public void testFloat4(TestContext ctx) {
    testDecodeGeneric(ctx, "test_float_4", Float.class, JDBCType.FLOAT, 3.40282e38F);
  }

  @Test
  @Override
  public void testChar(TestContext ctx) {
    testDecodeGeneric(ctx, "test_char", String.class, JDBCType.CHAR, "testchar");
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
