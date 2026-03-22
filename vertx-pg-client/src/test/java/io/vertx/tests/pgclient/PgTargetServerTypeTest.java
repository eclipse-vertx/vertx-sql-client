/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.vertx.tests.pgclient;

import io.vertx.core.Vertx;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.TargetServerType;
import io.vertx.pgclient.impl.PgPoolOptions;
import io.vertx.tests.pgclient.junit.ContainerPgRule;
import io.vertx.tests.sqlclient.ProxyServer;
import io.vertx.sqlclient.Pool;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicInteger;

@RunWith(VertxUnitRunner.class)
public class PgTargetServerTypeTest {

  @ClassRule
  public static ContainerPgRule rule = new ContainerPgRule();

  private Vertx vertx;
  private PgConnectOptions options;

  @Before
  public void setup() {
    vertx = Vertx.vertx();
    options = rule.options();
  }

  @After
  public void tearDown(TestContext ctx) {
    vertx.close().onComplete(ctx.asyncAssertSuccess());
  }

  private Pool createPool(TargetServerType targetServerType) {
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(targetServerType);
    poolOptions.setServers(Collections.singletonList(options));
    return PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(options)
      .using(vertx));
  }

  @Test
  public void testTargetAny(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(TargetServerType.ANY);
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testTargetPrimary(TestContext ctx) {
    // A single PG container is always a primary
    Async async = ctx.async();
    Pool pool = createPool(TargetServerType.PRIMARY);
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testTargetSecondaryFailsOnPrimary(TestContext ctx) {
    // A single PG container is a primary, so requesting SECONDARY should fail
    Async async = ctx.async();
    Pool pool = createPool(TargetServerType.SECONDARY);
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertTrue(err.getMessage().contains("Could not find a server of type SECONDARY"),
          "Expected error about SECONDARY, got: " + err.getMessage());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testTargetPreferSecondaryFallsToPrimary(TestContext ctx) {
    // PREFER_SECONDARY against a primary: the strict pass should connect (probing for
    // secondary), find PRIMARY, close, then fall back via connectToAny. This means at
    // least 2 TCP connections through the proxy: one probed and closed, one kept.
    Async async = ctx.async();
    AtomicInteger connectionCount = new AtomicInteger();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      connectionCount.incrementAndGet();
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v -> {
      PgConnectOptions proxyOpts = new PgConnectOptions(options)
        .setHost("localhost")
        .setPort(8080);
      PgPoolOptions poolOptions = new PgPoolOptions();
      poolOptions.setMaxSize(1);
      poolOptions.setTargetServerType(TargetServerType.PREFER_SECONDARY);
      poolOptions.setServers(Collections.singletonList(proxyOpts));
      Pool pool = PgBuilder.pool(b -> b
        .with(poolOptions)
        .connectingTo(proxyOpts)
        .using(vertx));
      pool.query("SELECT 1")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rs -> {
          ctx.assertEquals(1, rs.size());
          // At least 2: one from the strict pass (probed, found PRIMARY, closed)
          // and one from the connectToAny fallback (kept)
          ctx.assertTrue(connectionCount.get() >= 2,
            "Expected at least 2 connections (probe + fallback), got: " + connectionCount.get());
          pool.close().onComplete(ctx.asyncAssertSuccess(v2 -> async.complete()));
        }));
    }));
  }

  @Test
  public void testTargetPreferPrimary(TestContext ctx) {
    // PREFER_PRIMARY should connect to primary directly
    Async async = ctx.async();
    Pool pool = createPool(TargetServerType.PREFER_PRIMARY);
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testMultipleServersTargetPrimary(TestContext ctx) {
    // Use the same server twice to simulate multi-host.
    // connectingTo uses a refused address to prove the pool uses the servers list,
    // not the connectingTo address (ServerTypeAwarePgConnectionFactory ignores it).
    Async async = ctx.async();
    PgConnectOptions refused = new PgConnectOptions(options).setPort(1);
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(TargetServerType.PRIMARY);
    poolOptions.setServers(Arrays.asList(options, options));
    Pool pool = PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(refused)
      .using(vertx));
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testMultipleServersWithRefusedHost(TestContext ctx) {
    // First server refuses connection (wrong port), second is the real primary.
    // connectingTo uses a refused address to prove the pool uses the servers list.
    Async async = ctx.async();
    PgConnectOptions refused = new PgConnectOptions(options).setPort(1);
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(TargetServerType.PRIMARY);
    poolOptions.setServers(Arrays.asList(refused, options));
    Pool pool = PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(refused)
      .using(vertx));
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testPreferSecondaryFallsBackThroughRefusedHosts(TestContext ctx) {
    // PREFER_SECONDARY with [refused, primary, refused]:
    // Strict pass: refused (skip), primary (wrong type, closed), refused (skip) → fails
    // Fallback: refused (skip), primary → succeeds
    Async async = ctx.async();
    PgConnectOptions refused = new PgConnectOptions(options).setPort(1);
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(TargetServerType.PREFER_SECONDARY);
    poolOptions.setServers(Arrays.asList(refused, options, refused));
    Pool pool = PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(refused)
      .using(vertx));
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }

  @Test
  public void testStaleCacheDoesNotPreventProgress(TestContext ctx) {
    // Two entries for the same primary. After the first query, both are cached as PRIMARY.
    // On the second query, the strict pass (desired=SECONDARY) skips both via cache,
    // then retries with cache disabled, connects, finds PRIMARY, fails,
    // and falls back to connectToAny.
    Async async = ctx.async();
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(TargetServerType.PREFER_SECONDARY);
    poolOptions.setServers(Arrays.asList(options, options));
    PgConnectOptions refused = new PgConnectOptions(options).setPort(1);
    Pool pool = PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(refused)
      .using(vertx));
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs1 -> {
        // Both entries now cached as PRIMARY; second query exercises stale-cache retry
        pool.query("SELECT 1")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(rs2 -> {
            ctx.assertEquals(1, rs2.size());
            pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
          }));
      }));
  }

  @Test
  public void testStrictModeWithAllCachedWrongTypeRetries(TestContext ctx) {
    // Two entries for the same primary. Strict SECONDARY fails on first query (caches both
    // as PRIMARY). Second query skips both via cache, retries with cache disabled, connects,
    // finds PRIMARY, correctly fails with the same error.
    Async async = ctx.async();
    PgPoolOptions poolOptions = new PgPoolOptions();
    poolOptions.setMaxSize(1);
    poolOptions.setTargetServerType(TargetServerType.SECONDARY);
    poolOptions.setServers(Arrays.asList(options, options));
    PgConnectOptions refused = new PgConnectOptions(options).setPort(1);
    Pool pool = PgBuilder.pool(b -> b
      .with(poolOptions)
      .connectingTo(refused)
      .using(vertx));
    pool.query("SELECT 1")
      .execute()
      .onComplete(ctx.asyncAssertFailure(err1 -> {
        // Both cached as PRIMARY; second query exercises cache-disabled retry path
        pool.query("SELECT 1")
          .execute()
          .onComplete(ctx.asyncAssertFailure(err2 -> {
            ctx.assertTrue(err2.getMessage().contains("Could not find a server of type SECONDARY"),
              "Expected error about SECONDARY, got: " + err2.getMessage());
            pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
          }));
      }));
  }

  @Test
  public void testDetectServerTypeViaShowQuery(TestContext ctx) {
    // Verify that SHOW transaction_read_only returns 'off' for a primary
    Async async = ctx.async();
    Pool pool = createPool(TargetServerType.PRIMARY);
    pool.query("SHOW transaction_read_only")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(rs -> {
        ctx.assertEquals(1, rs.size());
        ctx.assertEquals("off", rs.iterator().next().getString(0));
        pool.close().onComplete(ctx.asyncAssertSuccess(v -> async.complete()));
      }));
  }
}
