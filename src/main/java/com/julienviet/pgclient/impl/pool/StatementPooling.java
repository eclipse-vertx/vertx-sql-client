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

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
class StatementPooling implements PgPoolImpl.PoolingStrategy {

  private final Set<PgPoolImpl.Holder> holders = new HashSet<>();
  private PgConnection shared;
  private boolean connecting;
  private ArrayDeque<PgPoolImpl.Holder> waiters = new ArrayDeque<>();
  private PgClientImpl client;

  StatementPooling(PgClientImpl client) {
    this.client = client;
  }

  @Override
  public void close() {
    if (shared != null) {
      shared.close();
    }
  }

  @Override
  public void acquire(PgPoolImpl.Holder holder) {
    if (shared != null) {
      holders.add(holder);
      holder.complete(shared);
    } else {
      waiters.add(holder);
      if (!connecting) {
        connecting = true;
        client.connect(ar -> {
          connecting = false;
          if (ar.succeeded()) {
            PgConnection conn = ar.result();
            shared = conn;
            conn.exceptionHandler(err -> {
              for (PgPoolImpl.Holder proxy : new ArrayList<>(holders)) {
                proxy.handleException(err);
              }
            });
            conn.closeHandler(v -> {
              shared = null;
              conn.exceptionHandler(null);
              conn.closeHandler(null);
              ArrayList<PgPoolImpl.Holder> list = new ArrayList<>(holders);
              holders.clear();
              for (PgPoolImpl.Holder proxy : list) {
                proxy.handleClosed();
              }
            });
            PgPoolImpl.Holder waiter;
            while ((waiter = waiters.poll()) != null) {
              holders.add(waiter);
              waiter.complete(conn);
            }
          } else {
            PgPoolImpl.Holder waiter;
            while ((waiter = waiters.poll()) != null) {
              waiter.fail(ar.cause());
            }
          }
        });
      }
    }
  }

  @Override
  public void release(PgPoolImpl.Holder holder) {
    holders.remove(holder);
  }
}
