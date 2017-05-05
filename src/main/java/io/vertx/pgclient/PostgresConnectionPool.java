package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PostgresConnectionPool {

  void execute(String sql, Handler<AsyncResult<Result>> resultHandler);

  void close();

}
