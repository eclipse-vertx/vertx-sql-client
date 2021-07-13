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
package io.vertx.oracle.test.tck;

import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.oracle.test.junit.OracleRule;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.tck.ConnectionTestBase;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class OracleConnectionTest extends ConnectionTestBase {
  @ClassRule
  public static OracleRule rule = OracleRule.SHARED_INSTANCE;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    options = rule.options();
    connector = ClientConfig.CONNECT.connect(vertx, options);
  }

  @Override
  public void tearDown(TestContext ctx) {
    connector.close();
    super.tearDown(ctx);
  }

  @Test
  public void testConnect(TestContext ctx) {
    Async async = ctx.async();
    connect(ctx.asyncAssertSuccess(conn -> async.complete()));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    Async async = ctx.async();
    options.setDatabase("invalidDatabase");
    connect(ctx.asyncAssertFailure(err -> {
      ctx.assertTrue(err.getMessage().contains("ORA-12514"));
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    Async async = ctx.async();
    options.setPassword("invalidPassword");
    connect(ctx.asyncAssertFailure(err -> {
      ctx.assertTrue(err.getMessage().contains("ORA-01017"));
      async.complete();
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    Async async = ctx.async();
    options.setUser("invalidUsername");
    connect(ctx.asyncAssertFailure(err -> {
      ctx.assertTrue(err.getMessage().contains("ORA-01017"));
      async.complete();
    }));
  }

  @Test
  public void testClose(TestContext ctx) {
    Async closedAsync = ctx.async();
    Async closeAsync = ctx.async();
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.closeHandler(v -> {
        closedAsync.complete();
      });
      conn.close(ctx.asyncAssertSuccess(v -> closeAsync.complete()));
    }));
    closedAsync.await();
  }

  @Test
  public void testCloseWithErrorInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(err -> async.countDown()));
      conn.closeHandler(v -> async.countDown());
      conn.close();
    }));
    async.await();
  }

  @Test
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, message from immutable").execute(ctx.asyncAssertFailure(result -> {
        ctx.assertEquals(2, async.count());
        async.countDown();
      }));
      conn.closeHandler(v -> {
        ctx.assertEquals(1, async.count());
        async.countDown();
      });
      conn.close();
    }));
    async.await();
  }

  @Test
  public void testDatabaseMetaData(TestContext ctx) {
    connect(ctx.asyncAssertSuccess(conn -> {
      DatabaseMetadata md = conn.databaseMetadata();
      ctx.assertNotNull(md, "DatabaseMetadata should not be null");
      ctx.assertNotNull(md.productName(), "Database product name should not be null");
      ctx.assertNotNull(md.fullVersion(), "Database full version string should not be null");
      ctx.assertTrue(md.majorVersion() >= 1, "Expected DB major version to be >= 1 but was " + md.majorVersion());
      ctx.assertTrue(md.minorVersion() >= 0, "Expected DB minor version to be >= 0 but was " + md.minorVersion());
      validateDatabaseMetaData(ctx, md);
    }));
  }

  @Override
  protected void validateDatabaseMetaData(TestContext ctx, DatabaseMetadata md) {
    ctx.assertTrue(md.fullVersion().contains("Oracle"));
    ctx.assertTrue(md.productName().contains("Oracle"));
  }
}
