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
import io.vertx.sqlclient.SqlConnectOptions;
import io.vertx.sqlclient.impl.tracing.QueryReporter;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.sqlclient.spi.connection.Connection;
import io.vertx.sqlclient.spi.connection.ConnectionContext;
import io.vertx.sqlclient.spi.connection.ConnectionFactory;
import io.vertx.sqlclient.spi.protocol.CommandBase;
import io.vertx.sqlclient.spi.protocol.QueryCommandBase;

import java.util.List;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;
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

  private static final Object NO_METRICS = new Object();

  private final Function<Context, Future<Connection>> connectionProvider;
  private final ConnectionFactory<?> connectionFactory;
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

  // TODO : use connection provider with Connection instead of SqlConnection
  public <O extends SqlConnectOptions> SqlConnectionPool(Supplier<Future<O>> optionsProvider,
                           ConnectionFactory<O> connectionFactory,
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
    this.connectionFactory = connectionFactory;
    this.metrics = metrics;
    this.vertx = vertx;
    this.pipelined = pipelined;
    this.idleTimeout = idleTimeout;
    this.maxLifetime = maxLifetime;
    this.maxSize = maxSize;
    this.hook = hook;
    this.connectionProvider = context -> connectionFactory.connect(context, optionsProvider.get());
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
      Future<Connection> future = connectionProvider.apply(context);
      return future.compose(conn -> {
        if (conn.isValid()) {
          PooledConnection pooled = new PooledConnection(connectionFactory, conn, listener);
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

  /**
   * A connection acquisition request.
   *
   * <p>The pool notifies the waiter listener and delivers the outcome of the acquisition on arbitrary threads,
   * therefore the request lifecycle is arbitrated by the single atomically updated {@link #state} field:
   *
   * <ul>
   *   <li>{@link #NO_TIMER}: no acquisition timer has been set</li>
   *   <li>a zero or positive value: the identifier of the acquisition timer</li>
   *   <li>{@link #COMPLETED}: terminal state</li>
   * </ul>
   *
   * <p>Whoever moves the state to {@link #COMPLETED} owns the dequeue metric and the single completion of the
   * request. Arbitrating with a single variable is necessary: relying on the combined outcome of
   * {@code Vertx#cancelTimer} and {@code ConnectionPool#cancel} is racy because both can report a loss for the
   * very same request, leaving it uncompleted forever.
   */
  private abstract static class PoolRequest implements PoolWaiter.Listener<PooledConnection>, Completable<Lease<PooledConnection>>, Handler<Long> {

    private static final AtomicLongFieldUpdater<PoolRequest> STATE = AtomicLongFieldUpdater.newUpdater(PoolRequest.class, "state");

    private static final long NO_TIMER = -1L;
    private static final long COMPLETED = -2L;

    private final SqlConnectionPool owner;
    private final ContextInternal context;
    private final long timeout;
    private final Object metric;

    private volatile long state = NO_TIMER;

    // Assigned before the acquisition timer is set and only read when this timer fires
    // It does not need to be volatile: setting the timer happens after the assignment and makes it visible to the thread firing the timer
    private PoolWaiter<PooledConnection> waiter;

    PoolRequest(SqlConnectionPool owner, ContextInternal context, long timeout) {
      this.owner = owner;
      this.context = context;
      this.timeout = timeout;
      this.metric = owner.enqueueMetric();
    }

    @Override
    public void onEnqueue(PoolWaiter<PooledConnection> waiter) {
      setAcquisitionTimer(waiter);
    }

    @Override
    public void onConnect(PoolWaiter<PooledConnection> waiter) {
      setAcquisitionTimer(waiter);
    }

    /**
     * Set the acquisition timer of this request, unless a timer has been set already.
     *
     * <p>The timer is set from the waiter listener callbacks because this is the only moment the waiter is
     * known, and consequently the only moment the request can be canceled. When the pool satisfies the request
     * without notifying the listener, the connection is available immediately and no timer is needed.
     *
     * <p>A waiter is notified twice when it is enqueued first and later associated with a connection request,
     * the state prevents a second timer.
     *
     * <p>The pool executes the listener callbacks on the thread that happens to drain its combiner, which is
     * neither necessarily the requester thread nor the connection thread, and it can even execute them after
     * the request has been completed. The state arbitrates both cases.
     */
    private void setAcquisitionTimer(PoolWaiter<PooledConnection> waiter) {
      if (timeout > 0L && state == NO_TIMER) {
        this.waiter = waiter;
        long timerId = context.setTimer(timeout, this);
        if (!STATE.compareAndSet(this, NO_TIMER, timerId)) {
          // The request has been completed or another timer has been set concurrently
          owner.vertx.cancelTimer(timerId);
        }
      }
    }

    /**
     * The acquisition timer fired: give up the request unless it has been completed meanwhile.
     */
    @Override
    public void handle(Long timerId) {
      if (STATE.getAndSet(this, COMPLETED) == COMPLETED) {
        return;
      }
      owner.dequeueMetric(metric);
      // Leave the pool wait queue before failing, the timed out request must no longer take a slot in the wait queue
      owner.pool.cancel(waiter, (cancelled, err) -> handleFailure(POOL_QUERY_TIMEOUT_EXCEPTION));
    }

    @Override
    public void complete(Lease<PooledConnection> lease, Throwable failure) {
      long previous = STATE.getAndSet(this, COMPLETED);
      if (previous == COMPLETED) {
        // The request has timed out, the connection is not needed anymore
        if (failure == null) {
          lease.get().refresh();
          lease.recycle();
        }
        return;
      }
      if (previous != NO_TIMER) {
        owner.vertx.cancelTimer(previous);
      }
      owner.dequeueMetric(metric);
      if (failure == null) {
        handleLease(lease);
      } else {
        handleFailure(failure);
      }
    }

    /**
     * Hand the acquired {@code lease} over to the requester, which becomes responsible for recycling it.
     *
     * <p>The pool emits a lease on the context of the connection, therefore this callback is executed on that
     * context and <i>not</i> on the context of the requester: implementations must not rely on the current
     * context and shall use {@link #context} instead, e.g. to report a command on the context of the requester.
     *
     * <p>Called at most once, and never after {@link #handleFailure}.
     */
    abstract void handleLease(Lease<PooledConnection> lease);

    /**
     * Fail the request.
     *
     * <p>Executed on the context of the requester when the pool rejects the request, on the context of the
     * connection when the pool closes while connecting, and on the thread draining the pool combiner when the
     * request times out. Implementations shall therefore complete the requester with a context aware promise
     * when the failure must be dispatched on a specific context.
     *
     * <p>Called at most once, and never after {@link #handleLease}.
     */
    abstract void handleFailure(Throwable failure);
  }

  public <R> void execute(CommandBase<R> cmd, Completable<R> handler, long timeout) {
    ContextInternal context = vertx.getOrCreateContext();
    class ExecutePoolRequest extends PoolRequest {

      ExecutePoolRequest() {
        super(SqlConnectionPool.this, context, timeout);
      }

      @Override
      void handleLease(Lease<PooledConnection> lease) {
        PooledConnection pooled = lease.get();
        pooled.timerMetric = beginMetric();
        Future<R> future;
        if (afterAcquire != null) {
          Connection conn = pooled.conn;
          // The hooks are a pair: the connection is recycled as it was acquired when the acquisition hook fails
          future = afterAcquire.apply(conn)
            .compose(v -> Future.<R>future(d -> pooled.schedule(context, cmd, d))
              .eventually(() -> beforeRecycle.apply(conn)));
        } else {
          PromiseInternal<R> pp = context.promise();
          pooled.schedule(context, cmd, pp);
          future = pp;
        }
        future.onComplete((res, err) -> {
          endMetric(pooled.timerMetric);
          pooled.refresh();
          lease.recycle();
          handler.complete(res, err);
        });
      }

      @Override
      void handleFailure(Throwable failure) {
        handler.fail(failure);
      }
    }
    ExecutePoolRequest request = new ExecutePoolRequest();
    pool.acquire(context, request, 0, request);
  }

  public void acquire(ContextInternal context, long timeout, Promise<PooledConnection> handler) {
    class AcquirePoolRequest extends PoolRequest {

      AcquirePoolRequest() {
        super(SqlConnectionPool.this, context, timeout);
      }

      @Override
      void handleLease(Lease<PooledConnection> lease) {
        if (afterAcquire != null) {
          afterAcquire.apply(lease.get().conn).onComplete((v, err) -> {
            if (err == null) {
              handleConnection(lease);
            } else {
              lease.recycle();
              handler.fail(err);
            }
          });
        } else {
          handleConnection(lease);
        }
      }

      private void handleConnection(Lease<PooledConnection> lease) {
        PooledConnection pooled = lease.get();
        pooled.timerMetric = beginMetric();
        // The lease is recycled when the connection is given back to the pool, see PooledConnection#cleanup
        pooled.lease = lease;
        handler.succeed(pooled);
      }

      @Override
      void handleFailure(Throwable failure) {
        handler.fail(failure);
      }
    }
    AcquirePoolRequest request = new AcquirePoolRequest();
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

  public class PooledConnection implements Connection, ConnectionContext {

    private final ConnectionFactory factory;
    private final Connection conn;
    private final PoolConnector.Listener listener;
    private ConnectionContext holder;
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
    public DatabaseMetadata databaseMetadata() {
      return conn.databaseMetadata();
    }

    @Override
    public <R> void schedule(CommandBase<R> cmd, Completable<R> handler) {
      schedule(vertx.getOrCreateContext(), cmd, handler);
    }

    /**
     * Schedule a command, reporting it on the specified {@code context}.
     *
     * <p>The pool hands a connection over on the context of this connection, therefore the context of the
     * requester cannot be inferred from the current context and must be provided by the caller.
     */
    <R> void schedule(ContextInternal context, CommandBase<R> cmd, Completable<R> handler) {
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
    public void init(ConnectionContext context) {
      if (this.holder != null) {
        throw new IllegalStateException();
      }
      this.holder = context;
    }

    @Override
    public void close(ConnectionContext holder, Completable<Void> promise) {
      doClose(holder, promise);
    }

    private void doClose(ConnectionContext holder, Completable<Void> promise) {
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
    public void handleException(Throwable failure) {
      if (holder != null) {
        holder.handleException(failure);
      }
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
