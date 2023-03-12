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

package io.vertx.sqlclient.tck;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.Tuple;
import io.vertx.sqlclient.data.NullValue;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public abstract class NullValueEncodeTestBase {

  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  protected abstract void initConnector();

  @Before
  public void setup(TestContext ctx) throws Exception {
    vertx = Vertx.vertx();
    initConnector();
  }

  @After
  public void tearDown(TestContext ctx) {
    connector.close();
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testEncodeNullBoolean(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Boolean);
  }

  @Test
  public void testEncodeNullShort(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Short);
  }

  @Test
  public void testEncodeNullInteger(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Integer);
  }

  @Test
  public void testEncodeNullLong(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Long);
  }

  @Test
  public void testEncodeNullFloat(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Float);
  }

  @Test
  public void testEncodeNullDouble(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Double);
  }

  @Test
  public void testEncodeNullString(TestContext ctx) {
    testEncodeNull(ctx, NullValue.String);
  }

  @Test
  public void testEncodeNullJsonObject(TestContext ctx) {
    testEncodeNull(ctx, NullValue.JsonObject);
  }

  @Test
  public void testEncodeNullJsonArray(TestContext ctx) {
    testEncodeNull(ctx, NullValue.JsonArray);
  }

  @Test
  public void testEncodeNullTemporal(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Temporal);
  }

  @Test
  public void testEncodeNullLocalDate(TestContext ctx) {
    testEncodeNull(ctx, NullValue.LocalDate);
  }

  @Test
  public void testEncodeNullLocalTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.LocalTime);
  }

  @Test
  public void testEncodeNullLocalDateTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.LocalDateTime);
  }

  @Test
  public void testEncodeNullOffsetTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.OffsetTime);
  }

  @Test
  public void testEncodeNullOffsetDateTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.OffsetDateTime);
  }

  @Test
  public void testEncodeNullBuffer(TestContext ctx) {
    testEncodeNull(ctx, NullValue.Buffer);
  }

  @Test
  public void testEncodeNullUUID(TestContext ctx) {
    testEncodeNull(ctx, NullValue.UUID);
  }

  @Test
  public void testEncodeNullBigDecimal(TestContext ctx) {
    testEncodeNull(ctx, NullValue.BigDecimal);
  }

  @Test
  public void testEncodeNullArrayOfBoolean(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfBoolean);
  }

  @Test
  public void testEncodeNullArrayOfShort(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfShort);
  }

  @Test
  public void testEncodeNullArrayOfInteger(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfInteger);
  }

  @Test
  public void testEncodeNullArrayOfLong(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfLong);
  }

  @Test
  public void testEncodeNullArrayOfFloat(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfFloat);
  }

  @Test
  public void testEncodeNullArrayOfDouble(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfDouble);
  }

  @Test
  public void testEncodeNullArrayOfString(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfString);
  }

  @Test
  public void testEncodeNullArrayOfJsonObject(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfJsonObject);
  }

  @Test
  public void testEncodeNullArrayOfJsonArray(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfJsonArray);
  }

  @Test
  public void testEncodeNullArrayOfTemporal(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfTemporal);
  }

  @Test
  public void testEncodeNullArrayOfLocalDate(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfLocalDate);
  }

  @Test
  public void testEncodeNullArrayOfLocalTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfLocalTime);
  }

  @Test
  public void testEncodeNullArrayOfLocalDateTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfLocalDateTime);
  }

  @Test
  public void testEncodeNullArrayOfOffsetTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfOffsetTime);
  }

  @Test
  public void testEncodeNullArrayOfOffsetDateTime(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfOffsetDateTime);
  }

  @Test
  public void testEncodeNullArrayOfBuffer(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfBuffer);
  }

  @Test
  public void testEncodeNullArrayOfUUID(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfUUID);
  }

  @Test
  public void testEncodeNullArrayOfBigDecimal(TestContext ctx) {
    testEncodeNull(ctx, NullValue.ArrayOfBigDecimal);
  }

  private void testEncodeNull(TestContext ctx, NullValue nullValue) {
    connector.connect(ctx.asyncAssertSuccess(conn -> {
      Tuple.tuple();
      conn.preparedQuery(statement("SELECT ", "")).execute(Tuple.of(nullValue), ctx.asyncAssertSuccess(rs -> {
        Row row = rs.iterator().next();
        ctx.assertNull(row.getValue(0));
      }));
    }));
  }

  protected abstract String statement(String... parts);
}
