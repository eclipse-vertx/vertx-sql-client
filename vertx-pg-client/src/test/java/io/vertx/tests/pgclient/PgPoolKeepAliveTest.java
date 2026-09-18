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

package io.vertx.tests.pgclient;

import io.vertx.core.Handler;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.sqlclient.Pool;
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.Row;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Tests the pool idle keep-alive probe.
 *
 * <p>The keep-alive probe verifies idle pooled connections are still usable by issuing a lightweight
 * configured statement (e.g. {@code SELECT 1}) before handing a connection to a caller.
 */
public class PgPoolKeepAliveTest extends PgPoolTestBase {

  @Override
  protected Pool createPool(PgConnectOptions connectOptions, PoolOptions poolOptions, Handler<SqlConnection> connectHandler) {
    return PgBuilder.pool(b -> b
      .connectingTo(connectOptions)
      .with(poolOptions)
      .using(vertx)
      .withConnectHandler(connectHandler));
  }

  private Pool createPool(int keepAliveMillis) {
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(1)
      .setIdleKeepAlive(keepAliveMillis)
      .setIdleKeepAliveUnit(TimeUnit.MILLISECONDS)
      .setPoolCleanerPeriod(20);
    return createPool(options, poolOptions);
  }

  /**
   * A healthy idle connection is kept alive: the keep-alive probe succeeds and the same connection is
   * reused (backend PID unchanged) even though it has been idle past the keep-alive threshold. This
   * confirms the probe issues {@code SELECT 1} on idle connections without disrupting them.
   */
  @Test
  public void testKeepsHealthyIdleConnection(TestContext ctx) {
    Pool pool = createPool(50);
    String sql = "SELECT pg_backend_pid() AS pid";
    Async async = ctx.async();

    pool
      .query(sql)
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs1 -> {
        Row row1 = rs1.iterator().next();
        int pid1 = row1.getInteger("pid");
        vertx.setTimer(200, l ->
          pool
            .query(sql)
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rs2 -> {
              int pid2 = rs2.iterator().next().getInteger("pid");
              // Same backend: the keep-alive probe verified the connection is healthy and reused it.
              ctx.assertEquals(pid1, pid2);
              pool.close();
              async.complete();
            })));
      }));

    async.awaitSuccess();
  }

  /**
   * After the pooled connection has been silently dropped (as by a load balancer) while idle, a
   * subsequent user query transparently succeeds on a fresh backend: the keep-alive probe detects the
   * stale connection, the pool closes and removes it, and the command is retried on a new connection.
   */
  @Test
  public void testRecoversFromDroppedIdleConnection(TestContext ctx) {
    // Route the pool through a TCP proxy so the connection can be dropped cleanly at the network
    // level, simulating an intermediate load balancer silently closing an idle connection.
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> listenLatch.complete()));
    listenLatch.awaitSuccess(20_000);

    options.setPort(8080);
    options.setHost("localhost");
    Pool pool = createPool(50);
    String sql = "SELECT pg_backend_pid() AS pid";
    Async async = ctx.async();

    pool
      .query(sql)
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs1 -> {
        int pid1 = rs1.iterator().next().getInteger("pid");

        // Drop the proxied connection's client socket while the connection is idle.
        vertx.setTimer(300, l -> {
          ProxyServer.Connection c = proxyConn.get();
          if (c != null) {
            c.clientSocket().close();
          }
          AtomicInteger attempts = new AtomicInteger();
          probeUntilFresh(ctx, pool, sql, pid1, attempts, async);
        });
      }));

    async.awaitSuccess();
  }

  private void probeUntilFresh(TestContext ctx, Pool pool, String sql, int oldPid, AtomicInteger attempts, Async async) {
    if (attempts.incrementAndGet() > 20) {
      ctx.fail("pool did not recover after backend termination");
      return;
    }
    pool
      .query(sql)
      .execute()
      .onComplete(ar -> {
        if (ar.succeeded()) {
          int pid = ar.result().iterator().next().getInteger("pid");
          if (pid != oldPid) {
            // The query succeeded on a fresh backend: the pool recovered transparently.
            pool.close();
            async.complete();
          } else {
            vertx.setTimer(50, l -> probeUntilFresh(ctx, pool, sql, oldPid, attempts, async));
          }
        } else {
          vertx.setTimer(50, l -> probeUntilFresh(ctx, pool, sql, oldPid, attempts, async));
        }
      });
  }
}
