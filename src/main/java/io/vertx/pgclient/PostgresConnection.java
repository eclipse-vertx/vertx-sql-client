package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */

public interface PostgresConnection {

  void exceptionHandler(Handler<Throwable> handler);

  void execute(String sql, Handler<AsyncResult<Result>> handler);

  void execute(String sql, List<Object> params, Handler<AsyncResult<Result>> handler);

  void closeHandler(Handler<Void> handler);

  void close();

}
