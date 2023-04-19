/*
 * Copyright (c) 2011-2023 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.tck;

import io.vertx.core.*;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

import static java.util.concurrent.TimeUnit.SECONDS;

public abstract class ConnectionTestBase {
  protected Vertx vertx;
  protected Connector<SqlConnection> connector;

  protected SqlConnectOptions options;

  protected void connect(Handler<AsyncResult<SqlConnection>> handler) {
    connector.connect(handler);
  }

  @Before
  public void setUp() throws Exception {
    vertx = Vertx.vertx();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnect(TestContext ctx) {
    connect(ctx.asyncAssertSuccess());
  }

  @Test
  public void testConnectNoLeak(TestContext ctx) throws Exception {
    Set<SqlConnectionBase<?>> connections = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    Set<ConnectionFactory> factories = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    Async async = ctx.async(100);
    for (int i = 0; i < 100; i++) {
      connect(ctx.asyncAssertSuccess(conn -> {
        SqlConnectionBase<?> base = (SqlConnectionBase<?>) conn;
        connections.add(base);
        factories.add(base.factory());
        conn.close().onComplete(ctx.asyncAssertSuccess(v -> async.countDown()));
      }));
    }
    async.awaitSuccess();
    for (int c = 0; c < 5; c++) {
      System.gc();
      SECONDS.sleep(1);
    }
    ctx.assertEquals(0, connections.size());
    ctx.assertEquals(0, factories.size());
  }

  @Test
  public void testConnectNoLeakInVerticle(TestContext ctx) throws Exception {
    Set<SqlConnectionBase<?>> connections = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    Set<ConnectionFactory> factories = Collections.synchronizedSet(Collections.newSetFromMap(new WeakHashMap<>()));
    Async async = ctx.async(100);
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start() throws Exception {
        for (int i = 0; i < 100; i++) {
          connect(ctx.asyncAssertSuccess(conn -> {
            SqlConnectionBase<?> base = (SqlConnectionBase<?>) conn;
            connections.add(base);
            factories.add(base.factory());
            conn.close().onComplete(ctx.asyncAssertSuccess(v -> async.countDown()));
          }));
        }
      }
    });
    async.awaitSuccess();
    for (int c = 0; c < 5; c++) {
      System.gc();
      SECONDS.sleep(1);
    }
    ctx.assertEquals(0, connections.size());
    ctx.assertEquals(0, factories.size());
  }

  @Test
  public void testCloseOnUndeploy(TestContext ctx) {
    Async done = ctx.async();
    vertx.deployVerticle(new AbstractVerticle() {
      @Override
      public void start(Promise<Void> startPromise) throws Exception {
        connect(ctx.asyncAssertSuccess(conn -> {
          conn.closeHandler(v -> {
            done.complete();
          });
          startPromise.complete();
        }));
      }
    }).onComplete(ctx.asyncAssertSuccess(id -> {
      vertx.undeploy(id);
    }));
  }

  @Test
  public void testConnectInvalidDatabase(TestContext ctx) {
    options.setDatabase("invalidDatabase");
    connect(ctx.asyncAssertFailure(err -> {
    }));
  }

  @Test
  public void testConnectInvalidPassword(TestContext ctx) {
    options.setPassword("invalidPassword");
    connect(ctx.asyncAssertFailure(err -> {
    }));
  }

  @Test
  public void testConnectInvalidUsername(TestContext ctx) {
    options.setUser("invalidUsername");
    connect(ctx.asyncAssertFailure(err -> {
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
      conn.query("SELECT whatever from DOES_NOT_EXIST").execute(ctx.asyncAssertFailure(err -> {
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
  public void testCloseWithQueryInProgress(TestContext ctx) {
    Async async = ctx.async(2);
    connect(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, message from immutable").execute(ctx.asyncAssertSuccess(result -> {
        ctx.assertEquals(2, async.count());
        ctx.assertEquals(12, result.size());
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

  protected abstract void validateDatabaseMetaData(TestContext ctx, DatabaseMetadata md);

}
