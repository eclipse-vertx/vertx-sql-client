/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package tests.oracleclient;

import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import tests.oracleclient.junit.OracleRule;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.JDBCType;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

@RunWith(VertxUnitRunner.class)
public class OracleTemporalDataTypesTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp() throws Exception {
    pool = OracleBuilder.pool(builder -> builder
      .connectingTo(oracle.options())
      .using(vertx));
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testEncodeDate(TestContext ctx) {
    testEncode(ctx, "test_date", LocalDateTime.class, LocalDateTime.of(2019, 11, 4, 0, 0));
  }

  @Test
  public void testEncodeTimestamp(TestContext ctx) {
    testEncode(ctx, "test_timestamp", LocalDateTime.class, LocalDateTime.of(2018, 11, 4, 15, 13, 28));
  }

  @Test
  public void testEncodeTimestampWithTimezone(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.of(LocalDateTime.of(2019, 11, 4, 15, 13, 28), ZoneOffset.ofHoursMinutes(1, 2));
    testEncode(ctx, "test_timestamp_with_timezone", OffsetDateTime.class, expected);
  }

  private <T> void testEncode(TestContext ctx, String columnName, Class<T> clazz, T expected) {
    pool
      .preparedQuery("UPDATE temporal_data_types SET " + columnName + " = ? WHERE id = 2")
      .execute(Tuple.of(expected))
      .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        pool
          .preparedQuery("SELECT " + columnName + " FROM temporal_data_types WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(expected, row.get(clazz, 0));
            ctx.assertEquals(expected, row.get(clazz, columnName));
            ctx.assertEquals(expected, row.getValue(0));
            ctx.assertEquals(expected, row.getValue(columnName));
          }));
      }));
  }

  @Test
  public void testDecodeDate(TestContext ctx) {
    testDecode(ctx, "test_date", JDBCType.TIMESTAMP, LocalDateTime.class, LocalDateTime.of(2019, 11, 4, 0, 0));
  }

  @Test
  public void testDecodeTimestamp(TestContext ctx) {
    testDecode(ctx, "test_timestamp", JDBCType.TIMESTAMP, LocalDateTime.class, LocalDateTime.of(2018, 11, 4, 15, 13, 28));
  }

  @Test
  public void testDecodeTimestampWithTimezone(TestContext ctx) {
    OffsetDateTime expected = OffsetDateTime.of(LocalDateTime.of(2019, 11, 4, 15, 13, 28), ZoneOffset.ofHoursMinutes(1, 2));
    testDecode(ctx, "test_timestamp_with_timezone", JDBCType.TIMESTAMP_WITH_TIMEZONE, OffsetDateTime.class, expected);
  }

  private <T> void testDecode(TestContext ctx, String columnName, JDBCType jdbcType, Class<?> clazz, T expected) {
    pool
      .preparedQuery("SELECT " + columnName + " FROM temporal_data_types WHERE id = 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.get(clazz, 0));
        ctx.assertEquals(expected, row.get(clazz, columnName));
        ctx.assertEquals(expected, row.getValue(0));
        ctx.assertEquals(expected, row.getValue(columnName));
        ColumnDescriptor columnDescriptor = result.columnDescriptors().get(0);
        ctx.assertEquals(jdbcType, columnDescriptor.jdbcType());
        ctx.assertNotNull(columnDescriptor);
      }));
  }

  @Test
  public void testEncodeNull(TestContext ctx) {
    pool
      .preparedQuery("UPDATE temporal_data_types SET test_date = ?, test_timestamp = ?, test_timestamp_with_timezone = ? WHERE id = 2")
      .execute(Tuple.tuple().addValue(null).addValue(null).addValue(null))
      .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        pool
          .preparedQuery("SELECT * FROM temporal_data_types WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(4, row.size());
            ctx.assertEquals(2, row.getInteger(0));
            for (int i = 1; i < row.size(); i++) {
              ctx.assertNull(row.getValue(i));
            }
          }));
      }));
  }

  @Test
  public void testDecodeNull(TestContext ctx) {
    pool
      .preparedQuery("SELECT test_date, test_timestamp, test_timestamp_with_timezone FROM temporal_data_types WHERE id = 3")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(3, row.size());
        for (int i = 0; i < row.size(); i++) {
          ctx.assertNull(row.getValue(i));
        }
      }));
  }
}
