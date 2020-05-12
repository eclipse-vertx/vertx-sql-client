package io.vertx.sqlclient.impl.codec;

/**
 * An event signals when a cached prepared statement is invalid and needs to be evicted from the cache.
 */
public class InvalidCachedStatementEvent {
  private final String sql;

  public InvalidCachedStatementEvent(String sql) {
    this.sql = sql;
  }

  public String sql() {
    return sql;
  }
}
