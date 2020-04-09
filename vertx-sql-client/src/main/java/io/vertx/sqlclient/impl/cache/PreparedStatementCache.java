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
import io.vertx.core.Promise;
import io.vertx.sqlclient.impl.PreparedStatement;
import io.vertx.sqlclient.impl.SocketConnectionBase;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;

import java.util.HashMap;
import java.util.Map;

/**
 * Cache which manages the lifecycle of all cached prepared statements .
 */
public class PreparedStatementCache {
  private final Map<String, InflightCachingStmtEntry> inflight;
  private final LruCache cache;

  public PreparedStatementCache(SocketConnectionBase conn, int cacheCapacity) {
    this.inflight = new HashMap<>();

    final Handler<AsyncResult<PreparedStatement>> onEvictedHandler = stmtAr -> {
      if (stmtAr.succeeded()) {
        // the stmt is evicted from the cache, we need to close it
        CloseStatementCommand cmd = new CloseStatementCommand(stmtAr.result());
        conn.schedule(cmd, Promise.promise());
      } else {
        // no need to close a failure stmt
      }
    };
    this.cache = new LruCache(cacheCapacity, onEvictedHandler);
  }

  /**
   * Append a new prepared statement request to this cache.
   *
   * @param sql the sql string to be prepare
   * @param originalHandler the original prepare command handler
   * @return {@code null} if the result has been cached or the network request is inflight,
   * or a new {@code Handler} which represents the handler of all appending req waiters so it can be called when the command response is ready.
   */
  public Handler<AsyncResult<PreparedStatement>> appendStmtReq(String sql, Handler<AsyncResult<PreparedStatement>> originalHandler) {
    AsyncResult<PreparedStatement> preparedStmtCachedResult = cache.get(sql);
    if (preparedStmtCachedResult != null) {
      // result is cached, just return it directly
      originalHandler.handle(preparedStmtCachedResult);
      return null;
    } else {
      InflightCachingStmtEntry inflightCachingStmtEntry = inflight.get(sql);
      if (inflightCachingStmtEntry != null) {
        // prepare stmt req is still inflight, add this to the waiters
        inflightCachingStmtEntry.addWaiter(originalHandler);
        return null;
      } else {
        // we need to create a new entry
        InflightCachingStmtEntry newEntry = new InflightCachingStmtEntry(sql, this);
        newEntry.addWaiter(originalHandler);
        inflight.put(sql, newEntry);
        return newEntry;
      }
    }
  }

  /*
   * we need to know how we handle the close statement command, this cmd might origin from PreparedStatement#close
   * or it's automatically sent by the cache once the stmt is evicted. we should remove the entry from both the inflight and cache.
   */
  public void closeStmt(PreparedStatement preparedStatement) {
    this.inflight.remove(preparedStatement.sql());
    this.cache.remove(preparedStatement.sql());
  }

  LruCache cache() {
    return cache;
  }

  Map<String, InflightCachingStmtEntry> inflight() {
    return inflight;
  }
}
