package io.vertx.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.pgclient.impl.PostgresClientImpl;

/**
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PostgresClient {

  static PostgresClient create(Vertx vertx, PostgresClientOptions options) {
    return new PostgresClientImpl(vertx, options);
  }

  void connect(Handler<AsyncResult<PostgresConnection>> completionHandler);

  void createPool(int size, Handler<AsyncResult<PostgresConnectionPool>> completionHandler);
}
