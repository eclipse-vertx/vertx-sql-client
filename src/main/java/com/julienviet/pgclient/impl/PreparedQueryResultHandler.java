package com.julienviet.pgclient.impl;

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
public class PreparedQueryResultHandler implements QueryResultHandler {

  private ResultSet result;
  private boolean suspended;
  private final Handler<AsyncResult<ResultSet>> handler;

  public PreparedQueryResultHandler(Handler<AsyncResult<ResultSet>> handler) {
    this.handler = handler;
  }

  public boolean suspended() {
    return suspended;
  }

  @Override
  public void beginResult(List<String> columnNames) {
    result = new ResultSet().setColumnNames(columnNames).setResults(new ArrayList<>());
  }

  @Override
  public void handleRow(JsonArray row) {
    result.getResults().add(row);
  }

  @Override
  public void endResult(boolean suspended) {
    this.suspended = suspended;
    handler.handle(Future.succeededFuture(result));
  }

  @Override
  public void fail(Throwable cause) {
    handler.handle(Future.failedFuture(cause));
  }

  @Override
  public void end() {
  }
}
