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
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.Tuple;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@RunWith(VertxUnitRunner.class)
public class MSSQLMultipleTdsPacketsTest extends MSSQLTestBase {

  Vertx vertx;
  MSSQLConnection connection;

  @Before
  public void setup(TestContext ctx) {
    vertx = Vertx.vertx();
    options = new MSSQLConnectOptions(MSSQLTestBase.options);
    MSSQLConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> this.connection = conn));
  }

  @After
  public void tearDown(TestContext ctx) {
    if (connection != null) {
      connection.close().onComplete(ctx.asyncAssertSuccess());
    }
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testLargeBatch(TestContext ctx) {
    List<Tuple> batch = IntStream.range(0, 100)
      .mapToObj(i -> Tuple.of(UUID.randomUUID().toString()))
      .collect(toList());
    connection.query("TRUNCATE TABLE EntityWithIdentity")
      .execute().onComplete(ctx.asyncAssertSuccess(truncate -> {
        connection.preparedQuery("INSERT INTO EntityWithIdentity (name) OUTPUT INSERTED.id, INSERTED.name VALUES (@p1)")
          .executeBatch(batch).onComplete(ctx.asyncAssertSuccess(result -> {
            for (Tuple tuple : batch) {
              Row row = result.iterator().next();
              ctx.assertNotNull(row.getInteger("id"));
              ctx.assertEquals(tuple.getString(0), row.getString("name"));
              result = result.next();
            }
            ctx.assertNull(result);
          }));
      }));
  }
}
