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
  private final Handler<AsyncResult<ResultSet>> handler;
  private ResultSet result;
  private ResultSet current;
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
  void handleDescription(List<String> columnNames) {
    ResultSet next = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
    if (current != null) {
      current.setNext(next);
      current = next;
    } else {
      result = current = next;
    }
  }

  @Override
  void handleRow(JsonArray row) {
    current.getResults().add(row);
  }

  @Override
  void handleComplete() {
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
