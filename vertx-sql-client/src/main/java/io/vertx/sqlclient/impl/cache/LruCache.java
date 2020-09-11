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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A LRU replacement strategy cache based on {@link java.util.LinkedHashMap} for prepared statements.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {

  List<V> removed;
  private final int capacity;

  public LruCache(int capacity) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  /**
   * Evict the eldest entry from the cache
   *
   * @return the eldest value or {@code null}
   */
  public V evict() {
    Iterator<V> it = values().iterator();
    if (it.hasNext()) {
      V value = it.next();
      it.remove();
      return value;
    } else {
      return null;
    }
  }

  public List<V> cache(K key, V value) {
    put(key, value);
    if (removed != null) {
      List<V> evicted = removed;
      removed = null;
      return evicted;
    } else {
      return Collections.emptyList();
    }
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    boolean evict = size() > capacity;
    if (evict) {
      if (removed == null) {
        removed = new ArrayList<>();
      }
      removed.add(eldest.getValue());
      return true;
    } else {
      return false;
    }
  }
}
