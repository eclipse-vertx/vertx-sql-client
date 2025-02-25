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
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.desc.ColumnDescriptor;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.sql.JDBCType;
import java.time.OffsetDateTime;

import static java.time.temporal.ChronoUnit.MINUTES;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertEquals;

@RunWith(VertxUnitRunner.class)
public class OracleQueriesTest extends OracleTestBase {

  @ClassRule
  public static OracleRule oracle = OracleRule.SHARED_INSTANCE;

  Pool pool;

  @Before
  public void setUp() throws Exception {
    pool = OracleBuilder.pool(builder -> builder
      .connectingTo(oracle.options())
      .using(vertx));
  }

  @Test
  public void testCurrentTimestampType(TestContext ctx) {
    pool
      .query("SELECT CURRENT_TIMESTAMP FROM dual")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rows -> {
      ctx.verify(v -> {
        assertEquals(1, rows.size());
        Object value = rows.iterator().next().getValue(0);
        assertThat(value, is(instanceOf(OffsetDateTime.class)));
        assertEquals(0, MINUTES.between((OffsetDateTime) value, OffsetDateTime.now()));
        ColumnDescriptor descriptor = rows.columnDescriptors().get(0);
        ctx.assertEquals("TIMESTAMP WITH TIME ZONE", descriptor.typeName());
        ctx.assertEquals(JDBCType.TIMESTAMP_WITH_TIMEZONE, descriptor.jdbcType());
      });
    }));
  }

  @Test
  public void testInsertSelectQuery(TestContext ctx) {
    pool.query("TRUNCATE TABLE mutable").execute().otherwiseEmpty().compose(v -> pool.withConnection(conn -> {
      String sql = "INSERT INTO mutable (id, val) SELECT id, message FROM immutable WHERE id IN (?,?)";
      return conn.preparedQuery(sql).execute(Tuple.of(9, 7));
    })).onComplete(ctx.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext ctx) throws Exception {
    pool.close().onComplete(ctx.asyncAssertSuccess());
  }
}
