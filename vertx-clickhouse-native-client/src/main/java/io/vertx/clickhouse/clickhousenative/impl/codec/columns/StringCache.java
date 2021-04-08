package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

public class StringCache {
  private final int nElements;
  private SoftReference<String[]> stringCache;

  public StringCache(int nElements) {
    this.nElements = nElements;
    this.stringCache = new SoftReference<>(new String[nElements]);
  }

  public String get(int rowIdx, Supplier<String> supplier) {
    String[] cache = stringCache.get();
    String ret;
    if (cache == null) {
      cache = new String[nElements];
      stringCache = new SoftReference<>(cache);
      ret = supplier.get();
      cache[rowIdx] = ret;
    } else {
      ret = cache[rowIdx];
      if (ret == null) {
        ret = supplier.get();
        cache[rowIdx] = ret;
      }
    }
    return ret;
  }
}
