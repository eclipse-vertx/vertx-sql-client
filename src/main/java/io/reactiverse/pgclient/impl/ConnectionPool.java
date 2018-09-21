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

package io.reactiverse.pgclient.impl;

import io.reactiverse.pgclient.PgException;
import io.reactiverse.pgclient.PgPoolOptions;
import io.vertx.core.*;
import io.vertx.core.impl.NoStackTraceThrowable;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConnectionPool {

  private final Consumer<Handler<AsyncResult<Connection>>> connector;
  private final int maxSize;
  private final ArrayDeque<Future<Connection>> waiters = new ArrayDeque<>();
  private final Set<PooledConnection> all = new HashSet<>();
  private final ArrayDeque<PooledConnection> available = new ArrayDeque<>();
  private int size;
  private final int maxWaitQueueSize;
  private boolean checkInProgress;
  private boolean closed;

  public ConnectionPool(Consumer<Handler<AsyncResult<Connection>>> connector) {
    this(connector, PgPoolOptions.DEFAULT_MAX_SIZE, PgPoolOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE);
  }

  public ConnectionPool(Consumer<Handler<AsyncResult<Connection>>> connector, int maxSize) {
    this(connector, maxSize, PgPoolOptions.DEFAULT_MAX_WAIT_QUEUE_SIZE);
  }

  public ConnectionPool(Consumer<Handler<AsyncResult<Connection>>> connector, int maxSize, int maxWaitQueueSize) {
    this.maxSize = maxSize;
    this.maxWaitQueueSize = maxWaitQueueSize;
    this.connector = connector;
  }

  public int available() {
    return available.size();
  }

  public int size() {
    return size;
  }

  public void acquire(Handler<AsyncResult<Connection>> holder) {
    if (closed) {
      throw new IllegalStateException("Connection pool closed");
    }
    waiters.add(Future.<Connection>future().setHandler(holder));
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
    Future<Connection> failure = Future.failedFuture("Connection pool close");
    for (Future<Connection> pending : waiters) {
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
    public Connection connection() {
      return this;
    }

    @Override
    public boolean isSsl() {
      return conn.isSsl();
    }

    @Override
    public void schedule(CommandBase<?> cmd) {
      conn.schedule(cmd);
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
    public void handleNotification(int processId, String channel, String payload) {
      if (holder != null) {
        holder.handleNotification(processId, channel, payload);
      }
    }

    @Override
    public void handleException(Throwable err) {
      if (holder != null) {
        holder.handleException(err);
      }
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
            Future<Connection> waiter = waiters.poll();
            waiter.complete(proxy);
          } else {
            if (size < maxSize) {
              size++;
              connector.accept(ar -> {
                if (ar.succeeded()) {
                  Connection conn = ar.result();
                  PooledConnection proxy = new PooledConnection(conn);
                  all.add(proxy);
                  conn.init(proxy);
                  release(proxy);
                } else {
                  size--;
                  Future<Connection> waiter;
                  while ((waiter = waiters.poll()) != null) {
                    waiter.fail(ar.cause());
                  }
                }
              });
            } else {
              if (maxWaitQueueSize >= 0) {
                int numInProgress = size - all.size();
                int numToFail = waiters.size() - (maxWaitQueueSize + numInProgress);
                while (numToFail-- > 0) {
                  Future<Connection> waiter = waiters.pollLast();
                  waiter.fail(new NoStackTraceThrowable("Max waiter size reached"));
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
