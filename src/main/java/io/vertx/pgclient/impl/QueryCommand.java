package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.pgclient.Result;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public class QueryCommand implements Command {

  private final String sql;
  final Handler<AsyncResult<Result>> handler;

  public QueryCommand(String sql, Handler<AsyncResult<Result>> handler) {
    this.sql = sql;
    this.handler = handler;
  }

  @Override
  public void onSuccess(Result result) {
    handler.handle(Future.succeededFuture(result));
  }

  @Override
  public void onError(String message) {
    handler.handle(Future.failedFuture(message));
  }

  public String getSql() {
    return sql;
  }
}
