package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.encoder.message.Query;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

class QueryCommand extends QueryCommandBase {

  private final String sql;

  QueryCommand(String sql, QueryResultHandler handler) {
    super(handler);
    this.sql = sql;
  }

  @Override
  void exec(DbConnection conn, Handler<Void> handler) {
    doneHandler = handler;
    conn.writeMessage(new Query(sql));
  }

  public String getSql() {
    return sql;
  }
}
