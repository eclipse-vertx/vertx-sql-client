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
import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.net.SocketAddress;
import io.vertx.core.net.impl.ConnectionBase;
import io.vertx.core.net.impl.pool.*;
import io.vertx.core.spi.metrics.ClientMetrics;
import io.vertx.core.spi.metrics.PoolMetrics;
import io.vertx.core.spi.tracing.VertxTracer;
import io.vertx.core.tracing.TracingPolicy;
import io.vertx.sqlclient.SqlConnection;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.SqlConnectionBase;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.impl.command.QueryCommandBase;
import io.vertx.sqlclient.impl.tracing.QueryReporter;
import io.vertx.sqlclient.spi.ConnectionFactory;
import io.vertx.sqlclient.spi.DatabaseMetadata;

import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * Todo :
 *
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SqlConnectionPool {

  private static final Object NO_METRICS = "bilto";

  private final Function<Context, Future<SqlConnection>> connectionProvider;
  private final PoolMetrics metrics;
  private final VertxInternal vertx;
  private final ConnectionPool<PooledConnection> pool;
  private final Supplier<Handler<PooledConnection>> hook;
  private final Function<Connection, Future<Void>> afterAcquire;
  private final Function<Connection, Future<Void>> beforeRecycle;
  private final boolean pipelined;
  private final long idleTimeout;
  private final long maxLifetime;
  private final int maxSize;

  public SqlConnectionPool(Function<Context, Future<SqlConnection>> connectionProvider,
                           Supplier<Handler<PooledConnection>> hook,
                           PoolMetrics<?> metrics,
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
    this.vertx = vertx;
    this.pipelined = pipelined;
    this.idleTimeout = idleTimeout;
    this.maxLifetime = maxLifetime;
    this.maxSize = maxSize;
    this.hook = hook;
    this.metrics = metrics;
    this.connectionProvider = connectionProvider;
    this.afterAcquire = afterAcquire;
    this.beforeRecycle = beforeRecycle;

    if (eventLoopSize > 0) {
      EventLoop[] loops = new EventLoop[eventLoopSize];
      for (int i = 0; i < eventLoopSize; i++) {
        loops[i] = vertx.nettyEventLoopGroup().next();
      }
      pool.contextProvider(new Function<ContextInternal, ContextInternal>() {
        int idx = 0;

        @Override
        public ContextInternal apply(ContextInternal contextInternal) {
          EventLoop loop = loops[idx++];
          if (idx == loops.length) {
            idx = 0;
          }
          return vertx.createEventLoopContext(loop, null, Thread.currentThread().getContextClassLoader());
        }
      });
    } else {
      pool.contextProvider(ctx -> ctx.owner().createEventLoopContext(ctx.nettyEventLoop(), null, Thread.currentThread().getContextClassLoader()));
    }
  }

  private final PoolConnector<PooledConnection> connector = new PoolConnector<PooledConnection>() {
    @Override
    public void connect(ContextInternal context, PoolConnector.Listener listener, Handler<AsyncResult<ConnectResult<PooledConnection>>> handler) {
      Future<SqlConnection> future = connectionProvider.apply(context);
      future.onComplete(ar -> {
        if (ar.succeeded()) {
          SqlConnectionBase res = (SqlConnectionBase) ar.result();
          Connection conn = res.unwrap();
          if (conn.isValid()) {
            PooledConnection pooled = new PooledConnection(res.factory(), conn, listener);
            conn.init(pooled);
            Handler<PooledConnection> connectionHandler = hook.get();
            if (connectionHandler != null) {
              pooled.poolResultHandler = handler;
              connectionHandler.handle(pooled);
            } else {
              handler.handle(Future.succeededFuture(new ConnectResult<>(pooled, pipelined ? conn.pipeliningLimit() : 1, 0)));
            }
          } else {
            handler.handle(Future.failedFuture(ConnectionBase.CLOSED_EXCEPTION));
          }
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
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
    pool.evict(conn -> conn.shouldEvict(now), ar -> {
      if (ar.succeeded()) {
        List<PooledConnection> res = ar.result();
        for (PooledConnection conn : res) {
          conn.close(Promise.promise());
        }
      }
    });
  }

  private Object enqueueMetric() {
    if (metrics != null) {
      try {
        return metrics.submitted();
      } catch (Exception e) {
        // Log
      }
    }
    return NO_METRICS;
  }

  private Object dequeueAndBeginUse(Object metric) {
    if (metrics != null && metric != NO_METRICS) {
      try {
        return metrics.begin(metric);
      } catch (Exception e) {
        // Log
      }
    }
    return NO_METRICS;
  }

  private void dequeueAndReject(Object metric) {
    if (metrics != null && metric != NO_METRICS) {
      try {
        metrics.rejected(metric);
      } catch (Exception e) {
        // Log
      }
    }
  }

  private Object endUse(Object metric) {
    if (metrics != null && metric != NO_METRICS) {
      try {
        metrics.end(metric, false);
      } catch (Exception e) {
        // Log
      }
    }
    return NO_METRICS;
  }

  public <R> Future<R> execute(ContextInternal context, CommandBase<R> cmd) {
    Promise<Lease<PooledConnection>> p = context.promise();
    Object queueMetric = enqueueMetric();
    pool.acquire(context, 0, p);
    return p.future().compose(lease -> {
      Object useMetric = dequeueAndBeginUse(queueMetric);
      PooledConnection pooled = lease.get();
      Connection conn = pooled.conn;
      Future<R> future;
      if (afterAcquire != null) {
        future = afterAcquire.apply(conn)
          .compose(v -> pooled.schedule(context, cmd))
          .eventually(v -> beforeRecycle.apply(conn));
      } else {
        future = pooled.schedule(context, cmd);
      }
      return future.andThen(ar -> {
        endUse(useMetric);
        pooled.refresh();
        lease.recycle();
      });
    });
  }

  public void acquire(ContextInternal context, long timeout, Handler<AsyncResult<PooledConnection>> handler) {
    class PoolRequest implements PoolWaiter.Listener<PooledConnection>, Handler<AsyncResult<Lease<PooledConnection>>> {

      private final Object queueMetric;
      private long timerID;

      public PoolRequest(Object queueMetric) {
        this.queueMetric = queueMetric;
        this.timerID = -1L;
      }

      @Override
      public void handle(AsyncResult<Lease<PooledConnection>> ar) {
        if (timerID != -1L) {
          vertx.cancelTimer(timerID);
        }
        if (ar.succeeded()) {
          Lease<PooledConnection> lease = ar.result();
          if (afterAcquire != null) {
            afterAcquire.apply(lease.get().conn).onComplete(ar2 -> {
              if (ar2.succeeded()) {
                handle(lease);
              } else {
                // Should we do some cleanup ?
                handler.handle(Future.failedFuture(ar.cause()));
              }
            });
          } else {
            handle(lease);
          }
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      }

      private void handle(Lease<PooledConnection> lease) {
        Object useMetric = dequeueAndBeginUse(queueMetric);
        PooledConnection pooled = lease.get();
        pooled.lease = lease;
        pooled.useMetric = useMetric;
        handler.handle(Future.succeededFuture(pooled));
      }

      @Override
      public void onEnqueue(PoolWaiter<PooledConnection> waiter) {
        if (timeout > 0L && timerID == -1L) {
          timerID = context.setTimer(timeout, id -> {
            pool.cancel(waiter, ar -> {
              if (ar.succeeded()) {
                if (ar.result()) {
                  dequeueAndReject(queueMetric);
                  handler.handle(Future.failedFuture("Timeout"));
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
    Object queueMetric = enqueueMetric();
    PoolRequest request = new PoolRequest(queueMetric);
    pool.acquire(context, request, 0, request);
  }

  public Future<Void> close() {
    Promise<Void> promise = vertx.promise();
    pool.close(ar1 -> {
      if (ar1.succeeded()) {
        List<Future> results = ar1
          .result()
          .stream()
          .map(connection -> connection
            .compose(pooled -> Future.<Void>future(p -> pooled.conn.close(pooled, p))))
          .collect(Collectors.toList());
        CompositeFuture
          .join(results)
          .<Void>mapEmpty()
          .onComplete(promise);
      } else {
        promise.fail(ar1.cause());
      }
    });
    Future<Void> f = promise.future();
    if (metrics != null) {
      f = f.andThen(ar -> metrics.close());
    }
    return f;
  }

  public class PooledConnection implements Connection, Connection.Holder {

    private final ConnectionFactory factory;
    private final Connection conn;
    private final PoolConnector.Listener listener;
    private Holder holder;
    private Handler<AsyncResult<ConnectResult<PooledConnection>>> poolResultHandler;
    private Lease<PooledConnection> lease;
    private Object useMetric;
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
    public int pipeliningLimit() {
      return conn.pipeliningLimit();
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
    public DatabaseMetadata getDatabaseMetaData() {
      return conn.getDatabaseMetaData();
    }

    @Override
    public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
      QueryReporter queryReporter;
      VertxTracer tracer = vertx.tracer();
      ClientMetrics metrics = conn.metrics();
      if (cmd instanceof QueryCommandBase && (tracer != null || metrics != null)) {
        queryReporter = new QueryReporter(tracer, metrics, context, (QueryCommandBase<?>) cmd, conn);
        queryReporter.before();
      } else {
        queryReporter = null;
      }
      Future<R> fut = conn.schedule(context, cmd);
      if (queryReporter != null) {
        fut = fut.andThen(queryReporter::after);
      }
      return fut;
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
    public void close(Holder holder, Promise<Void> promise) {
      doClose(holder, promise);
    }

    private void doClose(Holder holder, Promise<Void> promise) {
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
        Handler<AsyncResult<ConnectResult<PooledConnection>>> resultHandler = poolResultHandler;
        if (resultHandler != null) {
          poolResultHandler = null;
          promise.complete();
          resultHandler.handle(Future.succeededFuture(new ConnectResult<>(this, pipelined ? conn.pipeliningLimit() : 1, 0)));
          return;
        }
        if (beforeRecycle == null) {
          cleanup(promise);
        } else {
          beforeRecycle.apply(lease.get().conn).onComplete(ar -> cleanup(promise));
        }
      }
    }

    private void cleanup(Promise<Void> promise) {
      Lease<PooledConnection> l = this.lease;
      Object useMetric = this.useMetric;
      this.lease = null;
      this.useMetric = null;
      refresh();
      endUse(useMetric);
      l.recycle();
      promise.complete();
    }

    @Override
    public void handleClosed() {
      if (holder != null) {
        holder.handleClosed();
      }
      Handler<AsyncResult<ConnectResult<PooledConnection>>> resultHandler = poolResultHandler;
      if (resultHandler != null) {
        poolResultHandler = null;
        resultHandler.handle(Future.failedFuture(ConnectionBase.CLOSED_EXCEPTION));
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
