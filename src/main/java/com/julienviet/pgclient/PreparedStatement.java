package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PreparedStatement {

  Batch batch();

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}
