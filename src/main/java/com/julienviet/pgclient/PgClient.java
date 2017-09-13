package com.julienviet.pgclient;

import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import com.julienviet.pgclient.impl.PostgresClientImpl;
import io.vertx.ext.sql.SQLClient;

/**
 * The entry point for interacting with a Postgres database.
 *
 * @author <a href="mailto:julien@julienviet.com">Julien Viet</a>
 */
public interface PgClient extends SQLClient {

  /**
   * Create a client.
   *
   * @param vertx the vertx instance
   * @param options the client options
   * @return the client
   */
  static PgClient create(Vertx vertx, PgClientOptions options) {
    return new PostgresClientImpl(vertx, options);
  }

  /**
   * Connects to the database and returns the connection if that succeeds.
   * <p/>
   * The connection interracts directly with the database is not a proxy, so closing the
   * connection will close the underlying connection to the database.
   *
   * @param completionHandler the handler called with the connection or the failure
   */
  void connect(Handler<AsyncResult<PgConnection>> completionHandler);

  /**
   * Create a connection pool to the database configured with the given {@code options}.
   *
   * @param options the options for creating the pool
   * @return the connection pool
   */
  PgConnectionPool createPool(PgPoolOptions options);

}
