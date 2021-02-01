package io.vertx.sqlclient.impl;

import java.util.Comparator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * ExpiryQueue tracks elements in sorted time.
 * It's used by ConnectionPool to expire connections.
 */
public class ExpiryQueue<E> {
  private final Map<E, Long> elemMap = new ConcurrentHashMap<>();
  private final TreeMap<Long, E> expiryMap = new TreeMap<>();
  private final long idleTimeOut;

  public ExpiryQueue(long idle) {
    idleTimeOut = idle;
  }

  /**
   * Removes element from the queue.
   *
   * @param elem element to remove
   * @return time at which the element was set to expire
   */
  public Long remove(E elem) {
    Long expiryTime = elemMap.remove(elem);
    if (expiryTime != null) {
      expiryMap.remove(expiryTime);
    }
    return expiryTime;
  }

  /**
   * Adds or updates expiration time for element in queue.
   *
   * @param elem element to add/update
   * @return time at which the element is now set to expire.
   */
  public Long add(E elem) {
    long newExpiryTime = System.currentTimeMillis() + idleTimeOut;

    E newElem = expiryMap.get(newExpiryTime);
    if (newElem == null) {
      expiryMap.putIfAbsent(newExpiryTime, elem);
    }

    elemMap.putIfAbsent(elem, newExpiryTime);
    return newExpiryTime;
  }

  /**
   * Removes the next expired item from expiryMap.
   *
   * @return an element that are not expired, or null if all are expired.
   */
  public E poll() {
    long now = System.currentTimeMillis();

    Entry<Long, E> expiryMapEntry = (idleTimeOut != 0) ? expiryMap.ceilingEntry(now) : expiryMap.firstEntry();
    if (expiryMapEntry == null) {
      flushAll();
      return null;
    }

    E value = expiryMapEntry.getValue();
    remove(value);

    return value;
  }

  /**
   * amount of available items from expiryMap.
   *
   * @return size of expiryMap.
   */
  public int size() {
    return expiryMap.size();
  }

  private void flushAll() {
    expiryMap.keySet().removeAll(expiryMap.keySet());
    elemMap.keySet().removeAll(elemMap.keySet());
  }
}
