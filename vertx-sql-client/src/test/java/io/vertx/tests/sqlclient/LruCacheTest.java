package io.vertx.tests.sqlclient;

import io.vertx.sqlclient.impl.connection.LruCache;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class LruCacheTest {

  @Test
  public void testEvictOnInsertion() {
    int capacity = 1024;
    LruCache<String, String> cache = new LruCache<>(capacity);
    for (int i = 0;i < capacity;i++) {
      String key =  "key-" + i;
      String value = "value-" + i;
      assertEquals(0, cache.cache(key, value).size());
    }
    List<String> evicted = cache.cache("key-1024", "value-1024");
    assertEquals(1, evicted.size());
    assertEquals("value-0", evicted.iterator().next());
    assertEquals(1024, cache.size());
  }

  @Test
  public void testEvict() {
    int capacity = 1024;
    LruCache<String, String> cache = new LruCache<>(capacity);
    for (int i = 0;i < capacity;i++) {
      String key =  "key-" + i;
      String value = "value-" + i;
      assertEquals(0, cache.cache(key, value).size());
    }
    String evicted = cache.evict();
    assertEquals("value-0", evicted);
    assertEquals(1023, cache.size());
  }

  @Test
  public void testCacheCleared() {
    LruCache<String, String> cache = new LruCache<>(42);
    List<String> evicted = cache.cache("foo", "bar");
    assertTrue(evicted.isEmpty());
    assertNotNull(cache.get("foo"));
    cache.clear();
    assertNull(cache.get("foo"));
  }
}
