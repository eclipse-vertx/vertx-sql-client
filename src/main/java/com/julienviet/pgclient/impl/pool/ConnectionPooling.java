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

import com.julienviet.pgclient.PgConnection;
import com.julienviet.pgclient.impl.PgClientImpl;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class ConnectionPooling implements PgPoolImpl.PoolingStrategy {

  private final ArrayDeque<PgPoolImpl.Holder> waiters = new ArrayDeque<>();
  private final int maxSize;
  private final Set<PgConnection> all = new HashSet<>();
  private final PgClientImpl client;
  private final ArrayDeque<PgConnection> strategy = new ArrayDeque<>();
  private int connCount;

  ConnectionPooling(PgClientImpl client, int maxSize) {
    this.maxSize = maxSize;
    this.client = client;
  }

  @Override
  public void acquire(PgPoolImpl.Holder holder) {
    waiters.add(holder);
    check();
  }

  @Override
  public void close() {
    for (PgConnection conn : new ArrayList<>(all)) {
      conn.close();
    }
  }

  private void doAcq(Handler<AsyncResult<PgConnection>> handler) {
    if (strategy.size() > 0) {
      PgConnection conn = strategy.poll();
      handler.handle(Future.succeededFuture(conn));
    } else {
      if (connCount < maxSize) {
        connCount++;
        client.connect(ar -> {
          if (ar.succeeded()) {
            PgConnection conn = ar.result();
            all.add(conn);
            strategy.add(conn);
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
          PgConnection conn = ar.result();
          PgPoolImpl.Holder waiter = waiters.poll();
          conn.exceptionHandler(waiter::handleException);
          conn.closeHandler(v -> {
            all.remove(conn);
            connCount--;
            check();
            waiter.handleClosed();
          });
          waiter.complete(conn);
        } else {
          PgPoolImpl.Holder waiter;
          while ((waiter = waiters.poll()) != null) {
            waiter.fail(ar.cause());
          }
        }
      });
    }
  }

  @Override
  public void release(PgPoolImpl.Holder holder) {
    PgConnection conn = holder.connection();
    conn.closeHandler(null);
    conn.exceptionHandler(null);
    strategy.add(conn);
    check();
  }

  public void release(Proxy proxy) {
    PgConnection conn = proxy.connection();
    conn.closeHandler(null);
    conn.exceptionHandler(null);
    strategy.add(conn);
    check();
  }
}
