package io.vertx.mysqlclient.impl.codec;

/**
 * An event that signals all cached statements must be cleared from the cache.
 */
public class ClearCachedStatementsEvent {

  public static final ClearCachedStatementsEvent INSTANCE = new ClearCachedStatementsEvent();

  private ClearCachedStatementsEvent() {
    // Singleton
  }
}
