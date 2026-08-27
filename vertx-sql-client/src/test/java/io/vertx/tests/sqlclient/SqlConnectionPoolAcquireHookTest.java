/*
 * Copyright (c) 2011-2026 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.tests.sqlclient;

import io.vertx.core.Completable;
import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.pool.SqlConnectionPool;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.connection.ConnectionContext;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * When the {@code afterAcquire} hook fails, the acquisition must fail with the hook failure and
 * the lease must be returned to the pool, otherwise the slot leaks and the pool eventually starves.
 */
@RunWith(VertxUnitRunner.class)
public class SqlConnectionPoolAcquireHookTest {

  static class StubConnection implements Connection {

    @Override
    public TracingPolicy tracingPolicy() {
      return TracingPolicy.IGNORE;
    }

    @Override
    public ClientMetrics metrics() {
      return null;
    }

    @Override
    public SocketAddress server() {
      return SocketAddress.inetSocketAddress(5432, "localhost");
    }

    @Override
    public String database() {
      return "db";
    }

    @Override
    public String user() {
      return "user";
    }

    @Override
    public void init(ConnectionContext context) {
    }

    @Override
    public void close(ConnectionContext holder, Completable<Void> promise) {
      promise.succeed();
    }

    @Override
    public boolean isSsl() {
      return false;
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public DatabaseMetadata databaseMetadata() {
      return null;
    }

    @Override
    public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
      handler.fail("not supported");
    }
  }

  private Vertx vertx;
  private SqlConnectionPool pool;
  private final AtomicBoolean hookFails = new AtomicBoolean(true);
  private final Exception hookFailure = new Exception("hook failure");

  @Before
  public void setUp() {
    vertx = Vertx.vertx();
    ConnectionFactory<SqlConnectOptions> factory = new ConnectionFactory<SqlConnectOptions>() {
      @Override
      public Future<Connection> connect(Context context, SqlConnectOptions options) {
        return Future.succeededFuture(new StubConnection());
      }
      @Override
      public void close(Completable<Void> completion) {
        completion.succeed();
      }
    };
    pool = new SqlConnectionPool(
      () -> Future.succeededFuture(new SqlConnectOptions()),
      factory,
      null,
      null,
      conn -> hookFails.get() ? Future.failedFuture(hookFailure) : Future.succeededFuture(),
      conn -> Future.succeededFuture(),
      (VertxInternal) vertx,
      0,
      0,
      1,
      false,
      -1,
      0);
  }

  @After
  public void tearDown(TestContext ctx) {
    pool.close().onComplete(ctx.asyncAssertSuccess(v -> {
      vertx.close().onComplete(ctx.asyncAssertSuccess());
    }));
  }

  @Test
  public void testAcquireFailsWithHookFailureAndReleasesTheSlot(TestContext ctx) {
    Async async = ctx.async();
    ContextInternal context = (ContextInternal) vertx.getOrCreateContext();
    pool.acquire(context, 0, (conn, err) -> {
      // the acquisition must report the hook failure, not null
      ctx.assertEquals(hookFailure, err);
      hookFails.set(false);
      // the pool has a single slot: this acquisition succeeds only if the failed one was released
      pool.acquire(context, 0, (conn2, err2) -> {
        ctx.assertNull(err2);
        ctx.assertNotNull(conn2);
        async.complete();
      });
    });
  }
}
