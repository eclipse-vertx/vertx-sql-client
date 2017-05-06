package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PostgresConnectionPool {

  void getConnection(Handler<AsyncResult<PostgresConnection>> handler);

  void close();

}
