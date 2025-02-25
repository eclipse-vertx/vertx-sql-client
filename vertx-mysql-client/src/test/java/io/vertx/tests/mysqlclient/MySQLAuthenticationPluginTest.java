/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.mysqlclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.mysqlclient.MySQLAuthenticationPlugin;
import io.vertx.mysqlclient.MySQLConnectOptions;
import io.vertx.mysqlclient.MySQLConnection;
import io.vertx.sqlclient.Row;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class MySQLAuthenticationPluginTest extends MySQLTestBase {
  Vertx vertx;
  MySQLConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = new MySQLConnectOptions(MySQLTestBase.options);
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testDefault(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setAuthenticationPlugin(MySQLAuthenticationPlugin.DEFAULT);
    verifyConnection(ctx, connectOptions);
  }

  @Test
  public void testNative41Plugin(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setAuthenticationPlugin(MySQLAuthenticationPlugin.MYSQL_NATIVE_PASSWORD);
    verifyConnection(ctx, connectOptions);
  }

  @Test
  public void testCleartextPasswordPlugin(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setAuthenticationPlugin(MySQLAuthenticationPlugin.MYSQL_CLEAR_PASSWORD);
    verifyConnection(ctx, connectOptions);
  }

  @Test
  public void testSha256Plugin(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setAuthenticationPlugin(MySQLAuthenticationPlugin.SHA256_PASSWORD);
    verifyConnection(ctx, connectOptions);
  }

  @Test
  public void testCachingSha2Plugin(TestContext ctx) {
    MySQLConnectOptions connectOptions = options.setAuthenticationPlugin(MySQLAuthenticationPlugin.CACHING_SHA2_PASSWORD);
    verifyConnection(ctx, connectOptions);
  }

  private void verifyConnection(TestContext ctx, MySQLConnectOptions connectOptions) {
    MySQLConnection.connect(vertx, connectOptions).onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT 1;")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(res -> {
        Row row = res.iterator().next();
        ctx.assertEquals(1, row.size());
        ctx.assertEquals(1, row.getInteger(0));
        conn.close();
      }));
    }));
  }
}
