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

package com.julienviet.pgclient.impl.pool;

import com.julienviet.pgclient.impl.Connection;
import com.julienviet.pgclient.impl.ConnectionHolder;
import io.vertx.core.*;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class ConcurrentConnectionPool implements ConnectionPool {

  private final Consumer<Handler<AsyncResult<Connection>>> connector;
  private final int maxSize;
  private final ArrayDeque<Future<Connection>> waiters = new ArrayDeque<>();
  private final Set<PooledConnection> all = new HashSet<>();
  private final ArrayDeque<PooledConnection> available = new ArrayDeque<>();
  private int size;

  public ConcurrentConnectionPool(Consumer<Handler<AsyncResult<Connection>>> connector, int maxSize) {
    this.maxSize = maxSize;
    this.connector = connector;
  }

  @Override
  public void acquire(Handler<AsyncResult<Connection>> holder) {
    waiters.add(Future.<Connection>future().setHandler(holder));
    check();
  }

  @Override
  public void close() {
    for (PooledConnection pooled : new ArrayList<>(all)) {
      pooled.close();
    }
  }

  class PooledConnection extends ConnectionProxy {

    private ConnectionHolder holder;

    public PooledConnection(Connection conn) {
      super(conn);
    }

    @Override
    public void init(ConnectionHolder holder) {
      this.holder = holder;
    }

    @Override
    public void close(ConnectionHolder holder) {
                /*
                Context current = Vertx.currentContext();
                if (current == context) {
                  pooling.release(proxy);
                } else {
                  context.runOnContext(v -> pooling.release(proxy));
                }*/
      this.holder = null;
      available.add(this);
      check();
    }

    @Override
    public void handleClosed() {
      all.remove(this);
      available.remove(this);
      size--;
      if (holder != null) {
        holder.handleClosed();
      }
      check();
    }

    @Override
    public void handleException(Throwable err) {
      if (holder != null) {
        holder.handleException(err);
      }
    }
  }

  private void doAcq(Handler<AsyncResult<PooledConnection>> handler) {
    if (available.size() > 0) {
      PooledConnection proxy = available.poll();
      handler.handle(Future.succeededFuture(proxy));
    } else {
      if (size < maxSize) {
        size++;
        connector.accept(ar -> {
          if (ar.succeeded()) {
            Connection conn = ar.result();
            PooledConnection proxy = new PooledConnection(conn);
            all.add(proxy);
            available.add(proxy);
            conn.init(proxy);
            doAcq(handler);
          } else {
            handler.handle(Future.failedFuture(ar.cause()));
          }
        });
      }
    }
  }

  private void check() {
    if (waiters.size() > 0) {
      doAcq(ar -> {
        if (ar.succeeded()) {
          PooledConnection proxy = ar.result();
          Future<Connection> waiter = waiters.poll();
          waiter.complete(proxy);
        } else {
          Future<Connection> waiter;
          while ((waiter = waiters.poll()) != null) {
            waiter.fail(ar.cause());
          }
        }
      });
    }
  }
}
