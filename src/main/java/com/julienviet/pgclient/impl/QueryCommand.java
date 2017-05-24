package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.codec.Message;
import com.julienviet.pgclient.codec.decoder.message.ReadyForQuery;
import com.julienviet.pgclient.codec.encoder.message.Query;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

class QueryCommand extends QueryCommandBase {

  private final String sql;
  private final Handler<AsyncResult<ResultSet>> handler;
  private ResultSet result;
  private boolean completed;

  QueryCommand(String sql, Handler<AsyncResult<ResultSet>> handler) {
    this.handler = handler;
    this.sql = sql;
  }

  @Override
  boolean exec(DbConnection conn) {
    conn.writeToChannel(new Query(sql));
    return true;
  }

  @Override
  public boolean handleMessage(Message msg) {
    if (msg.getClass() == ReadyForQuery.class) {
      if (!completed) {
        completed = true;
        handler.handle(Future.succeededFuture(result));
      }
      return true;
    } else {
      return super.handleMessage(msg);
    }
  }

  @Override
  void handleResult(ResultSet next) {
    if (result == null) {
      result = next;
    } else {
      ResultSet current = result;
      while (current.getNext() != null) {
        current = current.getNext();
      }
      current.setNext(next);
    }
  }

  @Override
  void fail(Throwable cause) {
    if (!completed) {
      completed = true;
      handler.handle(Future.failedFuture(cause));
    }
  }

  public String getSql() {
    return sql;
  }
}
