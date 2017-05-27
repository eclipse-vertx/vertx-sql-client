package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgConnectionPool {

  void getConnection(Handler<AsyncResult<PgConnection>> handler);

  void close();

}
