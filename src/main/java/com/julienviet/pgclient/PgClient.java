package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import com.julienviet.pgclient.impl.PgClientImpl;
import io.vertx.ext.sql.SQLClient;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgClient extends SQLClient {

  static PgClient create(Vertx vertx, PgClientOptions options) {
    return new PgClientImpl(vertx, options);
  }

  void connect(Handler<AsyncResult<PgConnection>> completionHandler);

  PgConnectionPool createPool(int size);
}
