package com.julienviet.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public class StartupCommand implements Command<DbConnection> {

  private final Handler<AsyncResult<DbConnection>> handler;
  final String username;
  final String database;

  public StartupCommand(String username, String database, Handler<AsyncResult<DbConnection>> handler) {
    this.username = username;
    this.database = database;
    this.handler = handler;
  }

  @Override
  public void onSuccess(DbConnection result) {
    handler.handle(Future.succeededFuture(result));
  }

  @Override
  public void onError(String message) {
    handler.handle(Future.failedFuture(message));
  }
}
