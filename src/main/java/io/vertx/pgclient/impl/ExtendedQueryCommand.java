package io.vertx.pgclient.impl;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.pgclient.Result;

import java.util.List;

/**
 * @author <a href="mailto:emad.albloushi@gmail.com">Emad Alblueshi</a>
 */

public class ExtendedQueryCommand implements Command {

  private final String sql;
  private final List<Object> params;
  final Handler<AsyncResult<Result>> handler;

  public ExtendedQueryCommand(String sql, List<Object> params, Handler<AsyncResult<Result>> handler) {
    this.sql = sql;
    this.params = params;
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

  public List<Object> getParams() {
    return params;
  }
}
