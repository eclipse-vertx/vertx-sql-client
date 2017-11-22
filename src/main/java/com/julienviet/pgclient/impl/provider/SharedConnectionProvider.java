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

package com.julienviet.pgclient.impl.provider;

import com.julienviet.pgclient.impl.Connection;
import com.julienviet.pgclient.impl.ConnectionHolder;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class SharedConnectionProvider implements ConnectionProvider {

  private final Set<ConnectionHolder> holders = new HashSet<>();
  private ConnectionProxy shared;
  private boolean connecting;
  private ArrayDeque<Future<Connection>> waiters = new ArrayDeque<>();
  private Consumer<Handler<AsyncResult<Connection>>> connector;

  public SharedConnectionProvider(Consumer<Handler<AsyncResult<Connection>>> connector) {
    this.connector = connector;
  }

  @Override
  public void close() {
    if (shared != null) {
      shared.close();
    }
  }

  @Override
  public void acquire(Handler<AsyncResult<Connection>> waiter) {
    Future<Connection> fut = Future.<Connection>future().setHandler(waiter);
    if (shared != null) {
      fut.complete(shared);
    } else {
      waiters.add(fut);
      if (!connecting) {
        connecting = true;
        connector.accept(ar -> {
          connecting = false;
          if (ar.succeeded()) {
            Connection conn = ar.result();
            shared = new ConnectionProxy(conn) {
              @Override
              public void init(ConnectionHolder holder) {
                if (holders.contains(holder)) {
                  throw new IllegalStateException();
                }
                holders.add(holder);
              }
              @Override
              public void close(ConnectionHolder holder) {
                if (!holders.remove(holder)) {
                  throw new IllegalStateException();
                }
              }
              @Override
              public void handleClosed() {
                if (shared == null) {
                  throw new IllegalStateException();
                }
                shared = null;
                ArrayList<ConnectionHolder> copy = new ArrayList<>(holders);
                holders.clear();
                for (ConnectionHolder holder : copy) {
                  holder.handleClosed();
                }
              }
              @Override
              public void handleException(Throwable err) {
                for (ConnectionHolder holder : new ArrayList<>(holders)) {
                  holder.handleException(err);
                }
              }
            };
            conn.init(shared);
            Future<Connection> waiter_;
            while ((waiter_ = waiters.poll()) != null) {
              waiter_.complete(shared);
            }
          } else {
            Future<Connection> waiter_;
            while ((waiter_ = waiters.poll()) != null) {
              waiter_.fail(ar.cause());
            }
          }
        });
      }
    }
  }
}
