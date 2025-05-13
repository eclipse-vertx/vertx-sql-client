/*
 * Copyright (C) 2017 Julien Viet
 *
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
 *
 */

package io.vertx.tests.pgclient;

import io.netty.channel.EventLoop;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.VertxOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.core.internal.ContextInternal;
import io.vertx.pgclient.PgBuilder;
import io.vertx.pgclient.PgConnectOptions;
import io.vertx.pgclient.PgConnection;
import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.internal.SqlConnectionInternal;
import io.vertx.tests.sqlclient.ProxyServer;
import org.junit.Rule;
import org.junit.Test;
import org.testcontainers.shaded.com.trilead.ssh2.ConnectionInfo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

import java.time.OffsetDateTime;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  @Rule
  public RepeatRule rule = new RepeatRule();

  private Set<Pool> pools = new HashSet<>();

  @Override
  public void tearDown(TestContext ctx) {
    int size = pools.size();
    if (size > 0) {
      Async async = ctx.async(size);
      Set<Pool> pools = this.pools;
      this.pools = new HashSet<>();
      pools.forEach(pool -> {
        pool
          .close()
          .onComplete(ar -> {
          async.countDown();
        });
      });
      async.awaitSuccess(20_000);
    }
    super.tearDown(ctx);
  }

  @Override
  protected Pool createPool(PgConnectOptions connectOptions, PoolOptions poolOptions, Handler<SqlConnection> connectHandler) {
    Pool pool = PgBuilder.pool(b -> b
      .connectingTo(connectOptions)
      .with(poolOptions)
      .using(vertx)
      .withConnectHandler(connectHandler)
    );
    pools.add(pool);
    return pool;
  }

  @Test
  public void testClosePool(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.close().onComplete(ctx.asyncAssertSuccess(v1 -> {
        pool
          .close()
          .onComplete(v2 -> {
          async.complete();
        });
      }));
    }));
    async.await(4000000);
  }

  @Test
  public void testReconnectQueued(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      Pool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), 1);
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      }));
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertSuccess(conn -> {
        conn
          .query("SELECT id, randomnumber from WORLD")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testAuthFailure(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(new PgConnectOptions(options).setPassword("wrong"), 1);
    pool
      .query("SELECT id, randomnumber from WORLD")
      .execute()
      .onComplete(ctx.asyncAssertFailure(v2 -> {
      async.complete();
    }));
  }

  @Test
  public void testConnectionFailure(TestContext ctx) {
    Async async = ctx.async();
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    Pool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"),
      new PoolOptions()
        .setMaxSize(1)
        .setMaxWaitQueueSize(0)
    );
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertFailure(err -> {
      proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
        pool
          .getConnection()
          .onComplete(ctx.asyncAssertSuccess(conn -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testRunWithExisting(TestContext ctx) {
    Async async = ctx.async();
    vertx.runOnContext(v -> {
      try {
        PgBuilder.pool(b -> b.with(new PoolOptions()));
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(new PgConnectOptions(options), new PoolOptions());
    pool
      .query("SELECT id, randomnumber from WORLD")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(20_0000);
  }

  @Test
  public void testMaxWaitQueueSize(TestContext ctx) {
    Async async = ctx.async();
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(v -> {
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertFailure(err -> {
        v.close().onComplete(ctx.asyncAssertSuccess(vv -> {
          async.complete();
        }));
      }));
    }));
    async.await(4000000);
  }

  // This test check that when using pooled connections, the preparedQuery pool operation
  // will actually use the same connection for the prepare and the query commands
  @Test
  public void testConcurrentMultipleConnection(TestContext ctx) {
    Pool pool = createPool(new PgConnectOptions(this.options).setCachePreparedStatements(true), 2);
    int numRequests = 2;
    Async async = ctx.async(numRequests);
    for (int i = 0; i < numRequests; i++) {
      pool
        .preparedQuery("SELECT * FROM Fortune WHERE id=$1")
        .execute(Tuple.of(1))
        .onComplete(ctx.asyncAssertSuccess(results -> {
        ctx.assertEquals(1, results.size());
        Tuple row = results.iterator().next();
        ctx.assertEquals(1, row.getInteger(0));
        ctx.assertEquals("fortune: No such file or directory", row.getString(1));
        async.countDown();
      }));
    }
  }

  @Test
  public void testUseAvailableResources(TestContext ctx) {
    int poolSize = 10;
    Async async = ctx.async(poolSize + 1);
    Pool pool = PgBuilder.pool(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(poolSize)));
    AtomicReference<PgConnection> ctrlConnRef = new AtomicReference<>();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(ctrlConn -> {
      ctrlConnRef.set(ctrlConn);
      for (int i = 0; i < poolSize; i++) {
        vertx.setTimer(10 * (i + 1), l -> {
          pool
            .query("select pg_sleep(5)")
            .execute()
            .onComplete(ctx.asyncAssertSuccess(res -> async.countDown()));
        });
      }
      vertx.setTimer(10 * poolSize + 50, event -> {
        ctrlConn
          .query("select count(*) as cnt from pg_stat_activity where application_name like '%vertx%'")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(rows -> {
          Integer count = rows.iterator().next().getInteger("cnt");
          ctx.assertEquals(poolSize + 1, count);
          async.countDown();
        }));
      });
    }));
    try {
      async.await();
    } finally {
      PgConnection ctrlConn = ctrlConnRef.get();
      if (ctrlConn != null) {
        ctrlConn.close();
      }
      pool.close();
    }
  }

  @Test
  public void testEventLoopSize(TestContext ctx) {
    int num = VertxOptions.DEFAULT_EVENT_LOOP_POOL_SIZE;
    int size = num * 2;
    Pool pool = PgBuilder.pool(b -> b.with(new PoolOptions().setMaxSize(size).setEventLoopSize(2)).connectingTo(options));
    Set<EventLoop> eventLoops = Collections.synchronizedSet(new HashSet<>());
    Async async = ctx.async(size);
    for (int i = 0;i < size;i++) {
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertSuccess(conn -> {
        PgSocketConnection c = (PgSocketConnection) ((SqlConnectionInternal) conn).unwrap().unwrap();
        EventLoop eventLoop = ((ContextInternal) c.context()).nettyEventLoop();
        eventLoops.add(eventLoop);
        async.countDown();
      }));
    }
    try {
      async.await();
    } finally {
      pool.close();
    }
    ctx.assertEquals(2, eventLoops.size());
  }

  @Test
  public void testPipelining(TestContext ctx) {
    AtomicLong latency = new AtomicLong(0L);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    proxy.proxyHandler(conn -> {
      conn.clientHandler(buff -> {
        long delay = latency.get();
        if (delay == 0L) {
          conn.serverSocket().write(buff);
        } else {
          vertx.setTimer(delay, id -> {
            conn.serverSocket().write(buff);
          });
        }
      });
      conn.connect();
    });
    Async latch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(res -> latch.complete()));
    latch.awaitSuccess(20_000);
    options.setPort(8080);
    options.setHost("localhost");

    int num = 3;
    Async async = ctx.async(num);
    SqlClient pool = PgBuilder.client(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)));
    AtomicLong start = new AtomicLong();
    // Connect to the database
    pool
      .query("select 1")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(res1 -> {
      // We have a connection in the pool
      start.set(System.currentTimeMillis());
      latency.set(1000);
      for (int i = 0; i < num; i++) {
        pool
          .query("select 1")
          .execute()
          .onComplete(ctx.asyncAssertSuccess(res2 -> async.countDown()));
      }
    }));

    async.awaitSuccess(20_000);
    long elapsed = System.currentTimeMillis() - start.get();
    ctx.assertTrue(elapsed < 2000, "Was expecting pipelined latency " + elapsed + " < 2000");
  }

  @Test
  public void testCannotAcquireConnectionOnPipelinedPool(TestContext ctx) {
    Pool pool = (Pool) PgBuilder.client(b -> b.connectingTo(options).with(new PoolOptions().setMaxSize(1)));
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertFailure());
  }

  /*  @Test
  public void testPipeliningDistribution(TestContext ctx) {
    int num = 10;
    SqlClient pool = PgPool.client(options.setPipeliningLimit(512), new PoolOptions().setMaxSize(num));
    Async async = ctx.async(num);
    for (int i = 0;i < num;i++) {
      pool.query("select 1").execute(ctx.asyncAssertSuccess(res1 -> {
        async.countDown();
      }));
    }
    async.awaitSuccess(20_000);
    int s = ((PoolBase)pool).size();
    System.out.println("s = " + s);
    int count = 1000;
    Async async2 = ctx.async(num * count);
    for (int i = 0;i < count * num;i++) {
      pool.query("select 1").execute(ctx.asyncAssertSuccess(res1 -> {
        async2.countDown();
      }));
    }
    async2.awaitSuccess(20_000);
    ((PoolBase)pool).check(ctx.asyncAssertSuccess(list -> {
      System.out.println("list = " + list);
    }));
  }*/

  @Test
  public void testPoolIdleTimeout(TestContext ctx) {
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    int poolCleanerPeriod = 100;
    int idleTimeout = 3000;
    Async latch = ctx.async();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      long now = System.currentTimeMillis();
      conn.clientCloseHandler(v -> {
        long lifetime = System.currentTimeMillis() - now;
        int delta = 500;
        int lowerBound = idleTimeout - poolCleanerPeriod - delta;
        int upperBound = idleTimeout + poolCleanerPeriod + delta;
        ctx.assertTrue(lifetime >= lowerBound, "Was expecting connection to be closed in more than " + lowerBound + ": " + lifetime);
        ctx.assertTrue(lifetime <= upperBound, "Was expecting connection to be closed in less than " + upperBound + ": "+ lifetime);
        latch.complete();
      });
      conn.connect();
    });

    // Start proxy
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(res -> listenLatch.complete()));
    listenLatch.awaitSuccess(20_000);

    poolOptions
      .setPoolCleanerPeriod(poolCleanerPeriod)
      .setMaxLifetime(0)
      .setIdleTimeout(idleTimeout)
      .setIdleTimeoutUnit(TimeUnit.MILLISECONDS);
    options.setPort(8080);
    options.setHost("localhost");
    Pool pool = createPool(options, poolOptions);

    // Create a connection that remains in the pool
    pool
      .getConnection()
      .flatMap(SqlClient::close)
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPoolMaxLifetime(TestContext ctx) {
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    int poolCleanerPeriod = 100;
    int maxLifetime = 3000;
    Async latch = ctx.async();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      long now = System.currentTimeMillis();
      conn.clientCloseHandler(v -> {
        long lifetime = System.currentTimeMillis() - now;
        int delta = 500;
        int lowerBound = maxLifetime - poolCleanerPeriod - delta;
        int upperBound = maxLifetime + poolCleanerPeriod + delta;
        ctx.assertTrue(lifetime >= lowerBound, "Was expecting connection to be closed in more than " + lowerBound + ": " + lifetime);
        ctx.assertTrue(lifetime <= upperBound, "Was expecting connection to be closed in less than " + upperBound + ": "+ lifetime);
        latch.complete();
      });
      conn.connect();
    });

    // Start proxy
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(res -> listenLatch.complete()));
    listenLatch.awaitSuccess(20_000);

    poolOptions
      .setPoolCleanerPeriod(poolCleanerPeriod)
      .setIdleTimeout(0)
      .setMaxLifetime(maxLifetime)
      .setMaxLifetimeUnit(TimeUnit.MILLISECONDS);
    options.setPort(8080);
    options.setHost("localhost");
    Pool pool = createPool(options, poolOptions);

    // Create a connection that remains in the pool
    pool
      .getConnection()
      .flatMap(SqlClient::close)
      .onComplete(ctx.asyncAssertSuccess());
  }

  @Test
  public void testPoolConnectTimeout(TestContext ctx) {
    Async async = ctx.async(2);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    List<ProxyServer.Connection> connections = Collections.synchronizedList(new ArrayList<>());
    proxy.proxyHandler(conn -> {
      // Ignore connection
      connections.add(conn);
      async.countDown();
    });

    // Start proxy
    Async listenLatch = ctx.async();
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(res -> listenLatch.complete()));
    listenLatch.awaitSuccess(20_000);

    poolOptions
      .setConnectionTimeout(1)
      .setConnectionTimeoutUnit(TimeUnit.SECONDS);
    options.setPort(8080);
    options.setHost("localhost");
    Pool pool = createPool(options, poolOptions);

    // Create a connection that remains in the pool
    long now = System.currentTimeMillis();
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertFailure(err -> {
        ctx.assertTrue(System.currentTimeMillis() - now > 900);
        async.countDown();
      }));

    async.awaitSuccess(20_000);
    connections.forEach(conn -> conn.clientSocket().close());
  }

  @Test
  @Repeat(50)
  public void testNoConnectionLeaks(TestContext ctx) {
    Async killConnections = ctx.async();
    PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
      Collector<Row, ?, List<Boolean>> collector = mapping(row -> row.getBoolean(0), toList());
      String sql = "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE pid <> pg_backend_pid() AND datname = $1";
      PreparedQuery<SqlResult<List<Boolean>>> preparedQuery = conn.preparedQuery(sql).collecting(collector);
      Tuple params = Tuple.of(options.getDatabase());
      preparedQuery.execute(params).compose(cf -> conn.close()).onComplete(ctx.asyncAssertSuccess(v -> killConnections.complete()));
    }));
    killConnections.awaitSuccess();

    String sql = "SELECT pg_backend_pid() AS pid, (SELECT count(*) FROM pg_stat_activity WHERE application_name LIKE '%vertx%') AS cnt";

    int idleTimeout = 50;
    poolOptions
      .setMaxSize(1)
      .setIdleTimeout(idleTimeout)
      .setIdleTimeoutUnit(TimeUnit.MILLISECONDS)
      .setPoolCleanerPeriod(5);
    Pool pool = createPool(options, poolOptions);

    Async async = ctx.async();
    AtomicInteger pid = new AtomicInteger();
    vertx.getOrCreateContext().runOnContext(v -> {
      pool
        .query(sql)
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rs1 -> {
        Row row1 = rs1.iterator().next();
        pid.set(row1.getInteger("pid"));
        ctx.assertEquals(1, row1.getInteger("cnt"));
        vertx.setTimer(2 * idleTimeout, l -> {
          pool
            .query(sql)
            .execute()
            .onComplete(ctx.asyncAssertSuccess(rs2 -> {
            Row row2 = rs2.iterator().next();
            ctx.assertEquals(1, row2.getInteger("cnt"));
            ctx.assertNotEquals(pid.get(), row2.getInteger("pid"));
            async.complete();
          }));
        });
      }));
    });
    async.awaitSuccess();
  }

  @Test
  public void testConnectionHook1(TestContext ctx) {
    Async async = ctx.async(2);
    Handler<SqlConnection> hook = f -> {
      vertx.setTimer(1000, id -> {
        f.close().onComplete(ctx.asyncAssertSuccess(v -> async.countDown()));
      });
    };
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1), hook);
    pool
      .getConnection()
      .onComplete(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT id, randomnumber from WORLD")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(v2 -> {
        async.countDown();
      }));
    }));
  }

  @Test
  public void testConnectionHook2(TestContext ctx) {
    Async async = ctx.async(2);
    Handler<SqlConnection> hook = f -> {
      vertx.setTimer(1000, id -> {
        f.close().onComplete(ctx.asyncAssertSuccess(v -> async.countDown()));
      });
    };
    Pool pool = createPool(options, new PoolOptions().setMaxSize(1), hook);
    pool
      .query("SELECT id, randomnumber from WORLD")
      .execute()
      .onComplete(ctx.asyncAssertSuccess(v2 -> {
      async.countDown();
    }));
  }

  @Test
  public void testConnectionClosedInHook(TestContext ctx) {
    Async async = ctx.async(2);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      Handler<SqlConnection> hook = f -> {
        f.closeHandler(v -> {
          async.countDown();
        });
        proxyConn.get().close();
      };
      Pool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), new PoolOptions().setMaxSize(1), hook);
      pool
        .getConnection()
        .onComplete(ctx.asyncAssertFailure(conn -> {
        async.countDown();
      }));
    }));
  }

  @Test
  public void testConnectionJitter(TestContext ctx) {
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(1)
      .setMaxLifetime(3000)
      .setMaxLifetimeUnit(TimeUnit.MILLISECONDS)
      .setJitter(1)
      .setJitterUnit(TimeUnit.SECONDS)
      .setPoolCleanerPeriod(50);

    Pool pool = createPool(options, poolOptions);
    Async latch = ctx.async();

    List<Integer> pids = Collections.synchronizedList(new ArrayList<>());
    List<Long> times = Collections.synchronizedList(new ArrayList<>());
    AtomicInteger lastPid = new AtomicInteger(-1);
    AtomicLong timerId = new AtomicLong();

    Consumer<TestContext> checkPid = testCtx -> {
      pool.query("SELECT pg_backend_pid() as pid")
        .execute()
        .onComplete(testCtx.asyncAssertSuccess(rs -> {
          int currentPid = rs.iterator().next().getInteger("pid");
          if (lastPid.get() != currentPid) {
            pids.add(currentPid);
            times.add(System.currentTimeMillis());
            lastPid.set(currentPid);

            if (pids.size() == 3) {
              vertx.cancelTimer(timerId.get());
              long diff1to2 = times.get(1) - times.get(0);
              long diff2to3 = times.get(2) - times.get(1);

              // Verify time ranges
              int maxLifetime = 3000;
              int jitter = 1000;
              int buffer = 100;
              int lowerBound = maxLifetime - jitter + buffer;
              int upperBound = maxLifetime + jitter + buffer;

              ctx.assertTrue(diff1to2 >= lowerBound && diff1to2 <= upperBound,
                String.format("Time between PIDs %d->%d (%dms) should be between %dms and %dms",
                  pids.get(0), pids.get(1), diff1to2, lowerBound, upperBound));

              ctx.assertTrue(diff2to3 >= lowerBound && diff2to3 <= upperBound,
                String.format("Time between PIDs %d->%d (%dms) should be between %dms and %dms",
                  pids.get(1), pids.get(2), diff2to3, lowerBound, upperBound));

              pool.close().onComplete(ctx.asyncAssertSuccess(v -> latch.complete()));
            }
          }
        }));
    };

    timerId.set(vertx.setPeriodic(30, id -> checkPid.accept(ctx)));
    latch.awaitSuccess(20000);
  }

  @Test
  public void testConnectionCloseTimingParallel(TestContext ctx) {
    // Configure pool options.
    PoolOptions poolOptions = new PoolOptions()
      .setMaxSize(20)              // Allow parallel tasks.
      .setMaxLifetime(5000)        // Maximum lifetime = 5000 ms.
      .setMaxLifetimeUnit(TimeUnit.MILLISECONDS)
      .setJitter(1)                // Jitter = 1 second.
      .setJitterUnit(TimeUnit.SECONDS)
      .setPoolCleanerPeriod(50);

    Pool pool = createPool(options, poolOptions);
    Async latch = ctx.async();
    int totalConnections = 50;
    List<Future<JsonObject>> futures = new ArrayList<>();
    List<Integer> pids = Collections.synchronizedList(new ArrayList<>());
    ConcurrentMap<Integer, Long> startTimes = new ConcurrentHashMap<>();
    ConcurrentMap<Integer, Long> endTimes = new ConcurrentHashMap<>();

    // Launch connection tasks in parallel.
    for (int i = 0; i < totalConnections; i++) {
      futures.add(processSingleConnection(pool, i, ctx, pids, startTimes, endTimes));
    }

    Future.all(futures).onComplete(ctx.asyncAssertSuccess(ar -> {
      pool.close().onComplete(ctx.asyncAssertSuccess(v -> {
        // Wait 3 seconds after pool closure.
        vertx.setTimer(3000, timerId -> {
          PgConnection.connect(vertx, options).onComplete(ctx.asyncAssertSuccess(conn -> {
            // (Optional) Query to verify that none of our recorded PIDs are still active.
            String pidList = pids.stream().map(String::valueOf).collect(Collectors.joining(","));
            String sql = "SELECT pid FROM pg_stat_activity WHERE pid IN (" + pidList + ")";
            conn.query(sql).execute().onComplete(ctx.asyncAssertSuccess(rs2 -> {
              // Compute durations for each PID.
              List<Long> durations = pids.stream()
                .map(pid -> endTimes.get(pid) - startTimes.get(pid))
                .collect(Collectors.toList());
              long maxLifetime = 5000;
              long jitterMs = 1000;
              long bucketWidth = jitterMs / 5;
              // Bucket the durations based on an offset of maxLifetime.
              Map<Integer, List<Integer>> bucketMap = new HashMap<>();
              for (Integer pid : pids) {
                long duration = endTimes.get(pid) - startTimes.get(pid);
                int bucket = (int) ((duration - maxLifetime) / bucketWidth);
                bucketMap.computeIfAbsent(bucket, k -> new ArrayList<>()).add(pid);
              }

              ctx.assertTrue(bucketMap.size() >= 5, "Bucket Size should be 5");
              bucketMap.forEach((bucket, bucketPids) -> {
                ctx.assertTrue(!bucketPids.isEmpty(), "Bucket " + bucket + " should not be empty");
              });
              // Print one line per PID with its duration.
              for (Integer pid : pids) {
                long duration = endTimes.get(pid) - startTimes.get(pid);
              }
              conn.close();
              latch.complete();
            }));
          }));
        });
      }));
    }));
    latch.awaitSuccess(60000);
  }

  /**
   * Acquires a connection, retrieves its PID and backend_start time,
   * closes the connection, and polls for its closure.
   * Returns a Future with a JsonObject containing the PID, start time, and end time.
   */
  private Future<JsonObject> processSingleConnection(Pool pool, int index, TestContext ctx,
                                                     List<Integer> pids,
                                                     ConcurrentMap<Integer, Long> startTimes,
                                                     ConcurrentMap<Integer, Long> endTimes) {
    Promise<JsonObject> promise = Promise.promise();
    pool.getConnection().onComplete(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT pg_backend_pid() AS pid")
        .execute()
        .onComplete(ctx.asyncAssertSuccess(rs -> {
          int pid = rs.iterator().next().getInteger("pid");
          pids.add(pid);
          String sql = "SELECT backend_start FROM pg_stat_activity WHERE pid = " + pid;
          conn.query(sql).execute().onComplete(ctx.asyncAssertSuccess(rs2 -> {
            Row row = rs2.iterator().next();
            OffsetDateTime backendStart = row.getOffsetDateTime("backend_start");
            long startMillis = backendStart.toInstant().toEpochMilli();
            startTimes.put(pid, startMillis);
            pollForClose(pool, pid, closeTime -> {
              endTimes.put(pid, closeTime);
              JsonObject res = new JsonObject()
                .put("pid", pid)
                .put("start", startMillis)
                .put("end", closeTime);
              promise.complete(res);
            });
            conn.close().onComplete(x -> {});
          }));
        }));
    }));
    return promise.future();
  }

  /**
   * Polls pg_stat_activity periodically for the given PID.
   * Once the PID is no longer found (i.e. the connection is closed),
   * returns the current system time via the resultHandler.
   */
  private void pollForClose(Pool pool, int pid, Handler<Long> resultHandler) {
    long timerId = vertx.setPeriodic(50, id -> {
      pool.query("SELECT 1 FROM pg_stat_activity WHERE pid = " + pid)
        .execute()
        .onComplete(ar -> {
          if (ar.succeeded() && !ar.result().iterator().hasNext()) {
            long closeTime = System.currentTimeMillis();
            vertx.cancelTimer(id);
            resultHandler.handle(closeTime);
          }
        });
    });
  }
}
