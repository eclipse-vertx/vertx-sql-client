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
import io.vertx.sqlclient.PoolOptions;
import io.vertx.sqlclient.impl.Connection;
import io.vertx.sqlclient.impl.ConnectionFactory;
import io.vertx.sqlclient.impl.command.CommandBase;
import io.vertx.core.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * Todo :
 *
 * - handle timeout when acquiring a connection
 * - for per statement pooling, have several physical connection and use the less busy one to avoid head of line blocking effect
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConnectionPool {

  private final ConnectionFactory connector;
  private final ContextInternal context;
  private final int maxSize;
  private final ArrayDeque<Handler<AsyncResult<Connection>>> waiters = new ArrayDeque<>();
  private final Set<PooledConnection> all = new HashSet<>();
  private final ArrayDeque<PooledConnection> available = new ArrayDeque<>();
  private int size;
  private final int maxWaitQueueSize;
  private boolean checkInProgress;
  private boolean closed;

  public ConnectionPool(ConnectionFactory connector, int maxSize) {
    this(connector, maxSize, PoolOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE);
  }

  public ConnectionPool(ConnectionFactory connector, int maxSize, int maxWaitQueueSize) {
    this(connector, null, maxSize, maxWaitQueueSize);
  }

  public ConnectionPool(ConnectionFactory connector, Context context, int maxSize, int maxWaitQueueSize) {
    Objects.requireNonNull(connector, "No null connector");
    if (maxSize < 1) {
      throw new IllegalArgumentException("Pool max size must be > 0");
    }
    this.maxSize = maxSize;
    this.context = (ContextInternal) context;
    this.maxWaitQueueSize = maxWaitQueueSize;
    this.connector = connector;
  }

  public int available() {
    return available.size();
  }

  public int size() {
    return size;
  }

  public void acquire(Handler<AsyncResult<Connection>> waiter) {
    if (context != null) {
      context.dispatch(waiter, this::doAcquire);
    } else {
      doAcquire(waiter);
    }
  }

  private void doAcquire(Handler<AsyncResult<Connection>> waiter) {
    if (closed) {
      IllegalStateException err = new IllegalStateException("Connection pool closed");
      if (context != null) {
        waiter.handle(context.failedFuture(err));
      } else {
        waiter.handle(Future.failedFuture(err));
      }
      return;
    }
    waiters.add(waiter);
    check();
  }

  public void close() {
    if (closed) {
      throw new IllegalStateException("Connection pool already closed");
    }
    closed = true;
    for (PooledConnection pooled : new ArrayList<>(all)) {
      pooled.close();
    }
    Future<Connection> failure = Future.failedFuture("Connection pool closed");
    for (Handler<AsyncResult<Connection>> pending : waiters) {
      try {
        pending.handle(failure);
      } catch (Exception ignore) {
      }
    }
  }

  private class PooledConnection implements Connection, Connection.Holder  {

    private final Connection conn;
    private Holder holder;

    PooledConnection(Connection conn) {
      this.conn = conn;
    }

    @Override
    public boolean isSsl() {
      return conn.isSsl();
    }

    @Override
    public <R> void schedule(CommandBase<R> cmd, Promise<R> handler) {
      conn.schedule(cmd, handler);
    }

    /**
     * Close the underlying connection
     */
    private void close() {
      conn.close(this);
    }

    @Override
    public void init(Holder holder) {
      if (this.holder != null) {
        throw new IllegalStateException();
      }
      this.holder = holder;
    }

    @Override
    public void close(Holder holder) {
      if (holder != this.holder) {
        throw new IllegalStateException();
      }
      this.holder = null;
      release(this);
    }

    @Override
    public void handleClosed() {
      if (all.remove(this)) {
        size--;
        if (holder == null) {
          available.remove(this);
        } else {
          holder.handleClosed();
        }
        check();
      } else {
        throw new IllegalStateException();
      }
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

  private void release(PooledConnection proxy) {
    if (all.contains(proxy)) {
      available.add(proxy);
      check();
    }
  }

  private void check() {
    if (closed) {
      return;
    }
    if (!checkInProgress) {
      checkInProgress = true;
      try {
        while (waiters.size() > 0) {
          if (available.size() > 0) {
            PooledConnection proxy = available.poll();
            Handler<AsyncResult<Connection>> waiter = waiters.poll();
            waiter.handle(Future.succeededFuture(proxy));
          } else {
            if (size < maxSize) {
              Handler<AsyncResult<Connection>> waiter = waiters.poll();
              size++;
              connector.connect().onComplete(ar -> {
                if (ar.succeeded()) {
                  Connection conn = ar.result();
                  PooledConnection proxy = new PooledConnection(conn);
                  all.add(proxy);
                  conn.init(proxy);
                  waiter.handle(Future.succeededFuture(proxy));
                } else {
                  size--;
                  waiter.handle(Future.failedFuture(ar.cause()));
                  check();
                }
              });
            } else {
              if (maxWaitQueueSize >= 0) {
                int numInProgress = size - all.size();
                int numToFail = waiters.size() - (maxWaitQueueSize + numInProgress);
                while (numToFail-- > 0) {
                  Handler<AsyncResult<Connection>> waiter = waiters.pollLast();
                  waiter.handle(Future.failedFuture("Max waiter size reached"));
                }
              }
              break;
            }
          }
        }
      } finally {
        checkInProgress = false;
      }
    }
  }
}
