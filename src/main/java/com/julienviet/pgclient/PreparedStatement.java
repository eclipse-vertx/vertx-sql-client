package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.ext.sql.ResultSet;

import java.util.List;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PreparedStatement {

  void execute(PostgresBatch batch, Handler<AsyncResult<List<ResultSet>>> resultHandler);

  void close();

  void close(Handler<AsyncResult<Void>> completionHandler);

}
