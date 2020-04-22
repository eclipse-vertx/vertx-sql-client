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

class InflightCachingStmtEntry implements Handler<AsyncResult<PreparedStatement>> {
  private final Deque<Handler<AsyncResult<PreparedStatement>>> waiters = new ArrayDeque<>();
  private final String sql;
  private final PreparedStatementCache psCache;

  InflightCachingStmtEntry(String sql, PreparedStatementCache psCache) {
    this.sql = sql;
    this.psCache = psCache;
  }

  void addWaiter(Handler<AsyncResult<PreparedStatement>> handler) {
    waiters.add(handler);
  }

  @Override
  public void handle(AsyncResult<PreparedStatement> preparedStatementResult) {
    psCache.cache().put(sql, preparedStatementResult); // put it in the cache since the response is ready
    psCache.inflight().remove(sql);
    Handler<AsyncResult<PreparedStatement>> waiter;
    while ((waiter = waiters.poll()) != null) {
      waiter.handle(preparedStatementResult);
    }
  }
}
