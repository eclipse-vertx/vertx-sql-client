package io.vertx.sqlclient.impl;

import io.vertx.sqlclient.impl.SocketConnectionBase.CachedPreparedStatement;
import io.vertx.sqlclient.impl.command.CloseStatementCommand;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A LRU replacement strategy cache based on {@link java.util.LinkedHashMap} for prepared statements.
 */
class PreparedStatementCache extends LinkedHashMap<String, CachedPreparedStatement> {
  private final int capacity;
  private final Connection conn;

  PreparedStatementCache(int capacity, Connection conn) {
    super(capacity, 0.75f, true);
    this.capacity = capacity;
    this.conn = conn;
  }

  @Override
  protected boolean removeEldestEntry(Map.Entry<String, CachedPreparedStatement> eldest) {
    boolean needRemove = size() > capacity;
    CachedPreparedStatement cachedPreparedStatementToRemove = eldest.getValue();

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
    Map.Entry<String, CachedPreparedStatement> entry = getEldestEntry();
    if (entry == null) {
      return true;
    } else {
      return entry.getValue().resp != null;
    }
  }

  public int getCapacity() {
    return this.capacity;
  }

  private Map.Entry<String, CachedPreparedStatement> getEldestEntry() {
    if (size() == 0) {
      return null;
    }
    return (Map.Entry<String, CachedPreparedStatement>) entrySet().toArray()[size() - 1];
  }
}
