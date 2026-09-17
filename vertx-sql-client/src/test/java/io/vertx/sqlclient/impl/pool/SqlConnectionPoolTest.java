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

package io.vertx.sqlclient.impl.pool;

import io.vertx.core.Context;
import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.Driver;
import org.junit.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.*;

public class SqlConnectionPoolTest {

  private static Connection fakeConnection() {
    return new Connection() {
      @Override
      public TracingPolicy tracingPolicy() {
        return TracingPolicy.PROPAGATE;
      }

      @Override
      public SocketAddress server() {
        return SocketAddress.inetSocketAddress(5432, "localhost");
      }

      @Override
      public String database() {
        return "";
      }

      @Override
      public String user() {
        return "";
      }

      @Override
      public ClientMetrics metrics() {
        return null;
      }

      @Override
      public void init(Holder holder) {
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
      public int pipeliningLimit() {
        return 1;
      }

      @Override
      public DatabaseMetadata getDatabaseMetaData() {
        throw new UnsupportedOperationException();
      }

      @Override
      public void close(Holder holder, Promise<Void> promise) {
        promise.complete();
      }

      @Override
      public int getProcessId() {
        return 0;
      }

      @Override
      public int getSecretKey() {
        return 0;
      }

      @Override
      public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
        return context.failedFuture(new UnsupportedOperationException());
      }
    };
  }

  private static final Driver FAKE_DRIVER = new Driver() {
    @Override
    public SqlConnectOptions parseConnectionUri(String uri) {
      throw new UnsupportedOperationException();
    }

    @Override
    public boolean acceptsOptions(SqlConnectOptions connectOptions) {
      return true;
    }

    @Override
    public ConnectionFactory createConnectionFactory(Vertx vertx, SqlConnectOptions database) {
      throw new UnsupportedOperationException();
    }

    @Override
    public ConnectionFactory createConnectionFactory(Vertx vertx, java.util.function.Supplier<? extends Future<? extends SqlConnectOptions>> database) {
      throw new UnsupportedOperationException();
    }

    @Override
    public io.vertx.sqlclient.Pool newPool(Vertx vertx, java.util.function.Supplier<? extends Future<? extends SqlConnectOptions>> databases, io.vertx.sqlclient.PoolOptions options, io.vertx.core.impl.CloseFuture closeFuture) {
      throw new UnsupportedOperationException();
    }
  };

  @Test
  public void testAfterAcquireFailureReleasesConnection() throws Exception {
    AtomicReference<Boolean> shouldFail = new AtomicReference<>(true);
    RuntimeException hookError = new RuntimeException("afterAcquire failed");

    VertxInternal vertx = (VertxInternal) Vertx.vertx();

    try {
      java.util.function.Function<Context, Future<SqlConnection>> connectionProvider = ctx -> {
        ContextInternal ctxInternal = (ContextInternal) ctx;
        Connection conn = fakeConnection();
        SqlConnectionBase sqlConn = new SqlConnectionBase(ctxInternal, null, conn, FAKE_DRIVER) {
        };
        return Future.succeededFuture(sqlConn);
      };

      SqlConnectionPool pool = new SqlConnectionPool(
        connectionProvider,
        () -> null,
        null,
        conn -> {
          if (shouldFail.get()) {
            return Future.failedFuture(hookError);
          }
          return Future.succeededFuture();
        },
        conn -> Future.succeededFuture(),
        vertx,
        0,
        0,
        1,
        false,
        -1,
        0
      );

      ContextInternal ctx = vertx.getOrCreateContext();

      // First attempt: afterAcquire fails
      CountDownLatch latch1 = new CountDownLatch(1);
      AtomicReference<Throwable> failure = new AtomicReference<>();
      ctx.runOnContext(v -> {
        Promise<SqlConnectionPool.PooledConnection> promise = ctx.promise();
        pool.acquire(ctx, 0, promise);
        promise.future().onComplete(ar -> {
          if (ar.failed()) {
            failure.set(ar.cause());
          }
          latch1.countDown();
        });
      });
      assertTrue(latch1.await(5, TimeUnit.SECONDS));
      assertEquals(hookError, failure.get());

      // Second attempt: afterAcquire succeeds, proving the lease was recycled
      shouldFail.set(false);
      CountDownLatch latch2 = new CountDownLatch(1);
      AtomicReference<SqlConnectionPool.PooledConnection> acquired = new AtomicReference<>();
      ctx.runOnContext(v -> {
        Promise<SqlConnectionPool.PooledConnection> promise = ctx.promise();
        pool.acquire(ctx, 0, promise);
        promise.future().onComplete(ar -> {
          if (ar.succeeded()) {
            acquired.set(ar.result());
          }
          latch2.countDown();
        });
      });
      assertTrue(latch2.await(5, TimeUnit.SECONDS));
      assertNotNull(acquired.get());

      pool.close();
    } finally {
      vertx.close();
    }
  }
}
