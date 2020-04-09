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

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LRU replacement strategy cache based on {@link java.util.LinkedHashMap} for prepared statements.
 */
class LruCache extends LinkedHashMap<String, AsyncResult<PreparedStatement>> {
  private final int capacity;
  private final Handler<AsyncResult<PreparedStatement>> onEvictedHandler;

  LruCache(int capacity, Handler<AsyncResult<PreparedStatement>> onEvictedHandler) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
    this.onEvictedHandler = onEvictedHandler;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, AsyncResult<PreparedStatement>> eldest) {
    boolean evict = size() > capacity;
    if (evict) {
      onEvictedHandler.handle(eldest.getValue());
      return true;
    } else {
      return false;
    }
  }
}
