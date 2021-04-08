package io.vertx.clickhouse.clickhousenative.impl.codec.columns;

import io.vertx.core.impl.logging.Logger;
import io.vertx.core.impl.logging.LoggerFactory;

import java.lang.ref.SoftReference;
import java.util.function.Supplier;

public class StringCache {
  private static final Logger LOG = LoggerFactory.getLogger(StringCache.class);

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
      LOG.info(this.hashCode() + ": MISS: " + ret + "/" + rowIdx);
    } else {
      ret = cache[rowIdx];
      if (ret == null) {
        ret = supplier.get();
        cache[rowIdx] = ret;
        LOG.info(this.hashCode() + ": MISS: " + ret + "/" + rowIdx);
      } else {
        LOG.info(this.hashCode() + ": MISS: " + ret + "/" + rowIdx);
      }
    }
    return ret;
  }
}
