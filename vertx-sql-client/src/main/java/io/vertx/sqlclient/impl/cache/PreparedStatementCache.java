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

import io.vertx.sqlclient.impl.PreparedStatement;

import java.util.List;

/**
 * Cache which manages the lifecycle of all cached prepared statements .
 */
public class PreparedStatementCache {

  private final int capacity;
  private final LruCache<String, PreparedStatement> cache;

  public PreparedStatementCache(int cacheCapacity) {
    this.capacity = cacheCapacity;
    this.cache = new LruCache<>(cacheCapacity);
  }

  public PreparedStatement get(String sql) {
    return cache.get(sql);
  }

  /**
   * Put a statement in the cache.
   *
   * @param preparedStatement the prepared statement to cache
   * @return the list of prepared statement to evict and close
   */
  public List<PreparedStatement> put(PreparedStatement preparedStatement) {
    return cache.cache(preparedStatement.sql(), preparedStatement);
  }

  /**
   * Remove the cached entry when the cached statement is closing so that pending requests will not use a closed prepared statement.
   *
   * @param sql the identified sql of the cached statement
   */
  public void remove(String sql) {
    this.cache.remove(sql);
  }

  public PreparedStatement evict() {
    return cache.evict();
  }

  public boolean isFull() {
    return cache.size() == capacity;
  }

  /**
   * @return the cache size
   */
  public int size() {
    return cache.size();
  }
}
