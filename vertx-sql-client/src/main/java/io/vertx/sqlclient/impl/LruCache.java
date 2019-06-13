package io.vertx.sqlclient.impl;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A reusable cache implementation based on LRU(Least Recently Used) strategy.
 */
public class LruCache<K, V> extends LinkedHashMap<K, V> {
  private final int capacity;

  public LruCache(int capacity) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
    return size() > capacity;
  }

  public int getCapacity() {
    return this.capacity;
  }

  public Map.Entry<K, V> getEldestEntry() {
    if (size() == 0) {
      return null;
    }
    return (Map.Entry<K, V>) entrySet().toArray()[size() - 1];
  }
}
