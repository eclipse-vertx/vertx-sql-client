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

package io.vertx.sqlclient.impl.pool;

import io.vertx.core.impl.ContextInternal;
import io.vertx.core.impl.EventLoopContext;
import io.vertx.core.impl.VertxInternal;
import io.vertx.core.impl.future.PromiseInternal;
import io.vertx.core.net.impl.pool.ConnectResult;
import io.vertx.core.net.impl.pool.Lease;
import io.vertx.core.net.impl.pool.ConnectionPool;
import io.vertx.core.net.impl.pool.PoolConnection;
import io.vertx.core.net.impl.pool.PoolConnector;
import io.vertx.core.net.impl.pool.PoolWaiter;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.sqlclient.spi.DatabaseMetadata;
import io.vertx.core.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Todo :
 *
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SqlConnectionPool {

  private final ConnectionFactory factory;
  private final VertxInternal vertx;
  private final ConnectionPool<PooledConnection> pool;
  private final int pipeliningLimit;
  private final long idleTimeout;
  private final int maxSize;

  public SqlConnectionPool(ConnectionFactory factory, VertxInternal vertx, long idleTimeout, int maxSize, int pipeliningLimit, int maxWaitQueueSize) {
    Objects.requireNonNull(factory, "No null connector");
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    if (pipeliningLimit < 1) {
      throw new IllegalArgumentException("Pipelining limit must be > 0");
    }
    this.pool = ConnectionPool.pool(connector, new int[] { maxSize }, maxWaitQueueSize);
    this.vertx = vertx;
    this.pipeliningLimit = pipeliningLimit;
    this.idleTimeout = idleTimeout;
    this.maxSize = maxSize;
    this.factory = factory;

    if (pipeliningLimit > 1) {
      pool.connectionSelector((waiter, list) -> {
        PoolConnection<PooledConnection> selected = null;
        int size = list.size();
        for (int i = 0;i < size;i++) {
          PoolConnection<PooledConnection> conn = list.get(i);
          if (conn.concurrency() > 0) {
            if (selected == null) {
              selected = conn;
            } else if (conn.get().inflight < selected.get().inflight) {
              selected = conn;
            }
          }
        }
        return selected;
      });
    }

  }

  private final PoolConnector<PooledConnection> connector = new PoolConnector<PooledConnection>() {
    @Override
    public void connect(EventLoopContext context, PoolConnector.Listener listener, Handler<AsyncResult<ConnectResult<PooledConnection>>> handler) {
      PromiseInternal<Connection> promise = context.promise();
      factory.connect(promise);
      promise.future()
        .map(connection -> {
          PooledConnection pooled = new PooledConnection(connection, listener);
          connection.init(pooled);
          return new ConnectResult<>(pooled, pipeliningLimit, 0);
        })
        .onComplete(handler);
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

  public void checkExpired() {
    long now = System.currentTimeMillis();
    pool.evict(conn -> conn.expirationTimestamp < now, ar -> {
      if (ar.succeeded()) {
        List<PooledConnection> res = ar.result();
        for (PooledConnection conn : res) {
          conn.close(Promise.promise());
        }
      }
    });
  }

  public <R> Future<R> execute(ContextInternal context, CommandBase<R> cmd) {
    Promise<R> promise = context.promise();
    EventLoopContext eventLoopCtx = ConnectionFactory.asEventLoopContext(context);
    pool.acquire(eventLoopCtx, 0, ar -> {
      if (ar.succeeded()) {
        Lease<PooledConnection> lease = ar.result();
        PooledConnection pooled = lease.get();
        pooled.inflight++;
        pooled.num++;
        pooled.schedule(context, cmd)
          .onComplete(promise)
          .onComplete(v -> {
            pooled.expirationTimestamp = System.currentTimeMillis() + idleTimeout;
            pooled.inflight--;
            lease.recycle();
          });
      } else {
        promise.fail(ar.cause());
      }
    });
    return promise.future();
  }

  public void acquire(ContextInternal context, long timeout, Handler<AsyncResult<Connection>> handler) {
    class PoolRequest implements PoolWaiter.Listener<PooledConnection>, Handler<AsyncResult<Lease<PooledConnection>>> {
      private long timerID = -1L;
      @Override
      public void handle(AsyncResult<Lease<PooledConnection>> ar) {
        if (timerID != -1L) {
          vertx.cancelTimer(timerID);
        }
        if (ar.succeeded()) {
          Lease<PooledConnection> lease = ar.result();
          PooledConnection pooled = lease.get();
          pooled.lease = lease;
          handler.handle(Future.succeededFuture(pooled));
        } else {
          handler.handle(Future.failedFuture(ar.cause()));
        }
      }
      @Override
      public void onEnqueue(PoolWaiter<PooledConnection> waiter) {
        if (timeout > 0L && timerID == -1L) {
          timerID = context.setTimer(timeout, id -> {
            pool.cancel(waiter, ar -> {
              if (ar.succeeded()) {
                if (ar.result()) {
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
    EventLoopContext eventLoopContext = ConnectionFactory.asEventLoopContext(context);
    PoolRequest request = new PoolRequest();
    pool.acquire(eventLoopContext, request,0, request);
  }

  public Future<Void> close() {
    Promise<Void> promise = vertx.promise();
    pool.close(ar -> factory.close(promise));
    return promise.future();
  }

  private class PooledConnection implements Connection, Connection.Holder  {

    private final Connection conn;
    private final PoolConnector.Listener listener;
    private Holder holder;
    private Lease<PooledConnection> lease;
    public long expirationTimestamp;
    private int inflight;
    private int num;

    PooledConnection(Connection conn, PoolConnector.Listener listener) {
      this.conn = conn;
      this.listener = listener;
    }

    @Override
    public boolean isSsl() {
      return conn.isSsl();
    }

    @Override
    public DatabaseMetadata getDatabaseMetaData() {
      return conn.getDatabaseMetaData();
    }

    @Override
    public <R> Future<R> schedule(ContextInternal context, CommandBase<R> cmd) {
      return conn.schedule(context, cmd);
    }

    /**
     * Close the underlying connection
     */
    private void close(Promise<Void> promise) {
      conn.close(this, promise);
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
        Lease<PooledConnection> l = this.lease;
        this.holder = null;
        this.lease = null;
        this.expirationTimestamp = System.currentTimeMillis() + idleTimeout;
        l.recycle();
        promise.complete();
      }
    }

    @Override
    public void handleClosed() {
      if (holder != null) {
        holder.handleClosed();
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
  }

  public void check(Handler<AsyncResult<List<Integer>>> handler) {
    List<Integer> list = new ArrayList<>();
    pool.evict(pred -> {
      list.add(pred.num);
      return false;
    }, ar -> {
      handler.handle(Future.succeededFuture(list));
    });
  }
}
