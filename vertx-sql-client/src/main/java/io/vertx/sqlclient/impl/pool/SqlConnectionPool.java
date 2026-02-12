/*
 * Copyright (c) 2011-2022 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl.pool;

import io.netty.channel.EventLoop;
import io.vertx.core.*;
import io.vertx.core.internal.ContextInternal;
import io.vertx.core.internal.PromiseInternal;
import io.vertx.core.internal.VertxInternal;
import io.vertx.core.internal.net.NetSocketInternal;
import io.vertx.core.internal.pool.*;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.tracing.QueryReporter;
import io.vertx.sqlclient.internal.Connection;
import io.vertx.sqlclient.internal.SqlConnectionBase;
import io.vertx.sqlclient.internal.command.CommandBase;
import io.vertx.sqlclient.internal.command.QueryCommandBase;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Todo :
 *
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SqlConnectionPool {

  private static final Object NO_METRICS = new Object();

  private final Function<Context, Future<SqlConnection>> connectionProvider;
  private final VertxInternal vertx;
  private final PoolMetrics metrics;
  private final ConnectionPool<PooledConnection> pool;
  private final Handler<PooledConnection> hook;
  private final Function<Connection, Future<Void>> afterAcquire;
  private final Function<Connection, Future<Void>> beforeRecycle;
  private final boolean pipelined;
  private final long idleTimeout;
  private final long maxLifetime;
  private final int maxSize;

  public SqlConnectionPool(Function<Context, Future<SqlConnection>> connectionProvider,
                           PoolMetrics metrics,
                           Handler<PooledConnection> hook,
                           Function<Connection, Future<Void>> afterAcquire,
                           Function<Connection, Future<Void>> beforeRecycle,
                           VertxInternal vertx,
                           long idleTimeout,
                           long maxLifetime,
                           int maxSize,
                           boolean pipelined,
                           int maxWaitQueueSize,
                           int eventLoopSize) {
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    if (afterAcquire != null && beforeRecycle == null) {
      throw new IllegalArgumentException("afterAcquire and beforeRecycle hooks must be both not null");
    }
    this.pool = ConnectionPool.pool(connector, new int[]{maxSize}, maxWaitQueueSize);
    this.metrics = metrics;
    this.vertx = vertx;
    this.pipelined = pipelined;
    this.idleTimeout = idleTimeout;
    this.maxLifetime = maxLifetime;
    this.maxSize = maxSize;
    this.hook = hook;
    this.connectionProvider = connectionProvider;
    this.afterAcquire = afterAcquire;
    this.beforeRecycle = beforeRecycle;

    if (eventLoopSize > 0) {
      EventLoop[] loops = new EventLoop[eventLoopSize];
      for (int i = 0; i < eventLoopSize; i++) {
        loops[i] = vertx.nettyEventLoopGroup().next();
      }
      pool.contextProvider(new Function<>() {
        int idx = 0;

        @Override
        public ContextInternal apply(ContextInternal contextInternal) {
          EventLoop loop = loops[idx++];
          if (idx == loops.length) {
            idx = 0;
          }
          return vertx.contextBuilder().withEventLoop(loop).build();
        }
      });
    } else {
      pool.contextProvider(ctx -> ctx.owner().contextBuilder().withEventLoop(ctx.nettyEventLoop()).build());
    }
  }

  private final PoolConnector<PooledConnection> connector = new PoolConnector<>() {
    @Override
    public Future<ConnectResult<PooledConnection>> connect(ContextInternal context, Listener listener) {
      Future<SqlConnection> future = connectionProvider.apply(context);
      return future.compose(res -> {
        SqlConnectionBase connBase = (SqlConnectionBase) res;
        Connection conn = connBase.unwrap();
        if (conn.isValid()) {
          PooledConnection pooled = new PooledConnection(connBase.factory(), conn, listener);
          conn.init(pooled);
          if (hook != null) {
            Promise<ConnectResult<PooledConnection>> p = Promise.promise();
            pooled.poolCallback = p;
            hook.handle(pooled);
            return p.future();
          } else {
            return Future.succeededFuture(new ConnectResult<>(pooled, pipelined ? conn.pipeliningLimit() : 1, 0));
          }
        } else {
          return Future.failedFuture(NetSocketInternal.CLOSED_EXCEPTION);
        }
      });
    }

    @Override
    public boolean isValid(PooledConnection connection) {
      return true;
    }
  };

  public int available() {
    return maxSize - pool.size();
  }

  public int size() {
    return pool.size();
  }

  public void evict() {
    long now = System.currentTimeMillis();
    pool.evict(conn -> conn.shouldEvict(now), (res, err) -> {
      if (err == null) {
        for (PooledConnection conn : res) {
          conn.close(Promise.promise());
        }
      }
    });
  }

  private Object enqueueMetric() {
    if (metrics != null) {
      try {
        return metrics.enqueue();
      } catch (Exception e) {
        // Log
      }
    }
    return NO_METRICS;
  }

  private void dequeueMetric(Object metric) {
    if (metrics != null && metric != NO_METRICS) {
      try {
        metrics.dequeue(metric);
      } catch (Exception e) {
        //
      }
    }
  }

  private Object beginMetric() {
    if (metrics != null) {
      try {
        return metrics.begin();
      } catch (Exception e) {
        //
      }
    }
    return NO_METRICS;
  }

  private void endMetric(Object metric) {
    if (metrics != null && metric != NO_METRICS) {
      try {
        metrics.end(metric);
      } catch (Exception e) {
        //
      }
    }
  }

  private static final Exception POOL_QUERY_TIMEOUT_EXCEPTION = new VertxException("Timeout waiting for connection", true);

  // TODO : try optimize without promise
  public <R> void execute(CommandBase<R> cmd, Completable<R> handler, long timeout) {
    ContextInternal context = vertx.getOrCreateContext();
    Promise<Lease<PooledConnection>> p = context.promise();
    long timerId;
    if (timeout > 0) {
      timerId = vertx.setTimer(timeout, t -> handler.fail(POOL_QUERY_TIMEOUT_EXCEPTION));
    } else {
      timerId = -1;
    }
    Object metric = enqueueMetric();
    pool.acquire(context, 0, p);
    p.future().compose(lease -> {
      dequeueMetric(metric);
      PooledConnection pooled = lease.get();
      Future<R> future;
      if (timerId != -1 && !vertx.cancelTimer(timerId)) {
        // We want to make sure the connection is released properly below
        // But we don't want to record begin/end pool metrics
        pooled.timerMetric = NO_METRICS;
        future = Future.failedFuture(POOL_QUERY_TIMEOUT_EXCEPTION);
      } else {
        pooled.timerMetric = beginMetric();
        if (afterAcquire != null) {
          Connection conn = pooled.conn;
          future = afterAcquire.apply(conn)
            .compose(v -> Future.<R>future(d -> pooled.schedule(cmd, d)))
            .eventually(() -> beforeRecycle.apply(conn));
        } else {
          PromiseInternal<R> pp = context.promise();
          pooled.schedule(cmd, pp);
          future = pp;
        }
      }
      return future.andThen(ar -> {
        endMetric(pooled.timerMetric);
        pooled.refresh();
        lease.recycle();
      });
    }).onComplete(ar -> {
      if (ar.succeeded()) {
        handler.succeed(ar.result());
      } else if (!POOL_QUERY_TIMEOUT_EXCEPTION.equals(ar.cause())) {
        handler.fail(ar.cause());
      }
    });
  }

  public void acquire(ContextInternal context, long timeout, Completable<PooledConnection> handler) {
    class PoolRequest implements PoolWaiter.Listener<PooledConnection>, Completable<Lease<PooledConnection>> {

      private final Object metric;
      private long timerID = -1L;

      PoolRequest(Object metric) {
        this.metric = metric;
      }

      @Override
      public void complete(Lease<PooledConnection> lease, Throwable failure) {
        if (timerID != -1L && !vertx.cancelTimer(timerID)) {
          lease.recycle();
        } else {
          if (failure == null) {
            if (afterAcquire != null) {
              afterAcquire.apply(lease.get().conn).onComplete(ar2 -> {
                if (ar2.succeeded()) {
                  handle(lease);
                } else {
                  // Should we do some cleanup ?
                  handler.fail(failure);
                }
              });
            } else {
              handle(lease);
            }
          } else {
            handler.fail(failure);
          }
        }
      }

      private void handle(Lease<PooledConnection> lease) {
        dequeueMetric(metric);
        PooledConnection pooled = lease.get();
        pooled.timerMetric = beginMetric();
        pooled.lease = lease;
        handler.succeed(pooled);
      }

      @Override
      public void onEnqueue(PoolWaiter<PooledConnection> waiter) {
        if (timeout > 0L && timerID == -1L) {
          timerID = context.setTimer(timeout, id -> {
            pool.cancel(waiter, (res, err) -> {
              if (err == null) {
                if (res) {
                  dequeueMetric(metric);
                  handler.fail("Timeout");
                }
              } else {
                // ????
              }
            });
          });
        }
      }

      @Override
      public void onConnect(PoolWaiter<PooledConnection> waiter) {
        onEnqueue(waiter);
      }
    }
    Object metric = enqueueMetric();
    PoolRequest request = new PoolRequest(metric);
    pool.acquire(context, request, 0, request);
  }

  public Future<Void> close() {
    Promise<Void> promise = vertx.promise();
    pool.close((res, err) -> {
      if (err == null) {
        List<Future<Void>> results = res.stream()
          .map(connection -> connection
            .compose(pooled -> Future.<Void>future(p -> pooled.conn.close(pooled, p))))
          .collect(Collectors.toList());
        Future
          .join(results)
          .<Void>mapEmpty()
          .onComplete(promise);
      } else {
        promise.fail(err);
      }
    });
    return promise.future();
  }

  public class PooledConnection implements Connection, Connection.Holder {

    private final ConnectionFactory factory;
    private final Connection conn;
    private final PoolConnector.Listener listener;
    private Holder holder;
    private Promise<ConnectResult<PooledConnection>> poolCallback;
    private Lease<PooledConnection> lease;
    private Object timerMetric;
    public long idleEvictionTimestamp;
    public long lifetimeEvictionTimestamp;

    PooledConnection(ConnectionFactory factory, Connection conn, PoolConnector.Listener listener) {
      this.factory = factory;
      this.conn = conn;
      this.listener = listener;
      this.lifetimeEvictionTimestamp = maxLifetime > 0 ? System.currentTimeMillis() + maxLifetime : Long.MAX_VALUE;
      refresh();
    }

    @Override
    public ClientMetrics metrics() {
      return conn.metrics();
    }

    @Override
    public TracingPolicy tracingPolicy() {
      return conn.tracingPolicy();
    }

    @Override
    public String system() {
      return conn.system();
    }

    @Override
    public String database() {
      return conn.database();
    }

    @Override
    public String user() {
      return conn.user();
    }

    public ConnectionFactory factory() {
      return factory;
    }

    @Override
    public SocketAddress server() {
      return conn.server();
    }

    @Override
    public boolean isSsl() {
      return conn.isSsl();
    }

    @Override
    public boolean isValid() {
      return true;
    }

    @Override
    public int pipeliningLimit() {
      return conn.pipeliningLimit();
    }

    @Override
    public DatabaseMetadata getDatabaseMetaData() {
      return conn.getDatabaseMetaData();
    }

    @Override
    public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
      ContextInternal context = vertx.getOrCreateContext();
      QueryReporter queryReporter;
      VertxTracer tracer = vertx.tracer();
      ClientMetrics metrics = conn.metrics();
      if (cmd instanceof QueryCommandBase && (tracer != null || metrics != null)) {
        queryReporter = new QueryReporter(tracer, metrics, context, (QueryCommandBase<?>) cmd, conn);
        queryReporter.before();
      } else {
        queryReporter = null;
      }
      if (queryReporter != null) {
        Completable<R> ori = handler;
        handler = (res, err) -> {
          queryReporter.after(res, err);
          ori.complete(res, err);
        };
      }
      conn.schedule(cmd, handler);
    }


    /**
     * Close the underlying connection
     */
    private void close(Promise<Void> promise) {
      conn.close(this, promise);
    }

    private void refresh() {
      this.idleEvictionTimestamp = idleTimeout > 0 ? System.currentTimeMillis() + idleTimeout : Long.MAX_VALUE;
    }

    @Override
    public void init(Holder holder) {
      if (this.holder != null) {
        throw new IllegalStateException();
      }
      this.holder = holder;
    }

    @Override
    public void close(Holder holder, Completable<Void> promise) {
      doClose(holder, promise);
    }

    private void doClose(Holder holder, Completable<Void> promise) {
      if (holder != this.holder) {
        String msg;
        if (this.holder == null) {
          msg = "Connection released twice";
        } else {
          msg = "Connection released by " + holder + " owned by " + this.holder;
        }
        // Log it ?
        promise.fail(msg);
      } else {
        this.holder = null;
        Promise<ConnectResult<PooledConnection>> resultHandler = poolCallback;
        if (resultHandler != null) {
          poolCallback = null;
          promise.succeed();
          resultHandler.complete(new ConnectResult<>(this, pipelined ? conn.pipeliningLimit() : 1, 0));
          return;
        }
        if (beforeRecycle == null) {
          cleanup(promise);
        } else {
          beforeRecycle.apply(lease.get().conn).onComplete(ar -> cleanup(promise));
        }
      }
    }

    private void cleanup(Completable<Void> promise) {
      endMetric(timerMetric);
      Lease<PooledConnection> l = this.lease;
      this.lease = null;
      refresh();
      l.recycle();
      promise.succeed();
    }

    @Override
    public void handleClosed() {
      if (holder != null) {
        holder.handleClosed();
      }
      Promise<ConnectResult<PooledConnection>> resultHandler = poolCallback;
      if (resultHandler != null) {
        poolCallback = null;
        resultHandler.fail(NetSocketInternal.CLOSED_EXCEPTION);
      }
      listener.onRemove();
    }

    @Override
    public void handleEvent(Object event) {
      if (holder != null) {
        holder.handleEvent(event);
      }
    }

    @Override
    public void handleException(Throwable err) {
      if (holder != null) {
        holder.handleException(err);
      }
    }

    @Override
    public int getProcessId() {
      return conn.getProcessId();
    }

    @Override
    public int getSecretKey() {
      return conn.getSecretKey();
    }

    @Override
    public Connection unwrap() {
      return conn;
    }

    private boolean hasIdleExpired(long now) {
      return idleEvictionTimestamp < now;
    }

    private boolean hasLifetimeExpired(long now) {
      return lifetimeEvictionTimestamp < now;
    }

    private boolean shouldEvict(long now) {
      return hasIdleExpired(now) || hasLifetimeExpired(now);
    }

  }
}
