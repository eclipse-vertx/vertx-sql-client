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

package io.vertx.oracleclient.test;

import io.vertx.core.buffer.Buffer;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracleclient.OracleBuilder;
import io.vertx.oracleclient.data.Blob;
import io.vertx.oracleclient.test.junit.OracleRule;
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
import java.util.function.Function;

@RunWith(VertxUnitRunner.class)
public class OracleBinaryDataTypesTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp() throws Exception {
    pool = OracleBuilder.pool(builder -> builder.connectingTo(oracle.options()).using(vertx));
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testEncodeRaw(TestContext ctx) {
    testEncode(ctx, "test_raw", Buffer.buffer("See you space cowboy..."), Buffer::copy);
  }

  @Test
  public void testEncodeBlob(TestContext ctx) {
    testEncode(ctx, "test_blob", Buffer.buffer("See you space cowboy..."), Blob::copy);
  }

  private void testEncode(TestContext ctx, String columnName, Buffer expected, Function<Buffer, Object> input) {
    pool
      .preparedQuery("UPDATE binary_data_types SET " + columnName + " = ? WHERE id = 2")
      .execute(Tuple.of(input.apply(expected)))
      .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        pool
          .preparedQuery("SELECT " + columnName + " FROM binary_data_types WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(1, row.size());
            ctx.assertEquals(expected, row.get(Buffer.class, 0));
            ctx.assertEquals(expected, row.get(Buffer.class, columnName));
          }));
      }));
  }

  @Test
  public void testDecodeRaw(TestContext ctx) {
    testDecode(ctx, "test_raw", JDBCType.VARBINARY, Buffer.buffer("See you space cowboy..."));
  }

  @Test
  public void testDecodeBlob(TestContext ctx) {
    testDecode(ctx, "test_blob", JDBCType.BLOB, Buffer.buffer("See you space cowboy..."));
  }

  private <T> void testDecode(TestContext ctx, String columnName, JDBCType jdbcType, Buffer expected) {
    pool
      .preparedQuery("SELECT " + columnName + " FROM binary_data_types WHERE id = 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(expected, row.get(Buffer.class, 0));
        ctx.assertEquals(expected, row.get(Buffer.class, columnName));
        ColumnDescriptor columnDescriptor = result.columnDescriptors().get(0);
        ctx.assertEquals(jdbcType, columnDescriptor.jdbcType());
        ctx.assertNotNull(columnDescriptor);
      }));
  }

  @Test
  public void testEncodeNull(TestContext ctx) {
    pool
      .preparedQuery("UPDATE binary_data_types SET test_raw = ?, test_blob = ? WHERE id = 2")
      .execute(Tuple.tuple().addValue(null).addValue(null))
      .onComplete(ctx.asyncAssertSuccess(updateResult -> {
        pool
          .preparedQuery("SELECT * FROM binary_data_types WHERE id = 2")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(result -> {
            ctx.assertEquals(1, result.size());
            Row row = result.iterator().next();
            ctx.assertEquals(3, row.size());
            ctx.assertEquals(2, row.getInteger(0));
            for (int i = 1; i < 2; i++) {
              ctx.assertNull(row.getValue(i));
            }
          }));
      }));
  }

  @Test
  public void testDecodeNull(TestContext ctx) {
    pool
      .preparedQuery("SELECT test_raw, test_blob FROM binary_data_types WHERE id = 3")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(1, result.size());
        Row row = result.iterator().next();
        ctx.assertEquals(2, row.size());
        for (int i = 0; i < 2; i++) {
          ctx.assertNull(row.getValue(i));
        }
      }));
  }
}
