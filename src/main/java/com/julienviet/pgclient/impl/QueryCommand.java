package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.ext.sql.ResultSet;

import java.util.ArrayList;
import java.util.List;

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
  boolean exec(DbConnection conn) {
    conn.writeToChannel(new Query(sql));
    return true;
  }

  public String getSql() {
    return sql;
  }
}
