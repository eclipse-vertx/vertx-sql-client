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

package io.vertx.pgclient;

import io.netty.channel.EventLoop;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.VertxOptions;
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.NoStackTraceThrowable;
import io.vertx.ext.unit.Async;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.Repeat;
import io.vertx.ext.unit.junit.RepeatRule;
import io.vertx.pgclient.impl.PgSocketConnection;
import io.vertx.pgclient.spi.PgDriver;
import io.vertx.sqlclient.*;
import io.vertx.sqlclient.impl.SqlConnectionInternal;
import io.vertx.sqlclient.spi.ConnectionFactory;
import org.junit.Rule;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collector;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class PgPoolTest extends PgPoolTestBase {

  @Rule
  public RepeatRule rule = new RepeatRule();

  private Set<PgPool> pools = new HashSet<>();

  @Override
  public void tearDown(TestContext ctx) {
    int size = pools.size();
    if (size > 0) {
      Async async = ctx.async(size);
      Set<PgPool> pools = this.pools;
      this.pools = new HashSet<>();
      pools.forEach(pool -> {
        pool.close(ar -> {
          async.countDown();
        });
      });
      async.awaitSuccess(20_000);
    }
    super.tearDown(ctx);
  }

  @Override
  protected PgPool createPool(PgConnectOptions connectOptions, PoolOptions poolOptions) {
    PgPool pool = PgPool.pool(vertx, connectOptions, poolOptions);
    pools.add(pool);
    return pool;
  }

  @Test
  public void testClosePool(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.close(ctx.asyncAssertSuccess(v1 -> {
        pool.close(v2 -> {
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
      PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), 1);
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        proxyConn.get().close();
      }));
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
        conn.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v2 -> {
          async.complete();
        }));
      }));
    }));
  }

  @Test
  public void testAuthFailure(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(options).setPassword("wrong"), 1);
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertFailure(v2 -> {
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
    PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"),
      new PoolOptions()
        .setMaxSize(1)
        .setMaxWaitQueueSize(0)
    );
    pool.getConnection(ctx.asyncAssertFailure(err -> {
      proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
        pool.getConnection(ctx.asyncAssertSuccess(conn -> {
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
        PgPool.pool(new PoolOptions());
        ctx.fail();
      } catch (IllegalStateException ignore) {
        async.complete();
      }
    });
  }

  @Test
  public void testRunStandalone(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(new PgConnectOptions(options), new PoolOptions());
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v -> {
      async.complete();
    }));
    async.await(20_0000);
  }

  @Test
  public void testMaxWaitQueueSize(TestContext ctx) {
    Async async = ctx.async();
    PgPool pool = createPool(options, new PoolOptions().setMaxSize(1).setMaxWaitQueueSize(0));
    pool.getConnection(ctx.asyncAssertSuccess(v -> {
      pool.getConnection(ctx.asyncAssertFailure(err -> {
        v.close(ctx.asyncAssertSuccess(vv -> {
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
    PgPool pool = createPool(new PgConnectOptions(this.options).setCachePreparedStatements(true), 2);
    int numRequests = 2;
    Async async = ctx.async(numRequests);
    for (int i = 0; i < numRequests; i++) {
      pool.preparedQuery("SELECT * FROM Fortune WHERE id=$1").execute(Tuple.of(1), ctx.asyncAssertSuccess(results -> {
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
    PgPool pool = PgPool.pool(options, new PoolOptions().setMaxSize(poolSize));
    AtomicReference<PgConnection> ctrlConnRef = new AtomicReference<>();
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(ctrlConn -> {
      ctrlConnRef.set(ctrlConn);
      for (int i = 0; i < poolSize; i++) {
        vertx.setTimer(10 * (i + 1), l -> {
          pool.query("select pg_sleep(5)").execute(ctx.asyncAssertSuccess(res -> async.countDown()));
        });
      }
      vertx.setTimer(10 * poolSize + 50, event -> {
        ctrlConn.query("select count(*) as cnt from pg_stat_activity where application_name like '%vertx%'").execute(ctx.asyncAssertSuccess(rows -> {
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
    PgPool pool = PgPool.pool(options, new PoolOptions().setMaxSize(size).setEventLoopSize(2));
    Set<EventLoop> eventLoops = Collections.synchronizedSet(new HashSet<>());
    Async async = ctx.async(size);
    for (int i = 0;i < size;i++) {
      pool.getConnection(ctx.asyncAssertSuccess(conn -> {
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
    SqlClient pool = PgPool.client(options, new PoolOptions().setMaxSize(1));
    AtomicLong start = new AtomicLong();
    // Connect to the database
    pool.query("select 1").execute(ctx.asyncAssertSuccess(res1 -> {
      // We have a connection in the pool
      start.set(System.currentTimeMillis());
      latency.set(1000);
      for (int i = 0; i < num; i++) {
        pool.query("select 1").execute(ctx.asyncAssertSuccess(res2 -> async.countDown()));
      }
    }));

    async.awaitSuccess(20_000);
    long elapsed = System.currentTimeMillis() - start.get();
    ctx.assertTrue(elapsed < 2000, "Was expecting pipelined latency " + elapsed + " < 2000");
  }

  @Test
  public void testCannotAcquireConnectionOnPipelinedPool(TestContext ctx) {
    PgPool pool = (PgPool) PgPool.client(options, new PoolOptions().setMaxSize(1));
    pool.getConnection(ctx.asyncAssertFailure());
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
    PgPool pool = createPool(options, poolOptions);

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
    PgPool pool = createPool(options, poolOptions);

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
    PgPool pool = createPool(options, poolOptions);

    // Create a connection that remains in the pool
    long now = System.currentTimeMillis();
    pool
      .getConnection(ctx.asyncAssertFailure(err -> {
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
    PgConnection.connect(vertx, options, ctx.asyncAssertSuccess(conn -> {
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
    PgPool pool = createPool(options, poolOptions);

    Async async = ctx.async();
    AtomicInteger pid = new AtomicInteger();
    vertx.getOrCreateContext().runOnContext(v -> {
      pool.query(sql).execute(ctx.asyncAssertSuccess(rs1 -> {
        Row row1 = rs1.iterator().next();
        pid.set(row1.getInteger("pid"));
        ctx.assertEquals(1, row1.getInteger("cnt"));
        vertx.setTimer(2 * idleTimeout, l -> {
          pool.query(sql).execute(ctx.asyncAssertSuccess(rs2 -> {
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
    PgPool pool = createPool(options, new PoolOptions().setMaxSize(1)).connectHandler(hook);
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v2 -> {
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
    PgPool pool = createPool(options, new PoolOptions().setMaxSize(1)).connectHandler(hook);
    pool.query("SELECT id, randomnumber from WORLD").execute(ctx.asyncAssertSuccess(v2 -> {
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
      PgPool pool = createPool(new PgConnectOptions(options).setPort(8080).setHost("localhost"), new PoolOptions().setMaxSize(1)).connectHandler(hook);
      pool.getConnection(ctx.asyncAssertFailure(conn -> {
        async.countDown();
      }));
    }));
  }

  @Test
  public void testConnectionClosedInProvider1(TestContext ctx) {
    testConnectionClosedInProvider(ctx, true);
  }

  @Test
  public void testConnectionClosedInProvider2(TestContext ctx) {
    testConnectionClosedInProvider(ctx, false);
  }

  private void testConnectionClosedInProvider(TestContext ctx, boolean immediately) {
    Async async = ctx.async(2);
    ProxyServer proxy = ProxyServer.create(vertx, options.getPort(), options.getHost());
    AtomicReference<ProxyServer.Connection> proxyConn = new AtomicReference<>();
    proxy.proxyHandler(conn -> {
      proxyConn.set(conn);
      conn.connect();
    });
    proxy.listen(8080, "localhost", ctx.asyncAssertSuccess(v1 -> {
      PgConnectOptions options = new PgConnectOptions(this.options).setPort(8080).setHost("localhost");
      ConnectionFactory factory = PgDriver.INSTANCE.createConnectionFactory(vertx, options);
      PgPool pool = createPool(options, new PoolOptions().setMaxSize(1));
      pool.connectionProvider(context -> {
        Future<SqlConnection> fut = factory.connect(context);
        if (immediately) {
          return fut.map(conn -> {
            conn.close();
            return conn;
          });
        } else {
          return fut.flatMap(conn -> conn.close().map(conn));
        }
      });
      pool.getConnection(ctx.asyncAssertFailure(conn -> {
        vertx.runOnContext(v -> {
          ctx.assertEquals(0, pool.size());
          async.complete();
        });
      }));
    }));
  }

  @Test
  public void testConnectionTimeoutWhenExecutingDirectly(TestContext ctx) {
    PgPool pool = createPool(options, new PoolOptions().setConnectionTimeout(2).setMaxSize(2));
    final Async latch = ctx.async(2);
    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT id, message from immutable")
        .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(12, rows.size());
          latch.countDown();
        }));
    }));

    pool.getConnection(ctx.asyncAssertSuccess(conn -> {
      conn
        .query("SELECT id, message from immutable")
        .execute(ctx.asyncAssertSuccess(rows -> {
          ctx.assertEquals(12, rows.size());
          latch.countDown();
        }));
    }));

    latch.awaitSuccess();
    final long timerId = vertx.setTimer(10000L, id -> {
      ctx.fail("Timeout exceeded without completing");
    });
    //Used both connections
    Async async = ctx.async(10);
    for (int i = 0; i < 10; i++) {
      pool
        .query("SELECT id, message from immutable")
        .execute(ctx.asyncAssertFailure(t -> {
          ctx.assertTrue(t instanceof NoStackTraceThrowable);
          ctx.assertEquals("Timeout", t.getMessage());
          async.countDown();
        }));
    }

    async.handler(v -> vertx.cancelTimer(timerId));
  }
}
