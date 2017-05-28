package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.PgResultSet;
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
public class ResultSetBuilder implements QueryResultHandler {

  private ResultSet result;
  private ResultSet current;
  private boolean completed;
  private final Handler<AsyncResult<ResultSet>> handler;

  public ResultSetBuilder(Handler<AsyncResult<ResultSet>> handler) {
    this.handler = handler;
  }

  @Override
  public void beginResult(List<String> columnNames) {
    ResultSet next = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
    if (current != null) {
      current.setNext(next);
      current = next;
    } else {
      result = current = next;
    }
  }

  @Override
  public void handleRow(JsonArray row) {
    current.getResults().add(row);
  }

  @Override
  public void endResult(boolean suspended) {
  }

  @Override
  public void end() {
    if (!completed) {
      completed = true;
      handler.handle(Future.succeededFuture(result));
    }
  }

  @Override
  public void fail(Throwable cause) {
    if (!completed) {
      completed = true;
      handler.handle(Future.failedFuture(cause));
    }
  }
}
