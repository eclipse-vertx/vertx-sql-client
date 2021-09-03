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

package io.vertx.mssqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.NullValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(VertxUnitRunner.class)
public class MSSQLQueriesTest extends MSSQLTestBase {

  @Rule
  public RepeatRule rule = new RepeatRule();

  Vertx vertx;
  MSSQLConnection connection;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
    MSSQLConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> this.connection = conn));
  }

  @After
  public void tearDown(TestContext ctx) {
    if (connection != null) {
      connection.close(ctx.asyncAssertSuccess());
    }
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testSimpleQueryOrderBy(TestContext ctx) {
    connection.query("SELECT message FROM immutable ORDER BY message DESC")
      .execute(ctx.asyncAssertSuccess(rs -> ctx.assertTrue(rs.size() > 1)));
  }

  @Test
  public void testPreparedQueryOrderBy(TestContext ctx) {
    connection.preparedQuery("SELECT message FROM immutable WHERE id BETWEEN @p1 AND @p2 ORDER BY message DESC")
      .execute(Tuple.of(4, 9), ctx.asyncAssertSuccess(rs -> ctx.assertEquals(6, rs.size())));
  }

  @Test
  @Repeat(50)
  public void testQueryCurrentTimestamp(TestContext ctx) {
    LocalDateTime start = LocalDateTime.now();
    connection.query("SELECT current_timestamp")
      .execute(ctx.asyncAssertSuccess(rs -> {
        Object value = rs.iterator().next().getValue(0);
        ctx.assertTrue(value instanceof LocalDateTime);
        LocalDateTime localDateTime = (LocalDateTime) value;
        ctx.assertTrue(Math.abs(localDateTime.until(start, ChronoUnit.SECONDS)) < 1);
      }));
  }

  @Test
  public void testCreateTable(TestContext ctx) {
    connection.query("drop table if exists Basic")
      .execute(ctx.asyncAssertSuccess(drop -> {
        connection.preparedQuery("create table Basic (id int, dessimal numeric(19,2), primary key (id))")
          .execute(ctx.asyncAssertSuccess(create -> {
            connection.preparedQuery("INSERT INTO Basic (id, dessimal) values (3, @p1)")
              .execute(Tuple.of(NullValue.BigDecimal), ctx.asyncAssertSuccess());
          }));
      }));
  }

  @Test
  public void testInsertReturning(TestContext ctx) {
    connection.preparedQuery("insert into EntityWithIdentity (name) OUTPUT INSERTED.id, INSERTED.name VALUES (@p1)")
      .execute(Tuple.of("John"), ctx.asyncAssertSuccess(result -> {
        Row row = result.iterator().next();
        ctx.assertNotNull(row.getInteger("id"));
        ctx.assertEquals("John", row.getString("name"));
      }));
  }

  @Test
  public void testQueryNonExisting(TestContext ctx) {
    connection.preparedQuery("DELETE FROM Fonky.Family")
      .execute(Tuple.tuple(), ctx.asyncAssertFailure(t -> {
        ctx.verify(unused -> {
          assertThat(t, is(instanceOf(MSSQLException.class)));
        });
        MSSQLException mssqlException = (MSSQLException) t;
        ctx.assertEquals(208, mssqlException.number());
      }));
  }

  @Test
  public void testMultiplePacketsDecoding(TestContext ctx) {
    // Ensure TdsPacketDecoder works well when a single Netty buffer encompasses several TDS Packets

    String sql = "" +
      "SELECT table_name  AS TABLE_NAME,\n" +
      "       column_name AS COLUMN_NAME,\n" +
      "       data_type   AS TYPE_NAME,\n" +
      "       NULL        AS COLUMN_SIZE,\n" +
      "       NULL        AS DECIMAL_DIGITS,\n" +
      "       is_nullable AS IS_NULLABLE,\n" +
      "       NULL        AS DATA_TYPE\n" +
      "FROM information_schema.columns\n" +
      "ORDER BY table_catalog, table_schema, table_name, column_name, ordinal_position";

    connection.preparedQuery(sql)
      .execute(Tuple.tuple(), ctx.asyncAssertSuccess(rows -> {
        ctx.assertTrue(rows.size() > 0);
      }));
  }
}
