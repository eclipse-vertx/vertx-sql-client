package com.julienviet.pgclient;

import io.vertx.codegen.annotations.VertxGen;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * A pool of connection.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
@VertxGen
public interface PgConnectionPool {

  /**
   * Obtain a connection from the pool.
   *
   * @param handler the handler that will get the connection result
   */
  void getConnection(Handler<AsyncResult<PgConnection>> handler);

  /**
   * Close the pool and release the associated resources.
   */
  void close();

}
