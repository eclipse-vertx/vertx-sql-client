package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CloseStatementCommand implements Command {

  final String stmt;
  final Handler<AsyncResult<Void>> handler;

  public CloseStatementCommand(String stmt, Handler<AsyncResult<Void>> handler) {
    this.stmt = stmt;
    this.handler = handler;
  }

  @Override
  public void onSuccess(Result result) {
    handler.handle(Future.succeededFuture());
  }

  @Override
  public void onError(String message) {
    handler.handle(Future.failedFuture(message));
  }
}
