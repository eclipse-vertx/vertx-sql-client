/*
 * Copyright (c) 2011-2020 Contributors to the Eclipse Foundation
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0, or the Apache License, Version 2.0
 * which is available at https://www.apache.org/licenses/LICENSE-2.0.
 *
 * SPDX-License-Identifier: EPL-2.0 OR Apache-2.0
 */

package io.vertx.sqlclient.impl.cache;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.sqlclient.impl.PreparedStatement;

import java.util.ArrayDeque;
import java.util.Deque;

public class CachedPreparedStatement implements Handler<AsyncResult<PreparedStatement>> {

  private final Deque<Handler<AsyncResult<PreparedStatement>>> waiters = new ArrayDeque<>();
  AsyncResult<PreparedStatement> resp;

  public void get(Handler<AsyncResult<PreparedStatement>> handler) {
    if (resp != null) {
      handler.handle(resp);
    } else {
      waiters.add(handler);
    }
  }

  @Override
  public void handle(AsyncResult<PreparedStatement> event) {
    resp = event;
    Handler<AsyncResult<PreparedStatement>> waiter;
    while ((waiter = waiters.poll()) != null) {
      waiter.handle(resp);
    }
  }

  public Deque<Handler<AsyncResult<PreparedStatement>>> waiters() {
    return waiters;
  }
}
