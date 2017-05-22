package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PreparedStatement {

  void execute(PostgresBatch batch, Handler<AsyncResult<List<Result>>> resultHandler);

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}
