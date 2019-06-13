package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.impl.command.CloseStatementCommand;

import java.util.Map;

/**
 * A LRU replacement strategy based cache for prepared statements.
 */
class PreparedStatementCache extends LruCache<String, SocketConnectionBase.CachedPreparedStatement> {
  private final Connection conn;

  PreparedStatementCache(int capacity, Connection conn) {
    super(capacity);
    this.conn = conn;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, SocketConnectionBase.CachedPreparedStatement> eldest) {
    boolean needRemove = super.removeEldestEntry(eldest);
    SocketConnectionBase.CachedPreparedStatement cachedPreparedStatementToRemove = eldest.getValue();

    if (needRemove) {
      if (cachedPreparedStatementToRemove.resp.succeeded()) {
        // close the statement after it has been evicted from the cache
        PreparedStatement statement = cachedPreparedStatementToRemove.resp.result();
        CloseStatementCommand cmd = new CloseStatementCommand(statement);
        cmd.handler = ar -> {
        };
        conn.schedule(cmd);
      }
      return true;
    }
    return false;
  }

  public boolean isReady() {
    Map.Entry<String, SocketConnectionBase.CachedPreparedStatement> entry = getEldestEntry();
    if (entry == null) {
      return true;
    } else {
      return entry.getValue().resp != null;
    }
  }
}
