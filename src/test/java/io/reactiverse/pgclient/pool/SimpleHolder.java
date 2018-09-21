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

package io.reactiverse.pgclient.pool;

import io.reactiverse.pgclient.impl.Connection;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

class SimpleHolder implements Connection.Holder, Handler<AsyncResult<Connection>> {

  private Connection conn;
  private Throwable failure;
  private int closed;

  SimpleHolder() {
  }

  int closed() {
    return closed;
  }

  boolean isConnected() {
    return conn != null;
  }

  boolean isComplete() {
    return isConnected() || isFailed();
  }

  boolean isFailed() {
    return failure != null;
  }

  Throwable failure() {
    return failure;
  }

  void init() {
    conn.init(this);
  }

  @Override
  public void handle(AsyncResult<Connection> ar) {
    if (ar.succeeded()) {
      conn = ar.result();
    } else {
      failure = ar.cause();
    }
  }

  @Override
  public Connection connection() {
    return conn;
  }

  @Override
  public void handleClosed() {
    closed++;
  }

  @Override
  public void handleException(Throwable err) {
  }

  @Override
  public void handleNotification(int processId, String channel, String payload) {
  }

  void close() {
    conn.close(this);
  }
}
