package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.pgclient.Result;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class Command {

  final String sql;
  final Handler<AsyncResult<Result>> handler;

  public Command(String sql, Handler<AsyncResult<Result>> handler) {
    this.sql = sql;
    this.handler = handler;
  }

  void onSuccess(Result result) {
    handler.handle(Future.succeededFuture(result));
  }

  void onError(String msg) {
    handler.handle(Future.failedFuture(msg));
  }

}
