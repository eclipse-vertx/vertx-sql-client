package com.julienviet.pgclient.impl;

import com.julienviet.pgclient.Result;
import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class CloseStatementCommand implements Command {

  final PreparedStatementImpl ps;
  final Handler<AsyncResult<Void>> handler;

  public CloseStatementCommand(PreparedStatementImpl ps, Handler<AsyncResult<Void>> handler) {
    this.ps = ps;
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
